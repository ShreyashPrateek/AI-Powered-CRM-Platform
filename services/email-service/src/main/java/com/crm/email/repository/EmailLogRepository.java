package com.crm.email.repository;

import com.crm.email.entity.EmailLog;
import com.crm.email.enums.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    Page<EmailLog> findByRecipientEmail(String recipientEmail, Pageable pageable);
    Page<EmailLog> findByStatus(EmailStatus status, Pageable pageable);
    long countByStatus(EmailStatus status);
}
