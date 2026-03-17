package com.crm.email.controller;

import com.crm.email.dto.AiReplyDto;
import com.crm.email.service.AiReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/email/ai-reply")
@RequiredArgsConstructor
public class AiReplyController {

    private final AiReplyService aiReplyService;

    @PostMapping
    public AiReplyDto.Response generateAndSend(@Valid @RequestBody AiReplyDto.Request req) {
        return aiReplyService.generateAndSend(req);
    }
}
