package com.vqms.service;

import com.vqms.model.Counter;
import com.vqms.model.QueueEntry;
import com.vqms.model.User;
import com.vqms.repository.CounterRepository;
import com.vqms.repository.QueueEntryRepository;
import com.vqms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Service
public class QueueService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @Autowired
    private TwilioService twilioService;

    @Autowired
    private EmailService emailService;

    public void joinQueue(String phone, String email, int age) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        user.setPhone(phone);
        user.setEmail(email);
        userRepository.save(user);

        List<Counter> activeCounters = counterRepository.findByCounterStatus("ACTIVE");
        if (activeCounters.isEmpty()) {
            throw new IllegalStateException("No active counters available");
        }

        Counter counter = activeCounters.get(new Random().nextInt(activeCounters.size()));
        List<QueueEntry> activeEntries = queueEntryRepository.findByCounterIdAndPausedFalse(counter.getId());
        int position = activeEntries.size() + 1;

        if (age > 60) {
            position = 1;
            for (QueueEntry entry : activeEntries) {
                entry.setPosition(entry.getPosition() + 1);
                queueEntryRepository.save(entry);
            }
        }

        QueueEntry queueEntry = new QueueEntry();
        queueEntry.setUser(user);
        queueEntry.setCounter(counter);
        queueEntry.setToken(generateToken());
        queueEntry.setPosition(position);
        queueEntryRepository.save(queueEntry);

        twilioService.sendSMS(phone, "You’ve joined the queue! Token: " + queueEntry.getToken() + ", Position: " + position);
        emailService.sendEmail(email, "Queue Position", "You’ve joined the queue! Token: " + queueEntry.getToken() + ", Position: " + position);
    }

    public void pauseQueueEntry() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        QueueEntry entry = queueEntryRepository.findByUserId(user.getId());

        if (entry != null && !entry.isPaused()) {
            entry.setPaused(true);
            entry.setPauseTimestamp(LocalDateTime.now());
            queueEntryRepository.save(entry);
            twilioService.sendSMS(user.getPhone(), "Your queue position is paused for 15 minutes. Token: " + entry.getToken());
        }
    }

    public void resumeQueueEntry() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);
        QueueEntry entry = queueEntryRepository.findByUserId(user.getId());

        if (entry != null && entry.isPaused()) {
            long minutesPaused = ChronoUnit.MINUTES.between(entry.getPauseTimestamp(), LocalDateTime.now());
            if (minutesPaused <= 15) {
                entry.setPaused(false);
                entry.setPauseTimestamp(null);
            } else {
                List<QueueEntry> activeEntries = queueEntryRepository.findByCounterIdAndPausedFalse(entry.getCounter().getId());
                entry.setPosition(activeEntries.size() + 1);
                entry.setPaused(false);
                entry.setPauseTimestamp(null);
            }
            queueEntryRepository.save(entry);
            twilioService.sendSMS(user.getPhone(), "Your queue position is resumed. Token: " + entry.getToken() + ", Position: " + entry.getPosition());
        }
    }

    public void pauseCounter(Long counterId) {
        Counter counter = counterRepository.findById(counterId).orElseThrow();
        if ("ACTIVE".equals(counter.getCounterStatus())) {
            counter.setCounterStatus("PAUSED");
            counter.setLastUpdated(LocalDateTime.now());
            counterRepository.save(counter);

            List<QueueEntry> entries = queueEntryRepository.findByCounterId(counterId);
            List<Counter> activeCounters = counterRepository.findByCounterStatus("ACTIVE");

            if (!activeCounters.isEmpty()) {
                Counter newCounter = activeCounters.get(0);
                for (QueueEntry entry : entries) {
                    entry.setCounter(newCounter);
                    entry.setPosition(queueEntryRepository.findByCounterIdAndPausedFalse(newCounter.getId()).size() + 1);
                    queueEntryRepository.save(entry);
                    twilioService.sendSMS(entry.getUser().getPhone(), "Counter " + counter.getCounterName() + " is paused. You’re now at " + newCounter.getCounterName());
                }
            }
        }
    }

    public void resumeCounter(Long counterId) {
        Counter counter = counterRepository.findById(counterId).orElseThrow();
        if ("PAUSED".equals(counter.getCounterStatus())) {
            counter.setCounterStatus("ACTIVE");
            counter.setLastUpdated(LocalDateTime.now());
            counterRepository.save(counter);
        }
    }

    public void processNext(Long counterId) {
        List<QueueEntry> entries = queueEntryRepository.findByCounterIdAndPausedFalse(counterId);
        if (!entries.isEmpty()) {
            QueueEntry nextEntry = entries.get(0);
            Counter counter = counterRepository.findById(counterId).orElseThrow();
            counter.setCurrentToken(nextEntry.getToken());
            counterRepository.save(counter);
            queueEntryRepository.delete(nextEntry);
            entries.remove(0);

            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).setPosition(i + 1);
                queueEntryRepository.save(entries.get(i));
            }
        }
    }

    private String generateToken() {
        return "Q" + String.format("%03d", queueEntryRepository.count() + 1);
    }
}
