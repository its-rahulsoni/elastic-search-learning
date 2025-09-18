package com.spring.elasticsearch.learning.models;

public class CategoryStats {
    private String category;
    private double totalSales;
    private double avgSales;
    private double maxSale;

    public CategoryStats(String category, double totalSales, double avgSales, double maxSale) {
        this.category = category;
        this.totalSales = totalSales;
        this.avgSales = avgSales;
        this.maxSale = maxSale;
    }

    // ✅ Getters & Setters
    public String getCategory() { return category; }
    public double getTotalSales() { return totalSales; }
    public double getAvgSales() { return avgSales; }
    public double getMaxSale() { return maxSale; }
}
