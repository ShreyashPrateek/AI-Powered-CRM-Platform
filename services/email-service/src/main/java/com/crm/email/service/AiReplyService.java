package com.crm.email.service;

import com.crm.email.dto.AiReplyDto;
import com.crm.email.entity.EmailLog;
import com.crm.email.enums.EmailStatus;
import com.crm.email.repository.EmailLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiReplyService {

    private final SmtpEmailSender   smtpSender;
    private final EmailLogRepository logRepository;

    @Value("${ai.service.url:http://ai-service:8088}")
    private String aiServiceUrl;

    /**
     * Calls the AI service to generate a reply, then sends it via SMTP.
     * Falls back to a default template body if the AI service is unavailable.
     */
    public AiReplyDto.Response generateAndSend(AiReplyDto.Request req) {
        String tone    = req.tone() != null ? req.tone() : "professional";
        String subject = "Following up on your enquiry";
        String body;

        try {
            body = callAiService(req.context(), tone, req.recipientName());
        } catch (Exception ex) {
            log.warn("AI service unavailable, using fallback body: {}", ex.getMessage());
            body = buildFallbackBody(req.recipientName(), req.context());
        }

        boolean sent = false;
        EmailStatus status = EmailStatus.SENT;
        String failureReason = null;

        try {
            smtpSender.send(
                req.recipientEmail(),
                subject,
                "ai-reply",
                Map.of(
                    "recipientName", req.recipientName() != null ? req.recipientName() : "",
                    "subject",       subject,
                    "body",          body
                )
            );
            sent = true;
        } catch (Exception ex) {
            status = EmailStatus.FAILED;
            failureReason = ex.getMessage();
            log.error("Failed to send AI reply to {}: {}", req.recipientEmail(), ex.getMessage());
        }

        logRepository.save(EmailLog.builder()
            .recipientEmail(req.recipientEmail())
            .subject(subject)
            .emailType("AI_REPLY")
            .status(status)
            .failureReason(failureReason)
            .sentAt(sent ? Instant.now() : null)
            .build());

        return AiReplyDto.Response.builder()
            .recipientEmail(req.recipientEmail())
            .subject(subject)
            .body(body)
            .sent(sent)
            .build();
    }

    @SuppressWarnings("unchecked")
    private String callAiService(String context, String tone, String recipientName) {
        var response = RestClient.create(aiServiceUrl)
            .post()
            .uri("/api/v1/ai/generate-email")
            .body(Map.of(
                "context",       context,
                "tone",          tone,
                "recipientName", recipientName != null ? recipientName : ""
            ))
            .retrieve()
            .body(Map.class);

        if (response != null && response.containsKey("body")) {
            return (String) response.get("body");
        }
        throw new IllegalStateException("AI service returned empty body");
    }

    private String buildFallbackBody(String recipientName, String context) {
        String name = recipientName != null ? recipientName : "there";
        return """
            <p>Hi %s,</p>
            <p>Thank you for your interest. Based on our recent interaction, I wanted to follow up and ensure we address your needs effectively.</p>
            <p>%s</p>
            <p>Please don't hesitate to reach out if you have any questions.</p>
            """.formatted(name, context);
    }
}
