package com.spring.elasticsearch.learning.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Document(indexName = "orders_pagination")
public class OrderDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String order_id;

    @Field(type = FieldType.Keyword)
    private String customer;

    @Field(type = FieldType.Date)
    private LocalDate order_date;

    // Note: This is Index Mapping. name = "total_amount" is the field name in ES index and totalAmount is the variable name in this class ....
    @Field(name = "total_amount", type = FieldType.Float)
    private Double totalAmount;

    @Field(type = FieldType.Keyword)
    private String status;

    // Getters & Setters


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public LocalDate getOrder_date() {
        return order_date;
    }

    public void setOrder_date(LocalDate order_date) {
        this.order_date = order_date;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

