package com.example.server.Service.strategy;
import com.example.server.Models.Interview;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SuccessRateMetricStrategy implements EmployeeMetricStrategy {
    @Override
    public String getMetricName() {
        return "successRate";
    }

    @Override
    public double calculateMetric(List<Interview> interviews) {
        if (interviews.isEmpty()) {
            return 0.0;
        }
        long totalInterviews = interviews.size();
        long successfulInterviews = interviews.stream()
                .filter(interview -> "Принят".equals(interview.getResult()))
                .count();
        return (double) successfulInterviews / totalInterviews * 100.0;
    }
}
