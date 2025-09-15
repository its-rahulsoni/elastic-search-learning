package com.spring.elasticsearch.learning.service;

import co.elastic.clients.json.JsonData;
import com.spring.elasticsearch.learning.models.OrderDocument;
import com.spring.elasticsearch.learning.repository.OrdersPaginationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregation;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;

import java.util.List;

@Service
public class OrdersPaginationService {

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private OrdersPaginationRepository repository;

    /**
     * This method saves the OrderDocument to the Elasticsearch index.
     * It uses the repository's save method which handles both insert and update operations.
     * If the document already exists (based on its ID), it will update the existing document.
     * If it does not exist, it will insert a new document.
     * @param order
     * @return
     */
    public OrderDocument addOrder(OrderDocument order) {
        return repository.save(order); // inserts into Elasticsearch index
    }


    /**
     * 1️⃣ Term Query (Exact Match)
     * Kibana DSL:
     * {
     *   "query": {
     *     "term": {
     *       "customer": "Rahul"
     *     }
     *   }
     * }
     *
     * ✅ Use Case: Exact matching (keywords, IDs, enums).
     * 🔑 Remember: .value(v -> v.stringValue(...)) is used for term queries.
     */
    public List<OrderDocument> getOrdersByCustomerUsingTermQuery(String customerName) {

        // ✅ Build a native term query ....
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t.field("customer").value(v -> v.stringValue(customerName))))
                .build();

        // ✅ Execute search ....
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        // ✅ Convert SearchHits -> List<OrderDocument> ....
        return searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();
    }


    /**
     * 2️⃣ Match Query (Full-text Search)
     * Kibana DSL:
     * {
     *   "query": {
     *     "match": {
     *       "status": "PAID"
     *     }
     *   }
     * }
     *
     * ✅ Use Case: Full-text search (fields analyzed by ES analyzer).
     * 🔑 Remember: .query(...) sets the text to match.
     */
    public List<OrderDocument> getOrdersByStatusUsingMatchQuery(String status) {

        // ✅ Build a native term query ....
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.match(m -> m.field("status").query(status)))
                .build();

        // ✅ Execute search ....
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        // ✅ Convert SearchHits -> List<OrderDocument> ....
        return searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();
    }

    /**
     * 3️⃣ Range Query (Numbers/Dates)
     * Kibana DSL:
     * {
     *   "query": {
     *     "range": {
     *       "total_amount": {
     *         "gte": 100,
     *         "lte": 600
     *       }
     *     }
     *   }
     * }
     *
     * ✅ Use Case: Filter documents by numeric range or date range.
     * 🔑 Remember: Use JsonData.of(...) for numbers/dates to avoid type issues.
     */
    public List<OrderDocument> getOrdersUsingRangeQuery() {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.range(r -> r
                        .field("total_amount")
                        .gte(JsonData.of(100))
                        .lte(JsonData.of(600))
                        ))
                        .build();

        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        return searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();
    }


    /**
     * 4️⃣ Bool Query (Combine Queries)
     * Kibana DSL:
     * {
     *   "query": {
     *     "bool": {
     *       "must": [
     *         { "term": { "status": "PAID" } },
     *         { "range": { "total_amount": { "gte": 300 } } }
     *       ]
     *     }
     *   }
     * }
     *
     * ✅ Use Case: Combine multiple conditions (must/should/must_not/filter).
     * 🔑 Remember: .must(), .should(), .mustNot() correspond to Kibana bool clauses.
     */
    public List<OrderDocument> getOrdersByCombiningQueries() {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("status").value(v -> v.stringValue("PAID"))))
                        .must(m -> m.range(r -> r.field("total_amount").gte(JsonData.of(300))))
                        ))
                        .build();

        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        return searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();

    }


    /**
     * 5️⃣ Sort + Pagination
     * Kibana DSL:
     * {
     *   "query": { "match_all": {} },
     *   "sort": [{ "total_amount": "desc" }],
     *   "size": 5,
     *   "from": 0
     * }
     *
     * ✅ Use Case: Get top-N results sorted by a field.
     * 🔑 Remember: Use PageRequest.of(page, size) for pagination.
     */
    public List<OrderDocument> getOrdersBySortAndPaginationQueries(String sortField, int page, int size, boolean desc) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .withSort(Sort.by(Sort.Order.desc(sortField)))
                .withPageable(PageRequest.of(page, size)) // page 0, size 5
                .build();

        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        return searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();
    }







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
