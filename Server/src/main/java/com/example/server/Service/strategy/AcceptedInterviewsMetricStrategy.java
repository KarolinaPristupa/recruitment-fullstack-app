package com.example.server.Service.strategy;

import com.example.server.Models.Interview;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AcceptedInterviewsMetricStrategy implements EmployeeMetricStrategy {

    @Override
    public String getMetricName() {
        return "Accepted Interviews";
    }

    @Override
    public double calculateMetric(List<Interview> interviews) {
        return interviews.stream()
                .filter(interview -> "Принят".equals(interview.getResult()))
                .count();
    }
}