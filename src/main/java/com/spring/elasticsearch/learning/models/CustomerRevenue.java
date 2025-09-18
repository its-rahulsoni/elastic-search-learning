package com.spring.elasticsearch.learning.models;

public class CustomerRevenue {
    private final String customer;
    private final double totalSpent;

    public CustomerRevenue(String customer, double totalSpent) {
        this.customer = customer;
        this.totalSpent = totalSpent;
    }

    public String getCustomer() { return customer; }
    public double getTotalSpent() { return totalSpent; }
}
