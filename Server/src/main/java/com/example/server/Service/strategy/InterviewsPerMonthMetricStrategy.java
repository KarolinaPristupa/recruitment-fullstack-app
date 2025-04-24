package com.example.server.Service.strategy;

import com.example.server.Models.Interview;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class InterviewsPerMonthMetricStrategy implements EmployeeMetricStrategy {

    @Override
    public String getMetricName() {
        return "Interviews Per Month";
    }

    @Override
    public double calculateMetric(List<Interview> interviews) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        return interviews.stream()
                .filter(interview -> interview.getDate() != null &&
                        interview.getDate().isAfter(oneMonthAgo) &&
                        interview.getDate().isBefore(now))
                .count();
    }
}