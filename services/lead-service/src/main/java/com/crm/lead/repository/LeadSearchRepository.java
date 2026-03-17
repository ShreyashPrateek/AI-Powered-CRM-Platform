package com.crm.lead.repository;

import com.crm.lead.search.LeadDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LeadSearchRepository extends ElasticsearchRepository<LeadDocument, String> {
}
