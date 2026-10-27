package com.logistics.controltower.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    Page<NotificationEntity> findByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(
            String recipientEmail, Pageable pageable);
}
