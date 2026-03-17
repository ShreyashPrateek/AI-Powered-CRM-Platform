package com.crm.email.repository;

import com.crm.email.entity.CampaignRecipient;
import com.crm.email.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaignRecipientRepository extends JpaRepository<CampaignRecipient, Long> {
    List<CampaignRecipient> findByCampaignId(Long campaignId);
    List<CampaignRecipient> findByCampaignIdAndStatus(Long campaignId, EmailStatus status);
    long countByCampaignIdAndStatus(Long campaignId, EmailStatus status);
}
