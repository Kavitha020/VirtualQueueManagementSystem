package com.vqms.repository;


import com.vqms.model.Counter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CounterRepository extends JpaRepository<Counter, Long> {
    List<Counter> findByCounterStatus(String status);
}