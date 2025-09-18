package com.spring.elasticsearch.learning.models;

// DTO returned by the method (use lombok or regular getters/setters if you prefer)
public class RevenueStatsResponse {
    private final double totalRevenue;
    private final double averageOrderValue;
    private final double minOrderAmount;
    private final double maxOrderAmount;

    public RevenueStatsResponse(double totalRevenue,
                                double averageOrderValue,
                                double minOrderAmount,
                                double maxOrderAmount) {
        this.totalRevenue = totalRevenue;
        this.averageOrderValue = averageOrderValue;
        this.minOrderAmount = minOrderAmount;
        this.maxOrderAmount = maxOrderAmount;
    }

    public double getTotalRevenue() { return totalRevenue; }
    public double getAverageOrderValue() { return averageOrderValue; }
    public double getMinOrderAmount() { return minOrderAmount; }
    public double getMaxOrderAmount() { return maxOrderAmount; }
}
