package com.crm.email.controller;

import com.crm.email.dto.CampaignDto;
import com.crm.email.service.CampaignService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @GetMapping
    public List<CampaignDto.Response> getAll() {
        return campaignService.findAll();
    }

    @GetMapping("/{id}")
    public CampaignDto.Response getById(@PathVariable Long id) {
        return campaignService.findById(id);
    }

    @GetMapping("/{id}/recipients")
    public List<CampaignDto.RecipientResponse> getRecipients(@PathVariable Long id) {
        return campaignService.findRecipients(id);
    }

    @PostMapping
    public ResponseEntity<CampaignDto.Response> create(@Valid @RequestBody CampaignDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.create(req));
    }

    @PatchMapping("/{id}")
    public CampaignDto.Response update(@PathVariable Long id, @Valid @RequestBody CampaignDto.UpdateRequest req) {
        return campaignService.update(id, req);
    }

    @PostMapping("/{id}/dispatch")
    public ResponseEntity<Void> dispatch(@PathVariable Long id) {
        campaignService.dispatch(id);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        campaignService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
