package com.spring.elasticsearch.learning.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    private static final Logger logger = LogManager.getLogger(MyController.class);

    @GetMapping("/hello")
    public String hello() {
        logger.info("Hello endpoint called");
        logger.debug("This is a debug message");
        logger.error("This is an error message");
        return "Hello World!";
    }
}
