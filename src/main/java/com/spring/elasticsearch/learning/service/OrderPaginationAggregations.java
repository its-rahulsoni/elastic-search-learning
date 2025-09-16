package com.spring.elasticsearch.learning.service;

import co.elastic.clients.elasticsearch._types.aggregations.ValueCountAggregation;
import com.spring.elasticsearch.learning.models.OrderDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;

@Service
public class OrderPaginationAggregations {

    @Autowired
    private ElasticsearchOperations operations;

    /**
     * 1️⃣ Aggregation Query (Total count of orders using order_id)
     *
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "aggs": {
     *     "total_orders": {
     *       "value_count": {
     *         "field": "order_id"
     *       }
     *     }
     *   }
     * }
     */
    public long getTotalOrdersCount() {
        // ✅ Build Value Count Aggregation in the same style as revenuePerCustomerAgg
        Aggregation totalOrdersAgg = Aggregation.of(a -> a
                .valueCount(ValueCountAggregation.of(vc -> vc
                        .field("order_id")
                ))
        );

        // ✅ Build NativeQuery with the aggregation
        NativeQuery query = NativeQuery.builder()
                .withAggregation("total_orders", totalOrdersAgg)
                .build();

        // ✅ Run the query - we don't care about hits, only aggregation result
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null) return 0L;

        ElasticsearchAggregation totalOrdersAggWrapper = springAggs.aggregationsAsMap().get("total_orders");
        if (totalOrdersAggWrapper == null) return 0L;

        Aggregate totalOrdersAggObject = totalOrdersAggWrapper.aggregation().getAggregate();
        if (!totalOrdersAggObject.isValueCount()) return 0L;

        return (long) totalOrdersAggObject.valueCount().value();

// ----------------------------------------------------
// Another way of doing the same thing .....
        /*
        List<OrderDocument> orderDocumentList = searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();

        long count = orderDocumentList.size();
        System.out.println("Total Orders Count: " + count);

        return count;*/
    }

}
