package com.spring.elasticsearch.learning.repository;

import com.spring.elasticsearch.learning.models.OrderDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface OrdersPaginationRepository extends ElasticsearchRepository<OrderDocument, String> {

    List<OrderDocument> findByCustomer(String customer);

    List<OrderDocument> findByTotalAmountGreaterThan(Double amount);
}
