Nice — here’s a compact, copy-pasteable Java implementation **plus a step-by-step explanation** so you can build similar aggregation queries easily.

### ✅ Ready-to-use code (returns min/max as `Double`, `null` when missing)

```java
// Simple DTO to return both values
public record MinMax(Double min, Double max) {}

public MinMax getMinMaxAmount() {
    // 1) Build aggregations
    Aggregation minAgg = Aggregation.of(a -> a
            .min(org.elasticsearch.client.elco.MinAggregation.of(m -> m.field("total_amount")))
    );
    Aggregation maxAgg = Aggregation.of(a -> a
            .max(org.elasticsearch.client.elco.MaxAggregation.of(m -> m.field("total_amount")))
    );

    // 2) Build query with both aggregations
    NativeQuery query = NativeQuery.builder()
            .withAggregation("min_amount", minAgg)
            .withAggregation("max_amount", maxAgg)
            .build();

    // 3) Execute query
    SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

    // 4) Get Spring wrapper for aggregations
    ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
    if (springAggs == null) {
        return new MinMax(null, null);
    }

    // 5) Retrieve wrapper objects by name (use get(name) or aggregationsAsMap())
    ElasticsearchAggregation minWrapper = springAggs.get("min_amount");
    ElasticsearchAggregation maxWrapper = springAggs.get("max_amount");

    Double min = null;
    Double max = null;

    // 6) Extract Aggregate and values (check aggregation type)
    if (minWrapper != null) {
        Aggregate a = minWrapper.aggregation().getAggregate();
        if (a.isMin()) {
            min = a.min().value(); // double auto-boxed to Double
        }
    }

    if (maxWrapper != null) {
        Aggregate a = maxWrapper.aggregation().getAggregate();
        if (a.isMax()) {
            max = a.max().value();
        }
    }

    return new MinMax(min, max);
}
```

---

### ### Step-by-step explanation (points style)

1. **Decide what you want from ES**

    * Your JSON query asks for `min` and `max` on field `total_amount`. In Java we build two aggregation objects: a `min` aggregation and a `max` aggregation.

2. **Build the aggregation objects**

    * `Aggregation.of(a -> a.min(...))` creates a min aggregation; `Aggregation.of(a -> a.max(...))` creates a max aggregation.
    * The `.field("total_amount")` is the field name you want to operate on.

3. **Attach aggregations to a `NativeQuery`**

    * `NativeQuery.builder().withAggregation("min_amount", minAgg).withAggregation("max_amount", maxAgg).build()`
    * The strings `"min_amount"` and `"max_amount"` are the aggregation names — you’ll use these keys to look up results later.

4. **Execute the query using Spring Data Elasticsearch**

    * `operations.search(query, OrderDocument.class)` runs the query and returns `SearchHits`. We don’t care about hits (documents) here — only the aggregations.

5. **Get the `ElasticsearchAggregations` wrapper**

    * `ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();`
    * This wrapper gives convenient access to aggregation results as a map or list.

6. **Find the aggregation result by name**

    * Use `springAggs.get("min_amount")` or `springAggs.aggregationsAsMap().get("min_amount")`. That returns an `ElasticsearchAggregation` wrapper.

7. **Extract the `Aggregate` object**

    * `Aggregate a = wrapper.aggregation().getAggregate();` — `Aggregate` can represent many types (`sum`, `avg`, `min`, `max`, `value_count`, etc).

8. **Check aggregation type and read value**

    * For min: `if (a.isMin()) min = a.min().value();`
    * For max: `if (a.isMax()) max = a.max().value();`
    * `.value()` returns a `double` (auto-boxed to `Double` in example). If you prefer primitives use `double`.

9. **Handle missing/no-data cases**

    * If `springAggs` is `null` or the wrapper is `null`, we return `null` (or you can return `Double.NaN` or `0.0` depending on your requirements).
    * *Tip:* to detect “no documents,” you can also inspect `searchHits.getTotalHits()` before you use agg values.

10. **Return a small DTO**

    * I used a `record MinMax(Double min, Double max)` so you get both values neatly. You can change to `OptionalDouble`, primitives, or throw if values missing.

---

### Notes & best practices

* **Use the correct `isX()` check**: don’t test `isValueCount()` for `min`/`max` — use `isMin()` and `isMax()` respectively.
* **Return types**: min/max are doubles — use `double`/`Double` if you want decimals preserved. Only use `long` for counts.
* **Formatting**: if you need to present as currency or round to 2 decimals, format at presentation layer: `String.format(Locale.US, "%.2f", value)`.
* **Null vs NaN vs default**: choose the behavior appropriate for your app (I used `null` to indicate missing).
* **Alternative retrieval**: you can use `springAggs.aggregationsAsMap()` if you prefer a `Map<String, ElasticsearchAggregation>`.

---

If you want, I can:

* return a version that uses primitives (`double`) with `Double.NaN` defaults, or
* give you a generic helper that extracts any single-valued aggregation (sum/avg/min/max/count) by name and returns an `OptionalDouble`.

Which form would you like next?
