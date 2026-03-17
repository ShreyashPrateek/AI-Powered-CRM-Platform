package com.crm.deal.service;

import com.crm.deal.dto.DealDto;
import com.crm.deal.entity.Deal;
import com.crm.deal.enums.DealStage;
import com.crm.deal.event.DealEvent;
import com.crm.deal.event.DealEventPublisher;
import com.crm.deal.exception.InvalidStageTransitionException;
import com.crm.deal.exception.ResourceNotFoundException;
import com.crm.deal.repository.DealRepository;
import com.crm.deal.repository.DealSearchRepository;
import com.crm.deal.search.DealDocument;
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
public class DealService {

    /**
     * Allowed forward transitions. Terminal stages (CLOSED_WON / CLOSED_LOST) have no outgoing edges.
     * CLOSED_LOST is reachable from any non-terminal stage to model deals that fall through at any point.
     */
    private static final Map<DealStage, Set<DealStage>> ALLOWED_TRANSITIONS = Map.of(
        DealStage.LEAD,        Set.of(DealStage.QUALIFIED, DealStage.CLOSED_LOST),
        DealStage.QUALIFIED,   Set.of(DealStage.PROPOSAL,  DealStage.CLOSED_LOST),
        DealStage.PROPOSAL,    Set.of(DealStage.NEGOTIATION, DealStage.CLOSED_LOST),
        DealStage.NEGOTIATION, Set.of(DealStage.CLOSED_WON, DealStage.CLOSED_LOST),
        DealStage.CLOSED_WON,  Set.of(),
        DealStage.CLOSED_LOST, Set.of()
    );

    private final DealRepository        dealRepository;
    private final DealSearchRepository  dealSearchRepository;
    private final ElasticsearchOperations esOperations;
    private final DealEventPublisher    eventPublisher;

    // ── Queries ──────────────────────────────────────────────────────────────

    @Cacheable(value = "deals", key = "#id")
    public DealDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    public DealDto.PageResponse findAll(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        return toPageResponse(dealRepository.findAll(pageable));
    }

    public DealDto.PageResponse findByStage(DealStage stage, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toPageResponse(dealRepository.findByStage(stage, pageable));
    }

    public DealDto.PageResponse findByLead(Long leadId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toPageResponse(dealRepository.findByLeadId(leadId, pageable));
    }

    public DealDto.PageResponse findByOwner(Long ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return toPageResponse(dealRepository.findByOwnerId(ownerId, pageable));
    }

    public List<DealDto.Response> search(String query) {
        String esQuery = """
            {
              "multi_match": {
                "query": "%s",
                "fields": ["title"],
                "fuzziness": "AUTO"
              }
            }
            """.formatted(query);

        SearchHits<DealDocument> hits = esOperations.search(
            new StringQuery(esQuery), DealDocument.class
        );
        return hits.getSearchHits().stream()
            .map(hit -> toResponseFromDocument(hit.getContent()))
            .toList();
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    @Transactional
    public DealDto.Response create(DealDto.CreateRequest req) {
        Deal deal = Deal.builder()
            .leadId(req.leadId())
            .title(req.title())
            .value(req.value())
            .expectedCloseDate(req.expectedCloseDate())
            .ownerId(req.ownerId())
            .probability(req.probability() != null ? req.probability() : 10)
            .notes(req.notes())
            .build();

        Deal saved = dealRepository.save(deal);
        indexToElasticsearch(saved);
        eventPublisher.publish(buildEvent("DEAL_CREATED", saved));
        return toResponse(saved);
    }

    @Transactional
    @CachePut(value = "deals", key = "#id")
    public DealDto.Response update(Long id, DealDto.UpdateRequest req) {
        Deal deal = getOrThrow(id);
        if (req.title()             != null) deal.setTitle(req.title());
        if (req.value()             != null) deal.setValue(req.value());
        if (req.expectedCloseDate() != null) deal.setExpectedCloseDate(req.expectedCloseDate());
        if (req.probability()       != null) deal.setProbability(req.probability());
        if (req.notes()             != null) deal.setNotes(req.notes());

        Deal saved = dealRepository.save(deal);
        indexToElasticsearch(saved);
        eventPublisher.publish(buildEvent("DEAL_UPDATED", saved));
        return toResponse(saved);
    }

    @Transactional
    @CachePut(value = "deals", key = "#id")
    public DealDto.Response updateStage(Long id, DealDto.StageUpdateRequest req) {
        Deal deal = getOrThrow(id);
        DealStage current = deal.getStage();
        DealStage next    = req.stage();

        if (!ALLOWED_TRANSITIONS.get(current).contains(next)) {
            throw new InvalidStageTransitionException(
                "Cannot transition deal from %s to %s".formatted(current, next)
            );
        }
        deal.setStage(next);
        deal.setProbability(defaultProbability(next));

        Deal saved = dealRepository.save(deal);
        indexToElasticsearch(saved);
        eventPublisher.publish(buildEvent("DEAL_STAGE_CHANGED", saved));
        return toResponse(saved);
    }

    @Transactional
    @CachePut(value = "deals", key = "#id")
    public DealDto.Response assign(Long id, DealDto.AssignRequest req) {
        Deal deal = getOrThrow(id);
        deal.setOwnerId(req.ownerId());

        Deal saved = dealRepository.save(deal);
        indexToElasticsearch(saved);
        eventPublisher.publish(buildEvent("DEAL_ASSIGNED", saved));
        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "deals", key = "#id")
    public void delete(Long id) {
        Deal deal = getOrThrow(id);
        dealRepository.delete(deal);
        dealSearchRepository.deleteById(String.valueOf(id));
        eventPublisher.publish(buildEvent("DEAL_DELETED", deal));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Deal getOrThrow(Long id) {
        return dealRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Deal not found: " + id));
    }

    /** Default probability per stage — overridable by the user at any time. */
    private int defaultProbability(DealStage stage) {
        return switch (stage) {
            case LEAD        -> 10;
            case QUALIFIED   -> 25;
            case PROPOSAL    -> 50;
            case NEGOTIATION -> 75;
            case CLOSED_WON  -> 100;
            case CLOSED_LOST -> 0;
        };
    }

    private void indexToElasticsearch(Deal deal) {
        dealSearchRepository.save(DealDocument.builder()
            .id(String.valueOf(deal.getId()))
            .title(deal.getTitle())
            .leadId(deal.getLeadId())
            .value(deal.getValue())
            .stage(deal.getStage())
            .probability(deal.getProbability())
            .expectedCloseDate(deal.getExpectedCloseDate())
            .ownerId(deal.getOwnerId())
            .createdAt(deal.getCreatedAt())
            .build());
    }

    private DealEvent buildEvent(String type, Deal deal) {
        return DealEvent.builder()
            .eventType(type)
            .dealId(deal.getId())
            .leadId(deal.getLeadId())
            .stage(deal.getStage())
            .value(deal.getValue())
            .ownerId(deal.getOwnerId())
            .occurredAt(Instant.now())
            .build();
    }

    private DealDto.Response toResponse(Deal d) {
        return DealDto.Response.builder()
            .id(d.getId())
            .leadId(d.getLeadId())
            .title(d.getTitle())
            .value(d.getValue())
            .stage(d.getStage())
            .probability(d.getProbability())
            .expectedCloseDate(d.getExpectedCloseDate())
            .ownerId(d.getOwnerId())
            .notes(d.getNotes())
            .createdAt(d.getCreatedAt())
            .updatedAt(d.getUpdatedAt())
            .build();
    }

    private DealDto.Response toResponseFromDocument(DealDocument d) {
        return DealDto.Response.builder()
            .id(Long.parseLong(d.getId()))
            .leadId(d.getLeadId())
            .title(d.getTitle())
            .value(d.getValue())
            .stage(d.getStage())
            .probability(d.getProbability())
            .expectedCloseDate(d.getExpectedCloseDate())
            .ownerId(d.getOwnerId())
            .createdAt(d.getCreatedAt())
            .build();
    }

    private DealDto.PageResponse toPageResponse(Page<Deal> page) {
        return DealDto.PageResponse.builder()
            .content(page.getContent().stream().map(this::toResponse).toList())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }
}
