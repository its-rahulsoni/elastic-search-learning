Absolutely — this is a very common use case: **filter + aggregation** (only sum `total_amount` for documents where `status = "PAID"`).

Let’s write a **complete, production-ready Java method** and explain **every decision** step by step.

---

### ✅ Step 1: Decide Method Signature

Since this aggregation returns **only one number** (sum of revenue for `PAID` orders), our method can simply return a `double`.

```java
public double getPaidRevenue()
```

---

### ✅ Step 2: Build the Java Code

```java
public double getPaidRevenue() {
    // ✅ Step 1: Build the term query for status = "PAID"
    Query statusFilter = Query.of(q -> q
            .term(t -> t
                    .field("status")
                    .value("PAID")
            )
    );

    // ✅ Step 2: Build the sum aggregation
    Aggregation paidRevenueAgg = Aggregation.of(a -> a
            .sum(s -> s
                    .field("total_amount")
            )
    );

    // ✅ Step 3: Build the NativeQuery with both query + aggregation
    NativeQuery query = NativeQuery.builder()
            .withQuery(statusFilter)
            .withAggregation("paid_revenue", paidRevenueAgg)
            .withMaxResults(0) // we don't want document hits, only aggregation result
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
```

---

### ✅ Step 3: Controller to Expose API

```java
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/paid-revenue")
    public ResponseEntity<Double> getPaidRevenue() {
        double revenue = orderService.getPaidRevenue();
        return ResponseEntity.ok(revenue);
    }
}
```

---

### ✅ Explanation of Each Step

1. **Term Query (`status = "PAID"`)**

    * `Query.of(q -> q.term(...))` builds a type-safe Elasticsearch `term` query.
    * `field("status")` → match the `status` field.
    * `.value("PAID")` → only return documents where status is exactly `PAID`.

2. **Aggregation (`sum(total_amount)`)**

    * We use `Aggregation.of(a -> a.sum(...))` to build a `sum` aggregation.
    * `field("total_amount")` → field to sum.

3. **NativeQuery**

    * Combines **query + aggregation** in one request.
    * `withMaxResults(0)` avoids fetching hits (we just care about aggregations).

4. **Execution**

    * `operations.search()` runs the query against Elasticsearch.
    * `OrderDocument.class` is the mapping class for hits (not used here but required).

5. **Extracting Aggregations**

    * We cast to `ElasticsearchAggregations`.
    * `.aggregationsAsMap().get("paid_revenue")` fetches the aggregation by name.

6. **Validation & Value Extraction**

    * `.isSum()` ensures we got a sum aggregation.
    * `.sum().value()` extracts the final numeric result as `double`.

7. **Controller**

    * Exposes a clean REST API endpoint `/orders/paid-revenue`.
    * Returns an HTTP 200 with the sum as JSON (e.g. `242.34`).

---

### ✅ Example API Response

```json
242.3456
```

Or, if you want a structured JSON:

```java
@GetMapping("/paid-revenue")
public ResponseEntity<Map<String, Double>> getPaidRevenue() {
    double revenue = orderService.getPaidRevenue();
    return ResponseEntity.ok(Map.of("paidRevenue", revenue));
}
```

This would give:

```json
{
  "paidRevenue": 242.3456
}
```

---

### 📝 Best Practice Tips

* Use `double` (not `long`) since sum can be decimal.
* Always check for `null` aggregations to avoid `NullPointerException`.
* Use a **descriptive aggregation name** (`"paid_revenue"`) to make your code readable.
* Consider adding unit tests using a mock Elasticsearch client to validate behavior.

---

Would you like me to also show you **how to return this response in a custom DTO object** instead of just a double (for cleaner future extensions)?
