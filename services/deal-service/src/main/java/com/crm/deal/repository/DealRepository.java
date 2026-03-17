package com.crm.deal.repository;

import com.crm.deal.entity.Deal;
import com.crm.deal.enums.DealStage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DealRepository extends JpaRepository<Deal, Long> {
    Page<Deal> findByStage(DealStage stage, Pageable pageable);
    Page<Deal> findByLeadId(Long leadId, Pageable pageable);
    Page<Deal> findByOwnerId(Long ownerId, Pageable pageable);
    long countByStage(DealStage stage);
}
