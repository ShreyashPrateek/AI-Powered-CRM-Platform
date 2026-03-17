package com.crm.deal.controller;

import com.crm.deal.dto.DealDto;
import com.crm.deal.enums.DealStage;
import com.crm.deal.service.DealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deals")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;

    @GetMapping
    public DealDto.PageResponse getAll(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(required = false) DealStage stage,
        @RequestParam(required = false) Long leadId,
        @RequestParam(required = false) Long ownerId
    ) {
        if (stage   != null) return dealService.findByStage(stage, page, size);
        if (leadId  != null) return dealService.findByLead(leadId, page, size);
        if (ownerId != null) return dealService.findByOwner(ownerId, page, size);
        return dealService.findAll(page, size, sortBy);
    }

    @GetMapping("/{id}")
    public DealDto.Response getById(@PathVariable Long id) {
        return dealService.findById(id);
    }

    @GetMapping("/search")
    public List<DealDto.Response> search(@RequestParam String q) {
        return dealService.search(q);
    }

    @PostMapping
    public ResponseEntity<DealDto.Response> create(@Valid @RequestBody DealDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.create(req));
    }

    @PatchMapping("/{id}")
    public DealDto.Response update(@PathVariable Long id, @Valid @RequestBody DealDto.UpdateRequest req) {
        return dealService.update(id, req);
    }

    @PatchMapping("/{id}/stage")
    public DealDto.Response updateStage(@PathVariable Long id, @Valid @RequestBody DealDto.StageUpdateRequest req) {
        return dealService.updateStage(id, req);
    }

    @PatchMapping("/{id}/assign")
    public DealDto.Response assign(@PathVariable Long id, @Valid @RequestBody DealDto.AssignRequest req) {
        return dealService.assign(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dealService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
