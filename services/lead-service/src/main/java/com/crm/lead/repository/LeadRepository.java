package com.crm.lead.repository;

import com.crm.lead.entity.Lead;
import com.crm.lead.enums.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    Optional<Lead> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);
    Page<Lead> findByAssignedUserId(Long assignedUserId, Pageable pageable);
    Page<Lead> findByStatusAndAssignedUserId(LeadStatus status, Long assignedUserId, Pageable pageable);
    long countByStatus(LeadStatus status);
    long countByAssignedUserId(Long assignedUserId);
}
