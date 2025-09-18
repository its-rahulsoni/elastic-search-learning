package com.spring.elasticsearch.learning.models;

// ✅ DTO class to hold daily sales aggregation results
public class DailySalesStats {
    private long orderCount;
    private double totalSales;
    private double avgSales;

    public DailySalesStats(long orderCount, double totalSales, double avgSales) {
        this.orderCount = orderCount;
        this.totalSales = totalSales;
        this.avgSales = avgSales;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public double getAvgSales() {
        return avgSales;
    }

    public void setAvgSales(double avgSales) {
        this.avgSales = avgSales;
    }
}
