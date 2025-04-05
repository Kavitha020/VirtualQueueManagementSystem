package com.vqms.controller;


import com.vqms.model.QueueEntry;
import com.vqms.repository.QueueEntryRepository;
import com.vqms.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private QueueService queueService;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @GetMapping("/home")
    public String home() {
        return "user/home";
    }

    @GetMapping("/join")
    public String joinQueueForm() {
        return "user/join-queue";
    }

    @PostMapping("/join")
    public String joinQueue(@RequestParam String phone, @RequestParam String email, @RequestParam int age) {
        queueService.joinQueue(phone, email, age);
        return "redirect:/user/status";
    }

    @GetMapping("/status")
    public String queueStatus(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        QueueEntry entry = queueEntryRepository.findByUserId(
                queueEntryRepository.findByUserId(
                        queueEntryRepository.findAll().stream()
                                .filter(e -> e.getUser().getUsername().equals(username))
                                .findFirst().orElseThrow().getUser().getId()
                ).getUser().getId()
        );
        model.addAttribute("queueEntry", entry);
        return "user/queue-status";
    }

    @PostMapping("/pause")
    public String pauseQueue() {
        queueService.pauseQueueEntry();
        return "redirect:/user/status";
    }

    @PostMapping("/resume")
    public String resumeQueue() {
        queueService.resumeQueueEntry();
        return "redirect:/user/status";
    }
}
