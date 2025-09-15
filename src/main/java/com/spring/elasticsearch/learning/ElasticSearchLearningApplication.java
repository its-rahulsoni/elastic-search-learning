package com.spring.elasticsearch.learning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ElasticSearchLearningApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElasticSearchLearningApplication.class, args);
	}

}
