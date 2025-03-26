package com.example.carsharingservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @Operation(summary = "Check application health status")
    @GetMapping("/health")
    public String healthCheck() {
        return "OK";
    }
}
