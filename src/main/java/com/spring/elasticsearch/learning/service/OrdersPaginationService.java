package com.spring.elasticsearch.learning.service;

import co.elastic.clients.json.JsonData;
import com.spring.elasticsearch.learning.models.OrderDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregation;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;

@Service
public class OrdersPaginationService {

    @Autowired
    private ElasticsearchOperations operations;

    /**
     * ✅ Get top N orders sorted by totalAmount descending.
     */
    // Top N orders sorted by totalAmount desc (ELC lambda query)
    public SearchHits<OrderDocument> getTopOrders(int size) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .withSort(Sort.by(Sort.Order.desc("totalAmount")))
                .withPageable(PageRequest.of(0, size))
                .build();

        return operations.search(query, OrderDocument.class);
    }

    /**
     * ✅ Aggregation: Total revenue per customer
     */
    public SearchHits<OrderDocument> getRevenuePerCustomer() {

        // ✅ Build Terms Aggregation
        Aggregation revenuePerCustomerAgg = Aggregation.of(a -> a
                .terms(TermsAggregation.of(t -> t
                        .field("customer.keyword")
                ))
                .aggregations("total_spent", Aggregation.of(subAgg -> subAgg
                        .sum(SumAggregation.of(s -> s
                                .field("totalAmount")
                        ))
                ))
        );

        NativeQuery query = NativeQuery.builder()
                .withAggregation("revenue_per_customer", revenuePerCustomerAgg)
                .build();

        return operations.search(query, OrderDocument.class);
    }


    /**
     * ✅ Filter + Aggregation: High value orders only
     */
    public SearchHits<OrderDocument> getHighValueOrdersPerCustomer(double minAmount) {

        // ✅ Step 1: Build Range Query
        Query rangeQuery = Query.of(q -> q
                .range(RangeQuery.of(r -> r
                        .field("totalAmount")
                        .gte(JsonData.of(minAmount))  // ✅ use JsonData for values
                ))
        );

        // ✅ Step 2: Build Terms + Sum Aggregation
        Aggregation revenuePerCustomerAgg = Aggregation.of(a -> a
                .terms(TermsAggregation.of(t -> t
                        .field("customer.keyword")
                ))
                .aggregations("total_spent", Aggregation.of(subAgg -> subAgg
                        .sum(SumAggregation.of(s -> s
                                .field("totalAmount")
                        ))
                ))
        );

        // ✅ Step 3: Build Query with both filter + aggregation
        NativeQuery query = NativeQuery.builder()
                .withQuery(rangeQuery)                          // ✅ ELC Query object
                .withAggregation("revenue_per_customer", revenuePerCustomerAgg)
                .build();

        return operations.search(query, OrderDocument.class);
    }
}
