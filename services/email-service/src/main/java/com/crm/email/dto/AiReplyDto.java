package com.crm.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class AiReplyDto {

    public record Request(
        @NotBlank @Email String recipientEmail,
        String recipientName,
        @NotBlank String context,   // e.g. lead notes, last interaction summary
        String tone                 // e.g. "professional", "friendly" — defaults to "professional"
    ) {}

    @Builder
    public record Response(
        String recipientEmail,
        String subject,
        String body,
        boolean sent
    ) {}
}
