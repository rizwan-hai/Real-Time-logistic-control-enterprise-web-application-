package com.logistics.controltower.exceptions;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExceptionHistoryRepository extends JpaRepository<ExceptionHistory, Long> {
    List<ExceptionHistory> findByDeliveryExceptionIdOrderByOccurredAtAsc(Long exceptionId);
}
