package com.example.performance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PerformanceOptimizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(PerformanceOptimizationApplication.class, args);
    }
}
