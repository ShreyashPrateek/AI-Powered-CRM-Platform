package com.crm.email.controller;

import com.crm.email.dto.ReminderDto;
import com.crm.email.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/email/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping
    public List<ReminderDto.Response> getAll(
        @RequestParam(required = false) Long leadId,
        @RequestParam(required = false) Long assignedUserId
    ) {
        if (leadId         != null) return reminderService.findByLead(leadId);
        if (assignedUserId != null) return reminderService.findByAssignedUser(assignedUserId);
        return List.of();
    }

    @GetMapping("/{id}")
    public ReminderDto.Response getById(@PathVariable Long id) {
        return reminderService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ReminderDto.Response> create(@Valid @RequestBody ReminderDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reminderService.create(req));
    }

    @PatchMapping("/{id}")
    public ReminderDto.Response update(@PathVariable Long id, @Valid @RequestBody ReminderDto.UpdateRequest req) {
        return reminderService.update(id, req);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        reminderService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
