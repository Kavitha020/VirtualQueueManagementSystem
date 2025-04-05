package com.vqms.controller;


import com.vqms.model.Counter;
import com.vqms.repository.CounterRepository;
import com.vqms.repository.QueueEntryRepository;
import com.vqms.service.QueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private QueueService queueService;

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private QueueEntryRepository queueEntryRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("queueEntries", queueEntryRepository.findAll());
        model.addAttribute("counters", counterRepository.findAll());
        model.addAttribute("queueLimitExceeded", queueEntryRepository.count() > 20);
        return "admin/dashboard";
    }

    @GetMapping("/counters")
    public String manageCounters(Model model) {
        model.addAttribute("counters", counterRepository.findAll());
        return "admin/manage-counters";
    }

    @PostMapping("/counters/add")
    public String addCounter(@RequestParam String counterName, @RequestParam String counterStatus) {
        Counter counter = new Counter();
        counter.setCounterName(counterName);
        counter.setCounterStatus(counterStatus);
        counter.setLastUpdated(java.time.LocalDateTime.now());
        counterRepository.save(counter);
        return "redirect:/admin/counters";
    }

    @PostMapping("/counter/pause/{id}")
    public String pauseCounter(@PathVariable Long id) {
        queueService.pauseCounter(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/counter/resume/{id}")
    public String resumeCounter(@PathVariable Long id) {
        queueService.resumeCounter(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/counter/process/{id}")
    public String processNext(@PathVariable Long id) {
        queueService.processNext(id);
        return "redirect:/admin/dashboard";
    }
}