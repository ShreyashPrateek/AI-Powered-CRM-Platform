package com.crm.deal.repository;

import com.crm.deal.search.DealDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface DealSearchRepository extends ElasticsearchRepository<DealDocument, String> {
}
