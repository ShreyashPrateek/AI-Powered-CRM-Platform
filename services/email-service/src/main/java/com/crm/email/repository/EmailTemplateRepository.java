package com.crm.email.repository;

import com.crm.email.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByName(String name);
    List<EmailTemplate> findByActiveTrue();
    boolean existsByName(String name);
}
