package com.spring.elasticsearch.learning.service;

import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.spring.elasticsearch.learning.models.*;
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


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "query": {
     *     "term": { "status": "PAID" }
     *   },
     *   "aggs": {
     *     "paid_revenue": {
     *       "sum": {
     *         "field": "total_amount"
     *       }
     *     }
     *   }
     * }
     */
    public double getTotalRevenueFromPaidOrders() {
        // ✅ Step 1: Build the term query for status = "PAID" ....
        Query statusFilterQuery = Query.of(q -> q
                .term(t -> t
                        .field("status")
                        .value(v -> v.stringValue("PAID"))
                )
        );

        // ✅ Step 2: Build the sum aggregation
        Aggregation paidRevenueAgg = Aggregation.of(a -> a
                .sum(s -> s.field("total_amount")
                )
        );

        // ✅ Step 3: Build the NativeQuery with both query + aggregation
        NativeQuery query = NativeQuery.builder()
                .withQuery(statusFilterQuery)
                .withAggregation("paid_revenue", paidRevenueAgg)
                .withMaxResults(0) // IMP: we don't want document hits, only aggregation result ....
                .build();

        // ✅ Step 4: Execute search
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        // ✅ Step 5: Extract aggregations safely
        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null) return 0.0;

        ElasticsearchAggregation aggWrapper = springAggs.aggregationsAsMap().get("paid_revenue");
        if (aggWrapper == null) return 0.0;

        Aggregate aggObject = aggWrapper.aggregation().getAggregate();
        if (!aggObject.isSum()) return 0.0;

        // ✅ Step 6: Extract sum value
        return aggObject.sum().value();
    }


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "query": {
     *     "term": { "status": "PAID" }
     *   },
     *   "aggs": {
     *     "total_revenue": { "sum": { "field": "total_amount" } },
     *     "average_order_value": { "avg": { "field": "total_amount" } },
     *     "min_order_amount": { "min": { "field": "total_amount" } },
     *     "max_order_amount": { "max": { "field": "total_amount" } }
     *   }
     * }
     */
    public RevenueStatsResponse getPaidRevenueStats() {

        // ✅ Step 1: Build the filter (term query) - status == "PAID" ....
        Query statusFilterQuery = Query.of(q -> q.term(t ->
                t.field("status")
                        .value(v -> v.stringValue("PAID"))));


        // ✅ Step 2: Build the aggregations (sum, avg, min, max on total_amount)
        Aggregation sumAgg = Aggregation.of(a ->
                a.sum(s -> s.field("total_amount"))
        );

        Aggregation avgAgg = Aggregation.of(a ->
                a.avg(av -> av.field("total_amount"))
        );

        Aggregation minAgg = Aggregation.of(a ->
                a.min(m -> m.field("total_amount"))
        );

        Aggregation maxAgg = Aggregation.of(a ->
                a.max(m -> m.field("total_amount"))
        );

        // ✅ Step 3: Compose the NativeQuery with query + all aggregations; set size=0 (we don't need hits) ....
        NativeQuery query = NativeQuery.builder()
                .withQuery(statusFilterQuery)
                .withAggregation("total_revenue", sumAgg)
                .withAggregation("average_order_value", avgAgg)
                .withAggregation("min_order_amount", minAgg)
                .withAggregation("max_order_amount", maxAgg)
                .withMaxResults(0) // IMP: we don't want document hits, only aggregation result ....
                .build();

        // ✅ Step 4: Execute search ....
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        /**
         * ✅ Step 5: Extract Spring's aggregation wrapper (may be null if ES returned none) ....
         *
         * Getting aggregations:
         * searchHits.getAggregations() returns Spring's wrapper; we cast it to ElasticsearchAggregations.
         * ElasticsearchAggregations offers get(name) and aggregationsAsMap() for lookup. get(name) is convenient.
         */
        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null) {
            // No aggregations returned — return zeros (or handle as you prefer)
            return new RevenueStatsResponse(0.0, 0.0, 0.0, 0.0);
        }

        /**
         * ✅ Step 6: Safely get each named aggregation wrapper ....
         *
         * Safely reading aggregation results:
         * Each wrapper contains the Java Client's Aggregate union (a single object that may represent many possible aggregation results).
         * Important: You must check the correct variant:
         * For sum → aggregate.isSum() then aggregate.sum().value()
         * For avg → aggregate.isAvg() then aggregate.avg().value()
         * etc.
         *
         * Don’t check isValueCount() for sum/avg — that will be false.
         */
        ElasticsearchAggregation totalRevenueWrapper = springAggs.aggregationsAsMap().get("total_revenue");
        ElasticsearchAggregation averageOrderWrapper = springAggs.aggregationsAsMap().get("average_order_value");
        ElasticsearchAggregation minOrderWrapper = springAggs.aggregationsAsMap().get("min_order_amount");
        ElasticsearchAggregation maxOrderWrapper = springAggs.aggregationsAsMap().get("max_order_amount");


        /**
         * ✅ Step 7: Default values if any aggregation is missing ....
         *
         * Why double for metric values ?
         * sum, avg, min, max return floating-point values — use double (or Double) to preserve decimal precision. Converting to long truncates/rounds and will lose cents.
         */
        double totalRevenue = 0.0;
        double avgValue     = 0.0;
        double minValue     = 0.0;
        double maxValue     = 0.0;

        /**
         * ✅ Step 8: For each wrapper: get the underlying Aggregate union and read the correct metric type ....
         *
         * Null / missing safeguards:
         * Any of the wrappers may be null (e.g., if Elasticsearch failed to compute one aggregation). We default to 0.0 —
         * you can change this (e.g., return Optional, NaN, or throw an exception) based on your API contract.
         */
        if (totalRevenueWrapper != null) {
            Aggregate aggregate = totalRevenueWrapper.aggregation().getAggregate();
            if (aggregate.isSum()) {
                totalRevenue = aggregate.sum().value(); // sum -> double
            }
        }

        if (averageOrderWrapper != null) {
            Aggregate aggregate = averageOrderWrapper.aggregation().getAggregate();
            if (aggregate.isAvg()) {
                avgValue = aggregate.avg().value(); // avg -> double
            }
        }

        if (minOrderWrapper != null) {
            Aggregate aggregate = minOrderWrapper.aggregation().getAggregate();
            if (aggregate.isMin()) {
                minValue = aggregate.min().value(); // min -> double
            }
        }

        if (maxOrderWrapper != null) {
            Aggregate aggregate = maxOrderWrapper.aggregation().getAggregate();
            if (aggregate.isMax()) {
                maxValue = aggregate.max().value(); // max -> double
            }
        }

        // ✅ Step 9: Return DTO ....
        return new RevenueStatsResponse(totalRevenue, avgValue, minValue, maxValue);
    }


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "query": {
     *     "term": { "status": "PAID" }
     *   },
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
    public List<CustomerRevenue> getTopCustomersByRevenue() {
        // ✅ Step 1: Filter - status == "PAID"
        Query statusFilter = Query.of(q -> q
                .term(t -> t
                        .field("status") // change to "status.keyword" if needed
                        .value("PAID")
                )
        );

        /**
         * ✅ Step 2: Nested aggregation: terms aggregation on "customer" ....
         * Terms aggregation:
         * .terms(t -> t.field("customer").size(5)) → group docs by customer, return top 5 buckets by doc count.
         * .aggregations("total_spent", ...) → add a sub-aggregation under each bucket that sums total_amount.
         */
        Aggregation revenuePerCustomerAgg = Aggregation.of(a -> a
                .terms(t -> t
                        .field("customer")
                        .size(5)) // top 5 customers ....
                .aggregations("total_spent", Aggregation.of(subAgg -> subAgg
                        .sum(s -> s.field("total_amount"))
                ))
        );

        // ✅ Step 3: Build native query (with size = 0 because we don’t care about hits)
        NativeQuery query = NativeQuery.builder()
                .withQuery(statusFilter)
                .withAggregation("revenue_per_customer", revenuePerCustomerAgg)
                .withMaxResults(0)
                .build();

        // ✅ Step 4: Execute query
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        // ✅ Step 5: Extract aggregations
        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null){
            return Collections.emptyList();
        }

        ElasticsearchAggregation wrapper = springAggs.get("revenue_per_customer");
        if (wrapper == null){
            return Collections.emptyList();
        }

        Aggregate agg = wrapper.aggregation().getAggregate();
        if (!agg.isSterms()){
            return Collections.emptyList();
        }

        List<CustomerRevenue> result = new ArrayList<>();

        /**
         * ✅ Step 6: Iterate buckets ....
         *
         * Iterate buckets:
         * Each bucket contains:
         * bucket.key() → customer name
         * bucket.docCount() → how many orders this customer had
         * bucket.aggregations() → map of sub-aggregations (our total_spent)
         *
         * We read total_spent sum for each bucket and create a CustomerRevenue object.
         *
         * Note: The details of this Bucket structure can be found in ReadMe file ....
         */
        for (StringTermsBucket bucket : agg.sterms().buckets().array()) {
            String customerKey = bucket.key().stringValue();

            // Get nested sum aggregation per bucket
            Aggregate totalSpentAgg = bucket.aggregations().get("total_spent");
            double totalSpent = 0.0;
            if (totalSpentAgg.isSum()) {
                totalSpent = totalSpentAgg.sum().value();
            }

            result.add(new CustomerRevenue(customerKey, totalSpent));
        }

        return result;
    }


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "query": {
     *     "bool": {
     *       "must": [
     *         { "term": { "status": "PAID" } },
     *         { "range": { "order_date": { "gte": "now-30d/d" } } }
     *       ]
     *     }
     *   },
     *   "aggs": {
     *     "orders_by_customer": {
     *       "terms": {
     *         "field": "customer",
     *         "size": 10
     *       },
     *       "aggs": {
     *         "avg_order_value": {
     *           "avg": { "field": "total_amount" }
     *         },
     *         "max_order_value": {
     *           "max": { "field": "total_amount" }
     *         }
     *       }
     *     }
     *   }
     * }
     *
     * 🔍 Step-by-Step Breakdown of the Query:
     * 1️⃣ query.bool.must
     * We're filtering documents by:
     * term → status must be "PAID".
     * range → order_date must be within the last 30 days.
     * Effectively:
     * "Give me all PAID orders from the last 30 days."
     *
     * 2️⃣ aggs.orders_by_customer
     * Terms aggregation on customer field.
     * This groups orders by each unique customer, limited to top 10 customers by document count.
     *
     * 3️⃣ Sub-aggregations (avg_order_value, max_order_value)
     * avg_order_value → calculates the average total_amount per customer.
     * max_order_value → finds the maximum order value per customer.
     */
    public Map<String, CustomerOrderStats> getCustomerOrderStatsLast30Days() {

        /**
         * 1️⃣ Build the bool query (status = PAID AND order_date >= now-30d). We've used 330 as per our data in ES ....
         *
         * Query Building:
         * We use a bool query because we have multiple conditions (status and date range).
         * term is used for exact match (status is keyword).
         * range is used for time filtering with gte.
         */
        Query boolQuery = Query.of(q -> q
                .bool(b -> b
                        .must(m -> m.term(t -> t.field("status").value("PAID")))
                        .must(m -> m.range(r -> r.field("order_date").gte(JsonData.of("now-330d/d"))))
                )
        );


        /**
         * 2️⃣ Build sub-aggregations: avg and max order value ....
         *
         * Sub-Aggregations:
         * avg and max aggregations are built independently.
         * These are attached to the main terms aggregation via .aggregations("name", aggregation).
         *
         * Parent Aggregation:
         * terms on customer with .size(10) → top 10 customers.
         */
        Aggregation avgOrderValueAgg = Aggregation.of(a -> a
                .avg(avg -> avg.field("total_amount"))
        );

        Aggregation maxOrderValueAgg = Aggregation.of(a -> a
                .max(max -> max.field("total_amount"))
        );

        // 3️⃣ Build parent aggregation: group by customer
        Aggregation ordersByCustomerAgg = Aggregation.of(a -> a
                .terms(t -> t.field("customer").size(10))
                .aggregations("avg_order_value", avgOrderValueAgg)
                .aggregations("max_order_value", maxOrderValueAgg)
        );

        // 4️⃣ Build native query
        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withAggregation("orders_by_customer", ordersByCustomerAgg)
                .build();

        // 5️⃣ Execute the search
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);
        ElasticsearchAggregations aggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (aggs == null) return Collections.emptyMap();

        // 6️⃣ Extract the terms aggregation result
        ElasticsearchAggregation wrapper = aggs.aggregationsAsMap().get("orders_by_customer");
        Aggregate agg = wrapper.aggregation().getAggregate();

        Map<String, CustomerOrderStats> result = new LinkedHashMap<>();

        /**
         * Iteration Over Buckets:
         * Each bucket = one customer group.
         *
         * We fetch:
         * bucket.key().stringValue() → customer name.
         * bucket.docCount() → number of orders for this customer.
         * Nested aggregations for avg and max order value.
         */
        for (StringTermsBucket bucket : agg.sterms().buckets().array()) {
            String customer = bucket.key().stringValue();
            long orderCount = bucket.docCount();

            double avgOrderValue = bucket.aggregations()
                    .get("avg_order_value")
                    .avg()
                    .value();

            double maxOrderValue = bucket.aggregations()
                    .get("max_order_value")
                    .max()
                    .value();

            result.put(customer, new CustomerOrderStats(orderCount, avgOrderValue, maxOrderValue));
        }

        return result;
    }

    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "query": {
     *     "bool": {
     *       "must": [
     *         { "match": { "customer": "John" } },
     *         { "range": { "total_amount": { "gte": 100 } } }
     *       ]
     *     }
     *   },
     *   "aggs": {
     *     "daily_sales": {
     *       "date_histogram": {
     *         "field": "order_date",
     *         "calendar_interval": "day"
     *       },
     *       "aggs": {
     *         "total_sales": { "sum": { "field": "total_amount" } },
     *         "avg_sales": { "avg": { "field": "total_amount" } }
     *       }
     *     }
     *   }
     * }
     *
     * 🔍 Step-by-Step Breakdown of Query:
     * 1️⃣ query.bool.must
     * We are filtering orders where:
     * match → customer must match "John" (full-text match, not exact keyword match).
     * range → total_amount must be at least 100.
     *
     * Effectively:
     * "Get all orders by John where total amount is >= 100."
     *
     * 2️⃣ aggs.daily_sales
     * date_histogram on order_date
     * Groups orders per day (one bucket per calendar day).
     *
     * 3️⃣ Sub-aggregations inside each date bucket:
     * total_sales → sum of total_amount per day.
     * avg_sales → average of total_amount per day.
     * This is a classic "time series + metrics" style query — very common for dashboards.
     */
    public Map<String, DailySalesStats> getDailySalesForCustomer(String customerName) {

        // 1️⃣ Build bool query with match + range
        Query boolQuery = Query.of(q -> q
                .bool(b -> b
                        .must(m -> m.match(mm -> mm.field("customer").query(customerName)))
                        .must(m -> m.range(r -> r.field("total_amount").gte(JsonData.of(100))))
                )
        );

        // 2️⃣ Build sub-aggregations: total sales and avg sales
        Aggregation totalSalesAgg = Aggregation.of(a -> a.sum(s -> s.field("total_amount")));
        Aggregation avgSalesAgg = Aggregation.of(a -> a.avg(avg -> avg.field("total_amount")));

        // 3️⃣ Build date histogram aggregation with sub-aggs
        Aggregation dailySalesAgg = Aggregation.of(a -> a
                .dateHistogram(dh -> dh
                        .field("order_date")
                        .calendarInterval(CalendarInterval.Day)
                )
                .aggregations("total_sales", totalSalesAgg)
                .aggregations("avg_sales", avgSalesAgg)
        );

        // 4️⃣ Build native query
        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withAggregation("daily_sales", dailySalesAgg)
                .build();

        // 5️⃣ Execute the search
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);
        ElasticsearchAggregations aggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (aggs == null) return Collections.emptyMap();

        // 6️⃣ Get the daily_sales aggregation result
        ElasticsearchAggregation wrapper = aggs.aggregationsAsMap().get("daily_sales");
        Aggregate agg = wrapper.aggregation().getAggregate();

        Map<String, DailySalesStats> result = new LinkedHashMap<>();

        for (DateHistogramBucket bucket : agg.dateHistogram().buckets().array()) {
            String dateKey = bucket.keyAsString();
            long orderCount = bucket.docCount();

            double totalSales = bucket.aggregations()
                    .get("total_sales")
                    .sum()
                    .value();

            double avgSales = bucket.aggregations()
                    .get("avg_sales")
                    .avg()
                    .value();

            result.put(dateKey, new DailySalesStats(orderCount, totalSales, avgSales));
        }

        /**
         * 🗂 Structure of the Response:
         *
         * {
         *   "2024-12-01T00:00:00.000Z": {  // <-- KEY (date bucket)
         *     "orderCount": 1,            // <-- DOC COUNT (number of docs that match query on this date)
         *     "totalSales": 120.5,        // <-- SUM aggregation result
         *     "avgSales": 120.5           // <-- AVG aggregation result
         *   },
         *   "2024-12-02T00:00:00.000Z": {
         *     "orderCount": 0,
         *     "totalSales": 0.0,
         *     "avgSales": 0.0
         *   }
         * }
         *
         * 🔑 Key (Date Bucket)
         * 2024-12-01T00:00:00.000Z is the bucket key that represents midnight UTC for that day.
         *
         * It means:
         * This bucket contains all documents whose order_date falls on December 1st, 2024 (UTC).
         * Time part 00:00:00.000Z is just the start of the day for that bucket.
         * Z = Zulu time = UTC timezone.
         *
         * 🧠 Why Elasticsearch uses midnight UTC?
         * Date histograms always group by "start of interval."
         * If your interval is day, each bucket key represents the start of that day in UTC.
         *
         * Example:
         * 2024-12-01T00:00:00.000Z → bucket covers 2024-12-01T00:00:00.000Z through 2024-12-01T23:59:59.999Z.
         */
        return result;
    }


    /**
     * GET orders_pagination/_search
     * {
     *   "size": 0,
     *   "query": {
     *     "bool": {
     *       "must": [
     *         { "term": { "status": "PAID" } },
     *         { "range": { "order_date": { "gte": "now-30d/d" } } }
     *       ]
     *     }
     *   },
     *   "aggs": {
     *     "orders_by_category": {
     *       "terms": { "field": "category.keyword", "size": 5 },
     *       "aggs": {
     *         "total_sales": { "sum": { "field": "total_amount" } },
     *         "avg_sales": { "avg": { "field": "total_amount" } },
     *         "max_sale": { "max": { "field": "total_amount" } }
     *       }
     *     }
     *   }
     * }
     *
     * 🧠 Explanation of This Query
     *
     * Top-Level Query:
     * bool.must ensures both conditions must match:
     * term: { "status": "PAID" } → only orders with status=PAID.
     * range: { "order_date": { "gte": "now-30d/d" } } → only orders from the last 30 days (rounded to start of day).
     *
     * Aggregations:
     * orders_by_category → groups results by category.keyword field.
     * .keyword ensures exact match, not full-text search.
     * size: 5 limits to top 5 categories by document count.
     *
     * Inside each category bucket, we compute:
     * total_sales → sum of total_amount in that category.
     * avg_sales → average of total_amount in that category.
     * max_sale → highest single order amount in that category.
     *
     * Why is this query advanced?
     * It combines:
     * A filtering query (term + range)
     * A bucket aggregation (terms)
     * Multiple metric aggregations inside each bucket.
     *
     * This is a very common real-world use case for building category-level dashboards.
     */
    public List<CategoryStats> getCategoryStatsLast30Days() {

        // ✅ Step 1: Build Bool Query
        Query boolQuery = Query.of(q -> q
                .bool(b -> b
                        .must(
                                Query.of(q1 -> q1.term(t -> t.field("status").value("PAID"))),
                                Query.of(q2 -> q2.range(r -> r.field("order_date").gte(JsonData.of("now-330d/d"))))
                        )
                )
        );

        // ✅ Step 2: Build Sub-Aggregations
        Aggregation totalSalesAgg = Aggregation.of(a -> a.sum(s -> s.field("total_amount")));
        Aggregation avgSalesAgg = Aggregation.of(a -> a.avg(avg -> avg.field("total_amount")));
        Aggregation maxSaleAgg = Aggregation.of(a -> a.max(max -> max.field("total_amount")));

        // ✅ Step 3: Build Terms Aggregation with Sub-Aggs
        Aggregation categoryAgg = Aggregation.of(a -> a
                .terms(t -> t.field("category").size(5))
                .aggregations("total_sales", totalSalesAgg)
                .aggregations("avg_sales", avgSalesAgg)
                .aggregations("max_sale", maxSaleAgg)
        );

        // ✅ Step 4: Build NativeQuery
        NativeQuery query = NativeQuery.builder()
                .withQuery(boolQuery)
                .withAggregation("orders_by_category", categoryAgg)
                .build();

        // ✅ Step 5: Execute Query
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        // ✅ Step 6: Parse Aggregations
        ElasticsearchAggregations aggs = (ElasticsearchAggregations) searchHits.getAggregations();
        ElasticsearchAggregation ordersByCategoryAggWrapper = aggs.aggregationsAsMap().get("orders_by_category");

        Aggregate ordersByCategoryAgg = ordersByCategoryAggWrapper.aggregation().getAggregate();

        List<CategoryStats> result = new ArrayList<>();

        for (StringTermsBucket bucket : ordersByCategoryAgg.sterms().buckets().array()) {
            String category = bucket.key().stringValue();
            long docCount = bucket.docCount();

            double totalSales = bucket.aggregations().get("total_sales").sum().value();
            double avgSales = bucket.aggregations().get("avg_sales").avg().value();
            double maxSale = bucket.aggregations().get("max_sale").max().value();

            result.add(new CategoryStats(category, totalSales, avgSales, maxSale));
        }

        return result;
    }
}
