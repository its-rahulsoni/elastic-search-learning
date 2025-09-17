package com.spring.elasticsearch.learning.service;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import com.spring.elasticsearch.learning.models.CustomerRevenueResponse;
import com.spring.elasticsearch.learning.models.MinMax;
import com.spring.elasticsearch.learning.models.OrderDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderPaginationAggregations {

    @Autowired
    private ElasticsearchOperations operations;

    /**
     * Key Points: While checking Aggregate Object for required aggregation value.
     *
     * isValueCount() → used for value_count aggregations (counts the number of values in a field).
     * isSum() → used for sum aggregations (sums up numeric field values).
     * isAvg() → for avg aggregation
     * isMax() → for max aggregation
     * isMin() → for min aggregation
     */

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
        // ✅ Build Value Count Aggregation ....
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
        // isValueCount() is only true if the aggregation type is a value_count aggregation, not a sum ....
        if (!totalOrdersAggObject.isValueCount()){
            return 0L;
        }

        return (long) totalOrdersAggObject.valueCount().value();

// ----------------------------------------------------
// Another way of doing the same thing .....
// But its not suggested bcoz we're not using hte power of Aggregation in this approach.
        /*
        List<OrderDocument> orderDocumentList = searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();

        long count = orderDocumentList.size();
        System.out.println("Total Orders Count: " + count);

        return count;*/
    }


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "aggs": {
     *     "total_revenue": {
     *       "sum": {
     *         "field": "total_amount"
     *       }
     *     }
     *   }
     * }
     */
    public double getTotalRevenueFromOrders() {

        // ✅ Build Revenue Count Aggregation ....
        Aggregation revenueAggs = Aggregation.of(a -> a
                .sum(s -> s
                        .field("total_amount")
                )
        );

        // ✅ Build NativeQuery with the aggregation
        NativeQuery query = NativeQuery.builder()
                .withAggregation("total_revenue", revenueAggs)
                .build();

        // ✅ Run the query - we don't care about hits, only aggregation result
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null) return 0L;

        ElasticsearchAggregation totalOrdersAggWrapper = springAggs.aggregationsAsMap().get("total_revenue");
        if (totalOrdersAggWrapper == null) return 0L;

        Aggregate totalOrdersAggObject = totalOrdersAggWrapper.aggregation().getAggregate();

        // ✅ Check if it's a sum aggregation ....
        if (!totalOrdersAggObject.isSum()){
            return 0L;
        }

        // ✅ Extract sum value ....
        return totalOrdersAggObject.sum().value();

    }


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "aggs": {
     *     "avg_order_value": {
     *       "avg": {
     *         "field": "total_amount"
     *       }
     *     }
     *   }
     * }
     */
    public double getTotalAverageFromOrders() {
        // ✅ Build Average Aggregation ....
        Aggregation avgOrderAggs = Aggregation.of(a -> a
                .avg(s -> s.field("total_amount"))
        );

        // ✅ Build NativeQuery with the aggregation
        NativeQuery query = NativeQuery.builder()
                .withAggregation("avg_order_value", avgOrderAggs)
                .build();

        // ✅ Run the query - we don't care about hits, only aggregation result
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null) return 0L;

        ElasticsearchAggregation avgAggWrapper = springAggs.aggregationsAsMap().get("avg_order_value");
        if (avgAggWrapper == null) return 0L;

        Aggregate avgAggObject = avgAggWrapper.aggregation().getAggregate();

        // ✅ Check if it's a sum aggregation ....
        if (!avgAggObject.isAvg()){
            return 0L;
        }

        // ✅ Extract sum value ....
        return avgAggObject.avg().value();
    }


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "aggs": {
     *     "min_amount": { "min": { "field": "total_amount" } },
     *     "max_amount": { "max": { "field": "total_amount" } }
     *   }
     * }
     */
    public MinMax getMinAndMaxAmountFromOrders() {
        // ✅ Build Min And Max Amount Aggregation ....
        Aggregation minAggs = Aggregation.of(a -> a.min(m -> m.field("total_amount")));

        Aggregation maxAggs = Aggregation.of(a -> a.max(m -> m.field("total_amount")));

        // ✅ Build NativeQuery with the aggregation
        NativeQuery query = NativeQuery.builder()
                .withAggregation("min_amount", minAggs)
                .withAggregation("max_amount", maxAggs)
                .build();

        // ✅ Run the query - we don't care about hits, only aggregation result
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null)   return new MinMax(null, null);

        ElasticsearchAggregation minAggWrapper = springAggs.aggregationsAsMap().get("min_amount");
        ElasticsearchAggregation maxAggWrapper = springAggs.aggregationsAsMap().get("max_amount");

        Double min = null;
        Double max = null;

        if (minAggWrapper != null){
            Aggregate minAggObject = minAggWrapper.aggregation().getAggregate();

            if(minAggObject.isMin()){
                min = minAggObject.min().value();
                System.out.println("Minimum Amount: " + min);
            }
        }

        if (maxAggWrapper != null){
            Aggregate maxAggObject = maxAggWrapper.aggregation().getAggregate();

            if(maxAggObject.isMax()){
                max = maxAggObject.max().value();
                System.out.println("Maximum Amount: " + max);
            }
        }

        // ✅ Extract sum value ....
        return new MinMax(min, max);
    }


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "aggs": {
     *     "orders_by_status": {
     *       "terms": {
     *         "field": "status"
     *       }
     *     }
     *   }
     * }
     */
    public Map<String, Long> getOrdersGroupedByStatus() {
        // ✅ Build Average Aggregation ....
        Aggregation groupByAggs = Aggregation.of(a -> a
                .terms(t -> t.field("status"))
        );

        // ✅ Build NativeQuery with the aggregation
        NativeQuery query = NativeQuery.builder()
                .withAggregation("orders_by_status", groupByAggs)
                .build();

        // ✅ Run the query - we don't care about hits, only aggregation result
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        // ✅ get Spring's wrapper for aggregations ....
        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null){
            return Collections.emptyMap();
        }

        // ✅ get the named aggregation wrapper ....
        ElasticsearchAggregation groupByAggWrapper = springAggs.aggregationsAsMap().get("orders_by_status");
        if (groupByAggWrapper == null){
            return Collections.emptyMap();
        }

        // ✅ get the underlying Aggregate (typed union from the Java client) ....
        Aggregate groupByAggObject = groupByAggWrapper.aggregation().getAggregate();

        Map<String, Long> result = new LinkedHashMap<>();

        /**
         * ✅ terms aggregation response can come back in different typed flavours
         * depending on the field type: string-terms (sterms), long-terms (lterms),
         * or multi_terms (multiTerms). We check each and handle accordingly.
         *
         * b.key().stringValue() vs String.valueOf(b.key())
         * Explanation:
         *
         * b.key() returns FieldValue, which is a union type in the new Elasticsearch Java Client.
         *
         * FieldValue can represent string, long, double, boolean, etc.
         * That's why it has typed accessors:
         *
         * b.key().stringValue() → returns the actual string
         *
         * b.key().longValue() → returns the numeric value (if the field is numeric)
         *
         * b.key().isString() → you can check type if needed
         */
        if (groupByAggObject.isSterms()) { // string terms (most common for "status")
            //groupByAggObject.sterms().buckets().array()
                    //.forEach((StringTermsBucket b) -> result.put(b.key().stringValue(), b.docCount()));
            groupByAggObject.sterms().buckets().array()
                    .forEach((StringTermsBucket b) -> {
                        String key;
                        if (b.key().isString()) {
                            key = b.key().stringValue();
                        } else if (b.key().isLong()) {
                            key = String.valueOf(b.key().longValue());
                        } else {
                            key = b.key().toString(); // fallback
                        }
                        result.put(key, b.docCount());
                    });

        } else if (groupByAggObject.isLterms()) { // numeric terms
            groupByAggObject.lterms().buckets().array()
                    .forEach((LongTermsBucket b) -> result.put(String.valueOf(b.key()), b.docCount()));
        } else if (groupByAggObject.isMultiTerms()) { // multi-field terms -> bucket.key() is a list
            groupByAggObject.multiTerms().buckets().array()
                    .forEach((MultiTermsBucket b) -> {
                        String compositeKey = b.key().stream()
                                .map(Objects::toString)         // convert each key part to string
                                .collect(Collectors.joining("|")); // join with a separator
                        result.put(compositeKey, b.docCount());
                    });
        } else {
            // unexpected type — return empty or throw if you prefer
            return Collections.emptyMap();
        }

        return result;
    }


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "aggs": {
     *     "revenue_per_customer": {
     *       "terms": {
     *         "field": "customer",
     *         "size": 5
     *       },
     *       "aggs": {
     *         "total_spent": {
     *           "sum": {
     *             "field": "total_amount"
     *           }
     *         }
     *       }
     *     }
     *   }
     * }
     */
    public List<CustomerRevenueResponse> getRevenuePerCustomer(){

        // ✅ Step 1: Build inner sum aggregation ....
        Aggregation totalSpentAgg = Aggregation.of(a -> a
                .sum(s -> s.field("total_amount"))
        );
        // ✅ Step 2: Build outer terms aggregation ....
        Aggregation revenuePerCustomerAgg = Aggregation.of(a -> a
                .terms(t -> t.field("customer").size(5))
                .aggregations("total_spent", totalSpentAgg)
        );

        // ✅ Alternate Way that combines both the above approaches. Build Terms Aggregation with sub-aggregation for sum ....
        /*Aggregation termsAggs = Aggregation.of(a -> a
                .terms(t -> t
                        .field("customer")
                        .size(5)
                )
                .aggregations("total_spent", Aggregation.of(aa -> aa
                        .sum(s -> s.field("total_amount"))
                ))
        );*/

        // ✅ Step 3: Build query ....
        NativeQuery query = NativeQuery.builder()
                .withAggregation("revenue_per_customer", revenuePerCustomerAgg)
                .withMaxResults(0)
                .build();

        // ✅ Step 4: Run the query ....
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        // ✅ Step 5: Extract aggregations
        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null) return List.of();

        ElasticsearchAggregation aggWrapper = springAggs.aggregationsAsMap().get("revenue_per_customer");
        if (aggWrapper == null) return List.of();

        // ✅ Get the underlying Aggregate (typed union from the Java client) ....
        Aggregate termsAggObject = aggWrapper.aggregation().getAggregate();

        // ✅ Step 6: Build response list ....
        List<CustomerRevenueResponse> result = new ArrayList<>();

        if (termsAggObject.isSterms()) { // string terms (most common for "status") ....
            termsAggObject.sterms().buckets().array()
                    .forEach((StringTermsBucket b) -> {
                        String customer = b.key().stringValue();
                        long ordersCount = b.docCount();

                        // ✅ Extract sub-aggregation result ....
                        double totalSpent = 0.0;
                        if (b.aggregations() != null && b.aggregations().get("total_spent") != null) {
                            Aggregate subAgg = b.aggregations().get("total_spent");
                            if (subAgg.isSum()) {
                                totalSpent = subAgg.sum().value();
                            }
                        }

                        // ✅ Add to response list ....
                        result.add(new CustomerRevenueResponse(customer, ordersCount, totalSpent));
                    });

        } else {
            // unexpected type — return empty or throw if you prefer
            return Collections.emptyList();
        }

        return result;
    }

}
