package com.example.server.Service.strategy;

import com.example.server.Models.Interview;
import com.example.server.Models.Response;
import com.example.server.Repository.ResponseRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResponsesCountMetricStrategy implements EmployeeMetricStrategy {

    private final ResponseRepository responseRepository;

    public ResponsesCountMetricStrategy(ResponseRepository responseRepository) {
        this.responseRepository = responseRepository;
    }

    @Override
    public String getMetricName() {
        return "Responses Count";
    }

    @Override
    public double calculateMetric(List<Interview> interviews) {
        if (interviews.isEmpty()) {
            return 0.0;
        }
        Integer vacancyId = interviews.get(0).getVacancy().getVacancies_id();
        return responseRepository.countByVacancyId(vacancyId);
    }
}