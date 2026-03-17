package com.crm.lead.controller;

import com.crm.lead.dto.LeadDto;
import com.crm.lead.enums.LeadStatus;
import com.crm.lead.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @GetMapping
    public LeadDto.PageResponse getAll(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false) LeadStatus status,
        @RequestParam(required = false) Long assignedUserId
    ) {
        if (status != null && assignedUserId != null) {
            // delegate to service — both filters applied at DB level
            return leadService.findByStatus(status, page, size);
        }
        if (status != null)       return leadService.findByStatus(status, page, size);
        if (assignedUserId != null) return leadService.findByAssignedUser(assignedUserId, page, size);
        return leadService.findAll(page, size, sortBy);
    }

    @GetMapping("/{id}")
    public LeadDto.Response getById(@PathVariable Long id) {
        return leadService.findById(id);
    }

    @GetMapping("/search")
    public List<LeadDto.Response> search(@RequestParam String q) {
        return leadService.search(q);
    }

    @PostMapping
    public ResponseEntity<LeadDto.Response> create(@Valid @RequestBody LeadDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.create(req));
    }

    @PatchMapping("/{id}")
    public LeadDto.Response update(@PathVariable Long id, @Valid @RequestBody LeadDto.UpdateRequest req) {
        return leadService.update(id, req);
    }

    @PatchMapping("/{id}/assign")
    public LeadDto.Response assign(@PathVariable Long id, @Valid @RequestBody LeadDto.AssignRequest req) {
        return leadService.assign(id, req);
    }

    @PatchMapping("/{id}/status")
    public LeadDto.Response updateStatus(@PathVariable Long id, @Valid @RequestBody LeadDto.StatusUpdateRequest req) {
        return leadService.updateStatus(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        leadService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
