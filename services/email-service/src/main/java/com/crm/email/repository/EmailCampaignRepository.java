package com.crm.email.repository;

import com.crm.email.entity.EmailCampaign;
import com.crm.email.enums.CampaignStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {
    List<EmailCampaign> findByStatus(CampaignStatus status);
    List<EmailCampaign> findByStatusAndScheduledAtBefore(CampaignStatus status, Instant now);
    List<EmailCampaign> findByCreatedBy(Long createdBy);
}
