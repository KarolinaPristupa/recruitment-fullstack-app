package com.example.server.Service.strategy;
import com.example.server.Models.Interview;

import java.util.List;

public interface EmployeeMetricStrategy {
    String getMetricName();
    double calculateMetric(List<Interview> interviews);
}
