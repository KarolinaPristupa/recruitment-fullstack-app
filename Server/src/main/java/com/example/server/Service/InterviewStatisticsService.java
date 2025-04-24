package com.example.server.Service;

import com.example.server.Models.Interview;
import com.example.server.Models.Vacancy;
import com.example.server.Repository.InterviewRepository;
import com.example.server.Repository.VacancyRepository;
import com.example.server.Service.strategy.EmployeeMetricStrategy;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InterviewStatisticsService {

    private final InterviewRepository interviewRepository;
    private final VacancyRepository vacancyRepository;
    private final List<EmployeeMetricStrategy> metricStrategies;

    public InterviewStatisticsService(
            InterviewRepository interviewRepository,
            VacancyRepository vacancyRepository,
            List<EmployeeMetricStrategy> metricStrategies
    ) {
        this.interviewRepository = interviewRepository;
        this.vacancyRepository = vacancyRepository;
        this.metricStrategies = metricStrategies;
    }

    public byte[] generateInterviewStatisticsReport() throws IOException {
        List<Vacancy> vacancies = vacancyRepository.findAll();
        List<Interview> interviews = interviewRepository.findAll();

        Map<Integer, List<Interview>> interviewsByVacancy = interviews.stream()
                .collect(Collectors.groupingBy(interview -> interview.getVacancy().getVacancies_id()));

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Interview Statistics");

        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        headerRow.createCell(0).setCellValue("Vacancy ID");
        headerRow.createCell(1).setCellValue("Position");
        int columnIndex = 2;
        for (EmployeeMetricStrategy strategy : metricStrategies) {
            headerRow.createCell(columnIndex).setCellValue(strategy.getMetricName());
            columnIndex++;
        }

        for (int i = 0; i < columnIndex; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (Vacancy vacancy : vacancies) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(vacancy.getVacancies_id());
            row.createCell(1).setCellValue(vacancy.getPosition());

            List<Interview> vacancyInterviews = interviewsByVacancy.getOrDefault(vacancy.getVacancies_id(), List.of());
            columnIndex = 2;
            for (EmployeeMetricStrategy strategy : metricStrategies) {
                double metricValue = strategy.calculateMetric(vacancyInterviews);
                row.createCell(columnIndex++).setCellValue(metricValue);
            }
        }
        for (int i = 0; i < columnIndex; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}