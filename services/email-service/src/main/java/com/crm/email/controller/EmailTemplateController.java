package com.crm.email.controller;

import com.crm.email.dto.EmailTemplateDto;
import com.crm.email.service.EmailTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email/templates")
@RequiredArgsConstructor
public class EmailTemplateController {

    private final EmailTemplateService templateService;

    @GetMapping
    public List<EmailTemplateDto.Response> getAll(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return activeOnly ? templateService.findActive() : templateService.findAll();
    }

    @GetMapping("/{id}")
    public EmailTemplateDto.Response getById(@PathVariable Long id) {
        return templateService.findById(id);
    }

    @PostMapping
    public ResponseEntity<EmailTemplateDto.Response> create(@Valid @RequestBody EmailTemplateDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.create(req));
    }

    @PatchMapping("/{id}")
    public EmailTemplateDto.Response update(@PathVariable Long id, @Valid @RequestBody EmailTemplateDto.UpdateRequest req) {
        return templateService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
