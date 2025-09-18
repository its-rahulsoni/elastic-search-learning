Perfect ✅ — this is exactly what we needed!
This JSON is your **index mapping**, which defines the schema for `orders_pagination`.

Let’s break this down and see what it means for your future reproducibility setup:

---

### 🔍 Explanation of Your Schema

* **`_class`** → `text` (with a `keyword` subfield)

    * `text` = full-text searchable
    * `keyword` = exact match search (used for filters, aggregations)

* **`category`** → `keyword`

    * Good for aggregations (`terms` query works here)
    * ✅ You are correctly using `"category.keyword"` or just `"category"` in queries

* **`customer`** → `keyword`

    * Allows filtering and aggregation by customer name.

* **`id`** → `text` + `keyword`

    * Probably some Spring Data internal field (`@Id` maybe).
    * The `keyword` field ensures you can search by exact `id`.

* **`order_date`** → `date`

    * Required for your `range` query (`gte: now-330d/d`) to work.
    * ✅ Good choice.

* **`order_id`, `sortable_id`, `status`** → `keyword`

    * Again, perfect for filters/aggregations.

* **`total_amount`** → `float`

    * Used for `sum`, `avg`, `max`, etc.

---

### ✅ Why This Is Important for Your Goal

When you want to **re-create this index next year**, you can just do:

```json
PUT orders_pagination
{
  "mappings": {
    "properties": {
      "_class": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword", "ignore_above": 256 }
        }
      },
      "category": { "type": "keyword" },
      "customer": { "type": "keyword" },
      "id": {
        "type": "text",
        "fields": {
          "keyword": { "type": "keyword", "ignore_above": 256 }
        }
      },
      "order_date": { "type": "date" },
      "order_id": { "type": "keyword" },
      "sortable_id": { "type": "keyword" },
      "status": { "type": "keyword" },
      "total_amount": { "type": "float" }
    }
  }
}
```

This **creates the exact schema** you are using today — no surprises.

---

### 🛠️ Next Steps for You

1. **Save this JSON mapping in your repo**
   Example: `resources/es/orders_pagination_mapping.json`

2. **Save your sample data in bulk format**
   Example:

```json
{ "index": { "_index": "orders_pagination", "_id": "1" } }
{ "order_date": "2024-12-01", "total_amount": 120.5, "sortable_id": "ORD-1001", "category": "General", "order_id": "ORD-1001", "customer": "Alice", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "2" } }
{ "order_date": "2024-12-03", "total_amount": 95, "sortable_id": "ORD-1002", "category": "General", "order_id": "ORD-1002", "customer": "Bob", "status": "PENDING" }
...
```
---
### 📦 Creating the Bulk Data File

Perfect — you already have **all the data** and **the mapping**, so we can easily convert this into a **Bulk API request** to re-create the data in the `orders_pagination` index.

Here’s the **exact bulk query** you can save as a file (for example `orders_bulk.json`) and execute with:

```bash
POST _bulk
{ "index": { "_index": "orders_pagination", "_id": "1" } }
{ "order_date": "2024-12-01", "total_amount": 120.5, "sortable_id": "ORD-1001", "category": "General", "order_id": "ORD-1001", "customer": "Alice", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "2" } }
{ "order_date": "2024-12-03", "total_amount": 95, "sortable_id": "ORD-1002", "category": "General", "order_id": "ORD-1002", "customer": "Bob", "status": "PENDING" }
{ "index": { "_index": "orders_pagination", "_id": "3" } }
{ "order_date": "2024-12-05", "total_amount": 300, "sortable_id": "ORD-1003", "category": "General", "order_id": "ORD-1003", "customer": "Alice", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "4" } }
{ "order_date": "2024-12-10", "total_amount": 210, "sortable_id": "ORD-1004", "category": "General", "order_id": "ORD-1004", "customer": "Charlie", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "5" } }
{ "order_date": "2024-12-11", "total_amount": 50, "sortable_id": "ORD-1005", "category": "General", "order_id": "ORD-1005", "customer": "David", "status": "CANCELLED" }
{ "index": { "_index": "orders_pagination", "_id": "6" } }
{ "order_date": "2024-12-12", "total_amount": 175, "sortable_id": "ORD-1006", "category": "General", "order_id": "ORD-1006", "customer": "Eve", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "7" } }
{ "order_date": "2024-12-14", "total_amount": 400, "sortable_id": "ORD-1007", "category": "General", "order_id": "ORD-1007", "customer": "Frank", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "8" } }
{ "order_date": "2024-12-15", "total_amount": 250, "sortable_id": "ORD-1008", "category": "General", "order_id": "ORD-1008", "customer": "Bob", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "9" } }
{ "order_date": "2024-12-18", "total_amount": 180, "sortable_id": "ORD-1009", "category": "General", "order_id": "ORD-1009", "customer": "Alice", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "10" } }
{ "order_date": "2024-12-20", "total_amount": 305, "sortable_id": "ORD-1010", "category": "General", "order_id": "ORD-1010", "customer": "Charlie", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "11" } }
{ "total_amount": 560, "_class": "com.spring.elasticsearch.learning.models.OrderDocument", "id": "11", "category": "General", "customer": "Rahul", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "o3YqTJkBf6w30_5N_NHU" } }
{ "order_date": "2025-02-17", "total_amount": 320, "_class": "com.spring.elasticsearch.learning.models.OrderDocument", "category": "General", "order_id": "ORD-1012", "customer": "Rahul", "status": "PAID" }
{ "index": { "_index": "orders_pagination", "_id": "13" } }
{ "order_date": "2025-01-25", "total_amount": 215, "_class": "com.spring.elasticsearch.learning.models.OrderDocument", "id": "13", "category": "General", "order_id": "ORD-1013", "customer": "Raju", "status": "PAID" }
```

---

### 🔑 Key Points About This Bulk File

* **Each record has 2 lines:**

    1. Action line (`{ "index": { "_index": "...", "_id": "..." } }`)
    2. The actual document data.

* `_id` is preserved, so when you re-run this file, Elasticsearch will overwrite existing documents (useful for repeatable setups).

* You can run this in **Kibana Dev Tools** directly:

    * Paste everything after `POST _bulk`
    * Make sure there is **no trailing comma** or invalid JSON formatting

---

### ✅ What You Should Do Next

1. Save this file (`orders_bulk.json`) in your repo.
2. Add a small **README** with steps:

    * `DELETE orders_pagination` (optional reset)
    * `PUT orders_pagination` with your mapping
    * `POST _bulk` with this file

This way, future runs of your Java code will just work with the exact same dataset.

---

Do you want me to write **Java code that automatically reads this JSON file and posts it to Elasticsearch using Bulk API** (so you won’t need to manually run the Kibana Dev Tools next year)?
