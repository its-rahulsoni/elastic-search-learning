package com.spring.elasticsearch.learning.models;

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
