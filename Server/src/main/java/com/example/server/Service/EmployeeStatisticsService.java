package com.example.server.Service;

import com.example.server.DTO.EmployeeTopResponse;
import com.example.server.Models.Employee;
import com.example.server.Models.Interview;
import com.example.server.Repository.InterviewRepository;
import com.example.server.Service.strategy.EmployeeMetricStrategy;
import com.example.server.Service.strategy.SuccessRateMetricStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeStatisticsService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeStatisticsService.class);

    private final InterviewRepository interviewRepository;
    private final SuccessRateMetricStrategy successRateMetricStrategy;

    public EmployeeStatisticsService(
            InterviewRepository interviewRepository,
            List<EmployeeMetricStrategy> metricStrategies
    ) {
        this.interviewRepository = interviewRepository;
        // Находим нужную стратегию из списка
        this.successRateMetricStrategy = (SuccessRateMetricStrategy) metricStrategies.stream()
                .filter(strategy -> strategy instanceof SuccessRateMetricStrategy)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("SuccessRateMetricStrategy not found"));
    }

    public EmployeeTopResponse getTopEmployee(LocalDateTime startDate) {
        logger.info("Fetching top employee for period starting: {}", startDate);
        List<Interview> interviews = interviewRepository.findByDateAfter(startDate);

        Map<Employee, List<Interview>> interviewsByEmployee = interviews.stream()
                .filter(interview -> interview.getVacancy() != null && interview.getVacancy().getEmployee() != null)
                .collect(Collectors.groupingBy(interview -> interview.getVacancy().getEmployee()));

        EmployeeTopResponse topEmployee = interviewsByEmployee.entrySet().stream()
                .map(entry -> {
                    Employee employee = entry.getKey();
                    double successRate = successRateMetricStrategy.calculateMetric(entry.getValue());
                    String photoUrl = employee.getUser().getPhoto() != null && !employee.getUser().getPhoto().isEmpty()
                            ? "http://localhost:1111/images/" + employee.getUser().getPhoto()
                            : "/default-photo.jpg";
                    return new EmployeeTopResponse(
                            employee.getUser().getLastName(),
                            employee.getUser().getFirstName(),
                            employee.getPosition(),
                            photoUrl,
                            Math.round(successRate)
                    );
                })
                .max(Comparator.comparingDouble(EmployeeTopResponse::getSuccessRate))
                .orElse(null);

        if (topEmployee == null) {
            logger.warn("No top employee found");
        } else {
            logger.info("Top employee found: {} {}", topEmployee.getLastName(), topEmployee.getFirstName());
        }

        return topEmployee;
    }
}
