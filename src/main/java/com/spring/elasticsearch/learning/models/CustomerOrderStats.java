package com.spring.elasticsearch.learning.models;

// ✅ DTO class to hold customer-level aggregation results
public class CustomerOrderStats {
    private long orderCount;
    private double avgOrderValue;
    private double maxOrderValue;

    public CustomerOrderStats(long orderCount, double avgOrderValue, double maxOrderValue) {
        this.orderCount = orderCount;
        this.avgOrderValue = avgOrderValue;
        this.maxOrderValue = maxOrderValue;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public double getAvgOrderValue() {
        return avgOrderValue;
    }

    public void setAvgOrderValue(double avgOrderValue) {
        this.avgOrderValue = avgOrderValue;
    }

    public double getMaxOrderValue() {
        return maxOrderValue;
    }

    public void setMaxOrderValue(double maxOrderValue) {
        this.maxOrderValue = maxOrderValue;
    }
}
