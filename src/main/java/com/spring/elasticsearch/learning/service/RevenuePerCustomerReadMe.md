Perfect — that's the **right approach** for a clean API response!
We’ll create a **POJO (Java class)** to hold the result per customer, then return a **List** of those objects from your service method.

---

### ✅ Step 1: Create a Response DTO

```java
public class CustomerRevenueResponse {
    private String customer;
    private long orderCount;
    private double totalSpent;

    // ✅ Constructors
    public CustomerRevenueResponse(String customer, long orderCount, double totalSpent) {
        this.customer = customer;
        this.orderCount = orderCount;
        this.totalSpent = totalSpent;
    }

    // ✅ Getters & Setters
    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }
}
```

---

### ✅ Step 2: Modify Your Service Method

```java
public List<CustomerRevenueResponse> getRevenuePerCustomer() {
    // Step 1: Build inner sum aggregation
    Aggregation totalSpentAgg = Aggregation.of(a -> a
            .sum(s -> s.field("total_amount"))
    );

    // Step 2: Build outer terms aggregation
    Aggregation revenuePerCustomerAgg = Aggregation.of(a -> a
            .terms(t -> t.field("customer").size(5))
            .aggregations("total_spent", totalSpentAgg)
    );

    // Step 3: Build query
    NativeQuery query = NativeQuery.builder()
            .withAggregation("revenue_per_customer", revenuePerCustomerAgg)
            .withMaxResults(0)
            .build();

    // Step 4: Execute search
    SearchHits<OrderDocument> searchHits = operations.search(query, OrderDocument.class);

    // Step 5: Extract aggregations
    ElasticsearchAggregations springAggs = (ElasticsearchAggregations) searchHits.getAggregations();
    if (springAggs == null) return List.of();

    ElasticsearchAggregation aggWrapper = springAggs.aggregationsAsMap().get("revenue_per_customer");
    if (aggWrapper == null) return List.of();

    Aggregate groupByAgg = aggWrapper.aggregation().getAggregate();

    // Step 6: Build response list
    List<CustomerRevenueResponse> result = new ArrayList<>();

    groupByAgg.sterms().buckets().array().forEach(bucket -> {
        String customer = bucket.key().stringValue();
        long orderCount = bucket.docCount();

        // ✅ Extract sub-aggregation result
        double totalSpent = bucket.aggregations().get("total_spent").sum().value();

        // ✅ Add to response list
        result.add(new CustomerRevenueResponse(customer, orderCount, totalSpent));
    });

    return result;
}
```

---

### ✅ Step 3: Example API Response

If your controller calls this method:

```java
@GetMapping("/revenue-per-customer")
public List<CustomerRevenueResponse> revenuePerCustomer() {
    return orderService.getRevenuePerCustomer();
}
```

Your API response will look like:

```json
[
  {
    "customer": "Alice",
    "orderCount": 3,
    "totalSpent": 142.5
  },
  {
    "customer": "Bob",
    "orderCount": 2,
    "totalSpent": 200.0
  }
]
```

---

### 📝 Why This Is Good Design

* ✅ **Encapsulation** – The logic stays inside the service method, controller just delegates.
* ✅ **Type-Safe** – No raw `Map<String, Object>` that might break at runtime.
* ✅ **Extensible** – If you later add `averageSpent` or `lastOrderDate`, you just update the DTO.

---

Would you like me to also show **how to handle the case where no customers are found** (return empty list vs. throw an exception vs. return HTTP 204)?
This is important if your API might be consumed by a frontend.
