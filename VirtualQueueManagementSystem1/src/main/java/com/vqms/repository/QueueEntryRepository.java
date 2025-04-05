package com.vqms.repository;


import com.vqms.model.QueueEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, Long> {
    QueueEntry findByUserId(Long userId);
    List<QueueEntry> findByCounterId(Long counterId);
    List<QueueEntry> findByCounterIdAndPausedFalse(Long counterId);
}
