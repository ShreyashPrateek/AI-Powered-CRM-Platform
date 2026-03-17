package com.crm.email.service;

import com.crm.email.dto.CampaignDto;
import com.crm.email.entity.CampaignRecipient;
import com.crm.email.entity.EmailCampaign;
import com.crm.email.entity.EmailLog;
import com.crm.email.entity.EmailTemplate;
import com.crm.email.enums.CampaignStatus;
import com.crm.email.enums.EmailStatus;
import com.crm.email.exception.ResourceNotFoundException;
import com.crm.email.repository.CampaignRecipientRepository;
import com.crm.email.repository.EmailCampaignRepository;
import com.crm.email.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CampaignService {

    private final EmailCampaignRepository    campaignRepository;
    private final CampaignRecipientRepository recipientRepository;
    private final EmailLogRepository         logRepository;
    private final EmailTemplateService       templateService;
    private final SmtpEmailSender           smtpSender;

    public List<CampaignDto.Response> findAll() {
        return campaignRepository.findAll().stream().map(this::toResponse).toList();
    }

    public CampaignDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public List<CampaignDto.RecipientResponse> findRecipients(Long campaignId) {
        return recipientRepository.findByCampaignId(campaignId).stream()
            .map(this::toRecipientResponse).toList();
    }

    @Transactional
    public CampaignDto.Response create(CampaignDto.CreateRequest req) {
        EmailTemplate template = templateService.getOrThrow(req.templateId());

        EmailCampaign campaign = EmailCampaign.builder()
            .name(req.name())
            .template(template)
            .createdBy(req.createdBy())
            .scheduledAt(req.scheduledAt())
            .status(req.scheduledAt() != null ? CampaignStatus.SCHEDULED : CampaignStatus.DRAFT)
            .build();

        req.recipients().forEach(r -> campaign.getRecipients().add(
            CampaignRecipient.builder()
                .campaign(campaign)
                .recipientEmail(r.email())
                .recipientName(r.name())
                .leadId(r.leadId())
                .build()
        ));

        return toResponse(campaignRepository.save(campaign));
    }

    @Transactional
    public CampaignDto.Response update(Long id, CampaignDto.UpdateRequest req) {
        EmailCampaign campaign = getOrThrow(id);
        if (req.name()        != null) campaign.setName(req.name());
        if (req.scheduledAt() != null) campaign.setScheduledAt(req.scheduledAt());
        if (req.templateId()  != null) campaign.setTemplate(templateService.getOrThrow(req.templateId()));
        return toResponse(campaignRepository.save(campaign));
    }

    /**
     * Dispatches a campaign immediately — sends to all PENDING recipients.
     * Called by the scheduler for SCHEDULED campaigns or directly via API.
     */
    @Transactional
    public void dispatch(Long campaignId) {
        EmailCampaign campaign = getOrThrow(campaignId);
        if (campaign.getStatus() == CampaignStatus.SENT || campaign.getStatus() == CampaignStatus.CANCELLED) {
            log.warn("Campaign {} already in terminal state {}", campaignId, campaign.getStatus());
            return;
        }

        campaign.setStatus(CampaignStatus.SENDING);
        campaignRepository.save(campaign);

        EmailTemplate template = campaign.getTemplate();
        List<CampaignRecipient> pending = recipientRepository
            .findByCampaignIdAndStatus(campaignId, EmailStatus.PENDING);

        for (CampaignRecipient recipient : pending) {
            sendToRecipient(campaign, template, recipient);
        }

        campaign.setStatus(CampaignStatus.SENT);
        campaign.setSentAt(Instant.now());
        campaignRepository.save(campaign);
        log.info("Campaign {} dispatched to {} recipients", campaignId, pending.size());
    }

    @Transactional
    public void cancel(Long id) {
        EmailCampaign campaign = getOrThrow(id);
        campaign.setStatus(CampaignStatus.CANCELLED);
        campaignRepository.save(campaign);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void sendToRecipient(EmailCampaign campaign, EmailTemplate template, CampaignRecipient recipient) {
        EmailStatus result = EmailStatus.SENT;
        String failureReason = null;
        try {
            smtpSender.send(
                recipient.getRecipientEmail(),
                template.getSubject(),
                template.getTemplateKey(),
                Map.of(
                    "campaignName",  campaign.getName(),
                    "recipientName", recipient.getRecipientName() != null ? recipient.getRecipientName() : "",
                    "subject",       template.getSubject(),
                    "body",          ""
                )
            );
            recipient.setSentAt(Instant.now());
        } catch (Exception ex) {
            result = EmailStatus.FAILED;
            failureReason = ex.getMessage();
            log.error("Failed to send campaign email to {}: {}", recipient.getRecipientEmail(), ex.getMessage());
        }

        recipient.setStatus(result);
        recipient.setFailureReason(failureReason);
        recipientRepository.save(recipient);

        logRepository.save(EmailLog.builder()
            .recipientEmail(recipient.getRecipientEmail())
            .subject(template.getSubject())
            .emailType("CAMPAIGN")
            .referenceId(campaign.getId())
            .status(result)
            .failureReason(failureReason)
            .sentAt(result == EmailStatus.SENT ? Instant.now() : null)
            .build());
    }

    private EmailCampaign getOrThrow(Long id) {
        return campaignRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + id));
    }

    private CampaignDto.Response toResponse(EmailCampaign c) {
        return CampaignDto.Response.builder()
            .id(c.getId())
            .name(c.getName())
            .templateId(c.getTemplate().getId())
            .templateName(c.getTemplate().getName())
            .status(c.getStatus())
            .scheduledAt(c.getScheduledAt())
            .sentAt(c.getSentAt())
            .createdBy(c.getCreatedBy())
            .totalRecipients(c.getRecipients().size())
            .sentCount(recipientRepository.countByCampaignIdAndStatus(c.getId(), EmailStatus.SENT))
            .failedCount(recipientRepository.countByCampaignIdAndStatus(c.getId(), EmailStatus.FAILED))
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }

    private CampaignDto.RecipientResponse toRecipientResponse(CampaignRecipient r) {
        return CampaignDto.RecipientResponse.builder()
            .id(r.getId())
            .recipientEmail(r.getRecipientEmail())
            .recipientName(r.getRecipientName())
            .leadId(r.getLeadId())
            .status(r.getStatus())
            .failureReason(r.getFailureReason())
            .sentAt(r.getSentAt())
            .build();
    }
}
