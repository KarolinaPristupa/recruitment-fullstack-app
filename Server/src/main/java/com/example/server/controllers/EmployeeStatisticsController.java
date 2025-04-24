package com.example.server.controllers;

import com.example.server.DTO.EmployeeTopResponse;
import com.example.server.Service.EmployeeStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/statistics")
public class EmployeeStatisticsController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeStatisticsController.class);

    private final EmployeeStatisticsService employeeStatisticsService;

    public EmployeeStatisticsController(EmployeeStatisticsService employeeStatisticsService) {
        this.employeeStatisticsService = employeeStatisticsService;
    }

    @GetMapping
    public ResponseEntity<?> getTopEmployee(
            @RequestParam(value = "startDate", required = false) LocalDateTime startDate
    ) {
        try {
            logger.info("Received request to fetch top employee");
            if (startDate == null) {
                startDate = LocalDateTime.now().minusDays(30);
            }
            EmployeeTopResponse topEmployee = employeeStatisticsService.getTopEmployee(startDate);
            if (topEmployee == null) {
                logger.warn("No top employee found");
                return ResponseEntity.status(404).body("No top employee found");
            }
            return ResponseEntity.ok(topEmployee);
        } catch (Exception e) {
            logger.error("Error fetching top employee: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching top employee: " + e.getMessage());
        }
    }
}