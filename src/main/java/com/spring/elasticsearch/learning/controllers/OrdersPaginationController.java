package com.spring.elasticsearch.learning.controllers;

import com.spring.elasticsearch.learning.models.OrderDocument;
import com.spring.elasticsearch.learning.service.OrderPaginationAggregations;
import com.spring.elasticsearch.learning.service.OrdersPaginationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersPaginationController {

    @Autowired
    private OrdersPaginationService orderService;

    @Autowired
    OrderPaginationAggregations orderPaginationAggregations;

    @PostMapping("/add")
    public OrderDocument createOrder(@RequestBody OrderDocument order) {
        return orderService.addOrder(order);
    }

    @GetMapping("/fetch-by-customer/{customer}")
    public List<OrderDocument> getOrdersByCustomer(@PathVariable String customer) {
        return orderService.getOrdersByCustomerUsingTermQuery(customer);
    }


    @GetMapping("/match-by-status/{status}")
    public List<OrderDocument> getOrdersByStatus(@PathVariable String status) {
        return orderService.getOrdersByStatusUsingMatchQuery(status);
    }

    @GetMapping("/orders-by-range")
    public List<OrderDocument> getOrdersByRange() {
        return orderService.getOrdersUsingRangeQuery();
    }

    @GetMapping("/combinequeries")
    public List<OrderDocument> getOrdersByCombiningQueries() {
        return orderService.getOrdersByCombiningQueries();
    }


    @GetMapping("/sort-and-pagination")
    public List<OrderDocument> getOrdersBySortAndPaginationQueries() {
        return orderService.getOrdersBySortAndPaginationQueries();
    }

    /* ----------------------------------------------------------------------------------------------- */

    @GetMapping("/aggs-total-orders-count")
    public long getOrdersCountByAggregationQueries() {
        return orderPaginationAggregations.getTotalOrdersCount();
    }


    /* ----------------------------------------------------------------------------------------------- */

    @GetMapping("/top")
    public List<OrderDocument> getTopOrders(@RequestParam(defaultValue = "3") int size) {
        SearchHits<OrderDocument> searchHits = orderService.getTopOrders(size);

        List<OrderDocument> orderDocumentList = searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();

        return orderDocumentList;
    }

    @GetMapping("/revenue")
    public List<OrderDocument> getRevenuePerCustomer() {
        SearchHits<OrderDocument> searchHits = orderService.getRevenuePerCustomer();

        List<OrderDocument> orderDocumentList = searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();

        return orderDocumentList;
    }

    @GetMapping("/high-value")
    public List<OrderDocument> getHighValueOrders(@RequestParam double minAmount) {
        SearchHits<OrderDocument> searchHits = orderService.getHighValueOrdersPerCustomer(minAmount);

        List<OrderDocument> orderDocumentList = searchHits.stream()
                .map(hit -> hit.getContent())
                .toList();

        return orderDocumentList;
    }
}
