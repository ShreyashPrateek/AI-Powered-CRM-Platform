package com.crm.email.service;

import com.crm.email.dto.EmailTemplateDto;
import com.crm.email.entity.EmailTemplate;
import com.crm.email.exception.DuplicateResourceException;
import com.crm.email.exception.ResourceNotFoundException;
import com.crm.email.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailTemplateService {

    private final EmailTemplateRepository templateRepository;

    public List<EmailTemplateDto.Response> findAll() {
        return templateRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<EmailTemplateDto.Response> findActive() {
        return templateRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    public EmailTemplateDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public EmailTemplateDto.Response create(EmailTemplateDto.CreateRequest req) {
        if (templateRepository.existsByName(req.name())) {
            throw new DuplicateResourceException("Template already exists: " + req.name());
        }
        EmailTemplate template = EmailTemplate.builder()
            .name(req.name())
            .subject(req.subject())
            .templateKey(req.templateKey())
            .description(req.description())
            .build();
        return toResponse(templateRepository.save(template));
    }

    @Transactional
    public EmailTemplateDto.Response update(Long id, EmailTemplateDto.UpdateRequest req) {
        EmailTemplate template = getOrThrow(id);
        if (req.subject()     != null) template.setSubject(req.subject());
        if (req.description() != null) template.setDescription(req.description());
        if (req.active()      != null) template.setActive(req.active());
        return toResponse(templateRepository.save(template));
    }

    @Transactional
    public void delete(Long id) {
        templateRepository.delete(getOrThrow(id));
    }

    EmailTemplate getOrThrow(Long id) {
        return templateRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate not found: " + id));
    }

    private EmailTemplateDto.Response toResponse(EmailTemplate t) {
        return EmailTemplateDto.Response.builder()
            .id(t.getId())
            .name(t.getName())
            .subject(t.getSubject())
            .templateKey(t.getTemplateKey())
            .description(t.getDescription())
            .active(t.isActive())
            .createdAt(t.getCreatedAt())
            .updatedAt(t.getUpdatedAt())
            .build();
    }
}
