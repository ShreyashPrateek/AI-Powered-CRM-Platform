package com.crm.lead.service;

import com.crm.lead.dto.LeadDto;
import com.crm.lead.entity.Lead;
import com.crm.lead.enums.LeadStatus;
import com.crm.lead.event.LeadEvent;
import com.crm.lead.event.LeadEventPublisher;
import com.crm.lead.exception.DuplicateResourceException;
import com.crm.lead.exception.InvalidStatusTransitionException;
import com.crm.lead.exception.ResourceNotFoundException;
import com.crm.lead.repository.LeadRepository;
import com.crm.lead.repository.LeadSearchRepository;
import com.crm.lead.search.LeadDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeadService {

    // Valid forward-only status transitions
    private static final Map<LeadStatus, Set<LeadStatus>> ALLOWED_TRANSITIONS = Map.of(
        LeadStatus.NEW,       Set.of(LeadStatus.CONTACTED, LeadStatus.LOST),
        LeadStatus.CONTACTED, Set.of(LeadStatus.QUALIFIED, LeadStatus.LOST),
        LeadStatus.QUALIFIED, Set.of(LeadStatus.LOST),
        LeadStatus.LOST,      Set.of()
    );

    private final LeadRepository        leadRepository;
    private final LeadSearchRepository  leadSearchRepository;
    private final ElasticsearchOperations esOperations;
    private final LeadEventPublisher    eventPublisher;

    // ── Queries ──────────────────────────────────────────────────────────────

    @Cacheable(value = "leads", key = "#id")
    public LeadDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public LeadDto.PageResponse findAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return toPageResponse(leadRepository.findAll(pageable));
    }

    public LeadDto.PageResponse findByStatus(LeadStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toPageResponse(leadRepository.findByStatus(status, pageable));
    }

    public LeadDto.PageResponse findByAssignedUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toPageResponse(leadRepository.findByAssignedUserId(userId, pageable));
    }

    public List<LeadDto.Response> search(String query) {
        String esQuery = """
            {
              "multi_match": {
                "query": "%s",
                "fields": ["name", "email", "company"],
                "fuzziness": "AUTO"
              }
            }
            """.formatted(query);

        SearchHits<LeadDocument> hits = esOperations.search(
            new StringQuery(esQuery), LeadDocument.class
        );
        return hits.getSearchHits().stream()
            .map(hit -> toResponseFromDocument(hit.getContent()))
            .toList();
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    @Transactional
    public LeadDto.Response create(LeadDto.CreateRequest req) {
        if (leadRepository.existsByEmail(req.email())) {
            throw new DuplicateResourceException("Lead already exists with email: " + req.email());
        }
        Lead lead = Lead.builder()
            .name(req.name())
            .email(req.email())
            .phone(req.phone())
            .company(req.company())
            .industry(req.industry())
            .source(req.source())
            .assignedUserId(req.assignedUserId())
            .notes(req.notes())
            .build();

        Lead saved = leadRepository.save(lead);
        indexToElasticsearch(saved);
        eventPublisher.publish(buildEvent("LEAD_CREATED", saved));
        return toResponse(saved);
    }

    @Transactional
    @CachePut(value = "leads", key = "#id")
    public LeadDto.Response update(Long id, LeadDto.UpdateRequest req) {
        Lead lead = getOrThrow(id);
        if (req.name()     != null) lead.setName(req.name());
        if (req.phone()    != null) lead.setPhone(req.phone());
        if (req.company()  != null) lead.setCompany(req.company());
        if (req.industry() != null) lead.setIndustry(req.industry());
        if (req.source()   != null) lead.setSource(req.source());
        if (req.notes()    != null) lead.setNotes(req.notes());

        Lead saved = leadRepository.save(lead);
        indexToElasticsearch(saved);
        eventPublisher.publish(buildEvent("LEAD_UPDATED", saved));
        return toResponse(saved);
    }

    @Transactional
    @CachePut(value = "leads", key = "#id")
    public LeadDto.Response assign(Long id, LeadDto.AssignRequest req) {
        Lead lead = getOrThrow(id);
        lead.setAssignedUserId(req.assignedUserId());

        Lead saved = leadRepository.save(lead);
        indexToElasticsearch(saved);
        eventPublisher.publish(buildEvent("LEAD_ASSIGNED", saved));
        return toResponse(saved);
    }

    @Transactional
    @CachePut(value = "leads", key = "#id")
    public LeadDto.Response updateStatus(Long id, LeadDto.StatusUpdateRequest req) {
        Lead lead = getOrThrow(id);
        LeadStatus current = lead.getStatus();
        LeadStatus next    = req.status();

        if (!ALLOWED_TRANSITIONS.get(current).contains(next)) {
            throw new InvalidStatusTransitionException(
                "Cannot transition lead from %s to %s".formatted(current, next)
            );
        }
        lead.setStatus(next);

        Lead saved = leadRepository.save(lead);
        indexToElasticsearch(saved);
        eventPublisher.publish(buildEvent("LEAD_STATUS_CHANGED", saved));
        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "leads", key = "#id")
    public void delete(Long id) {
        Lead lead = getOrThrow(id);
        leadRepository.delete(lead);
        leadSearchRepository.deleteById(String.valueOf(id));
        eventPublisher.publish(buildEvent("LEAD_DELETED", lead));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Lead getOrThrow(Long id) {
        return leadRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Lead not found: " + id));
    }

    private void indexToElasticsearch(Lead lead) {
        leadSearchRepository.save(LeadDocument.builder()
            .id(String.valueOf(lead.getId()))
            .name(lead.getName())
            .email(lead.getEmail())
            .phone(lead.getPhone())
            .company(lead.getCompany())
            .industry(lead.getIndustry())
            .source(lead.getSource())
            .status(lead.getStatus())
            .assignedUserId(lead.getAssignedUserId())
            .createdAt(lead.getCreatedAt())
            .build());
    }

    private LeadEvent buildEvent(String type, Lead lead) {
        return LeadEvent.builder()
            .eventType(type)
            .leadId(lead.getId())
            .leadEmail(lead.getEmail())
            .status(lead.getStatus())
            .assignedUserId(lead.getAssignedUserId())
            .occurredAt(Instant.now())
            .build();
    }

    private LeadDto.Response toResponse(Lead l) {
        return LeadDto.Response.builder()
            .id(l.getId())
            .name(l.getName())
            .email(l.getEmail())
            .phone(l.getPhone())
            .company(l.getCompany())
            .industry(l.getIndustry())
            .source(l.getSource())
            .status(l.getStatus())
            .assignedUserId(l.getAssignedUserId())
            .notes(l.getNotes())
            .createdAt(l.getCreatedAt())
            .updatedAt(l.getUpdatedAt())
            .build();
    }

    private LeadDto.Response toResponseFromDocument(LeadDocument d) {
        return LeadDto.Response.builder()
            .id(Long.parseLong(d.getId()))
            .name(d.getName())
            .email(d.getEmail())
            .phone(d.getPhone())
            .company(d.getCompany())
            .industry(d.getIndustry())
            .source(d.getSource())
            .status(d.getStatus())
            .assignedUserId(d.getAssignedUserId())
            .createdAt(d.getCreatedAt())
            .build();
    }

    private LeadDto.PageResponse toPageResponse(Page<Lead> page) {
        return LeadDto.PageResponse.builder()
            .content(page.getContent().stream().map(this::toResponse).toList())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }
}
