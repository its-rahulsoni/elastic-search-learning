Nice — below is a **ready-to-drop-in Java method** (Spring Data Elasticsearch + the new Java client) that runs the `terms` aggregation you showed:

* Query:

```json
GET orders_pagination/_search
{
  "size": 0,
  "aggs": {
    "orders_by_status": {
      "terms": { "field": "status" }
    }
  }
}
```

* Java method (returns `Map<String, Long>` of `status -> docCount`):

```java
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.Aggregation;
import org.springframework.data.elasticsearch.client.elc.TermsAggregation;
import org.springframework.data.elasticsearch.core.query.NativeQuery;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.MultiTermsBucket;

// your document class
// import your.package.OrderDocument;

public class OrderAggService {

    private final ElasticsearchOperations operations;

    public OrderAggService(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    /**
     * Returns a map: status -> docCount (top terms buckets).
     */
    public Map<String, Long> getOrdersByStatus() {

        // 1) build the terms aggregation (name must match what you will look up later)
        Aggregation ordersByStatusAgg = Aggregation.of(a -> a
                .terms(TermsAggregation.of(t -> t
                        .field("status")      // use "status" or "status.keyword" depending on your mapping
                        .size(100)            // top-N buckets to return (change as needed)
                ))
        );

        // 2) build the query with the aggregation (we don't need hits)
        NativeQuery query = NativeQuery.builder()
                .withAggregation("orders_by_status", ordersByStatusAgg)
                .build();

        // 3) execute the query
        SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

        // 4) get Spring's wrapper for aggregations
        ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
        if (springAggs == null) {
            return Collections.emptyMap();
        }

        // 5) get the named aggregation wrapper
        ElasticsearchAggregation ordersAggWrapper = springAggs.aggregationsAsMap().get("orders_by_status");
        if (ordersAggWrapper == null) {
            return Collections.emptyMap();
        }

        // 6) get the underlying Aggregate (typed union from the Java client)
        Aggregate agg = ordersAggWrapper.aggregation().getAggregate();

        Map<String, Long> result = new LinkedHashMap<>();

        // 7) terms aggregation response can come back in different typed flavours
        //    depending on the field type: string-terms (sterms), long-terms (lterms),
        //    or multi_terms (multiTerms). We check each and handle accordingly.
        if (agg.isSterms()) { // string terms (most common for "status")
            agg.sterms().buckets().array()
                    .forEach((StringTermsBucket b) -> result.put(b.key(), b.docCount()));
        } else if (agg.isLterms()) { // numeric terms
            agg.lterms().buckets().array()
                    .forEach((LongTermsBucket b) -> result.put(String.valueOf(b.key()), b.docCount()));
        } else if (agg.isMultiTerms()) { // multi-field terms -> bucket.key() is a list
            agg.multiTerms().buckets().array()
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
}
```

---

# Step-by-step explanation and rationale (detailed)

1. `Aggregation.of(...)` / `TermsAggregation.of(...)`

    * We create an **Aggregation** object in the same style you used earlier (the `Aggregation.of(a -> a.xxx(...))` builder).
    * `terms(...)` is the *terms* aggregation request. The important options:

        * `.field("status")` — the field to group by.

            * **Note:** if `status` is a `text` field with a `keyword` subfield, you must use `"status.keyword"` (or whichever actual keyword field exists). Using the wrong field may return zero or unexpected buckets.
        * `.size(100)` — how many top buckets to return (default is often 10). Increase if you need more unique statuses.

2. `NativeQuery.builder().withAggregation("orders_by_status", ...)`

    * Attach the aggregation to a NativeQuery. The aggregation name `"orders_by_status"` must match what we look up later in the response.

3. `operations.search(query, OrderDocument.class)`

    * Execute the query using your existing `ElasticsearchOperations` instance. We ignore hits — aggregations are returned even with `size:0` (we didn't set `size` explicitly here, but this is the pattern you used before).

4. `ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();`

    * Spring Data Elasticsearch wraps the Java client aggregates in `ElasticsearchAggregations` which gives convenience methods (list and map view).
    * cast is safe because Spring returns that implementation when using the new client.

5. `springAggs.aggregationsAsMap().get("orders_by_status")`

    * Use `.aggregationsAsMap()` (you discovered this earlier) to fetch the named aggregation wrapper directly. This is more convenient than iterating a list.

6. `Aggregate agg = ordersAggWrapper.aggregation().getAggregate();`

    * The inner wrapper exposes the **Java client** `Aggregate` (a tagged union / variant type). This union can represent many different aggregation result shapes (sum, avg, terms, histogram, etc).

7. **Why we check multiple `isX` variants**

    * Terms aggregation *response* shape depends on the *field type*:

        * `string`/`keyword` fields → **string-terms** (method on union: `isSterms()` / `sterms()`)
        * numeric fields → **long-terms** (`isLterms()` / `lterms()`)
        * multi\_terms → `isMultiTerms()` / `multiTerms()`
    * The *request* is always `terms`, but the *response union* has different variants. That’s why we check `isSterms()` / `isLterms()` / `isMultiTerms()` instead of a single `isTerms()`.

8. `b.key()` and `b.docCount()`

    * Each bucket object exposes `key()` (the term) and `docCount()` (number of documents in the bucket).
    * For string buckets `key()` returns a `String`.
    * For long buckets `key()` returns a `Long`, so we convert to string for the map key (or you can keep it typed if you prefer).
    * `docCount()` returns a `long`.

9. `LinkedHashMap` for `result`

    * Keeps insertion order (buckets are returned ordered by doc count by default).
    * You can switch to `HashMap` or `List<DTO>` if you prefer.

10. **Size / ordering / top N**

    * The `size` parameter controls how many buckets are returned. If you need *all* unique values and you have many, consider using the **composite aggregation** with pagination rather than setting a huge `size` (terms with very large `size` is expensive).

11. **Sub-aggregations (common pattern)**

    * If you added a sub-aggregation inside each status bucket (e.g. average order value per status), you can pull that metric from each bucket’s sub-aggregations.
    * Example (pseudo):

      ```java
      // inside the bucket loop:
      Map<String, Aggregate> bucketAggs = b.aggregations(); // (type depends on client version)
      Aggregate avgAgg = bucketAggs.get("avg_total_amount");
      double avg = avgAgg.avg().value(); // check avgAgg.isAvg() first
      ```
    * The exact accessor for bucket-level aggregations depends on client version; if you need sample code for sub-aggregations I can add it.

12. **Why your earlier `isValueCount()` check returned false for sum/avg**

    * `isValueCount()` checks whether the aggregate returned is a *value\_count* result. For `sum` aggregation the correct check is `isSum()` and you should then read `aggregate.sum().value()` which returns `double` (or numeric).
    * Metric aggregations (sum/avg/min/max) return numeric types — read them as `double` (or `Double`) to preserve fractions.

13. **Precision: doubles vs longs**

    * Terms bucket doc counts are `long` (counts).
    * Metric results (avg, sum) are floating point values — use `double` or `Double`.
    * If you cast/round to `long` or use `Math.round()` you will lose fractional precision (you mentioned your sum `242.3456` becoming `242` — use `double` and format as needed).

---

# A few practical tips & gotchas

* Always check the **field mapping**:

    * If `status` is `text` with `keyword`, prefer `"status.keyword"`.
    * If `status` is already `keyword`, `"status"` is fine.
* If you expect many distinct statuses (millions) use **composite aggregation** with pagination.
* If you want deterministic ordering, set the `order` or rely on default (by doc\_count desc).
* If you add sub-aggregations (e.g. `avg` inside each `terms` bucket) show me your desired JSON and I’ll add the exact Java code to extract nested metric(s).

---

If you want, I can:

* show the **sub-aggregation** example (avg per status) with exact Java bucket-level code, or
* give a **composite-aggregation** example for paginating large cardinalities, or
* adapt the method to return a `List<DTO>` sorted by count.

Which would you like next? (I can just pick one and add it — no need for you to wait.)
