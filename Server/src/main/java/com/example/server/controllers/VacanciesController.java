package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Models.Vacancy;
import com.example.server.Service.VacancyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vacancies")
public class VacanciesController {

    private VacancyService vacancyService;
    private final JwtUtil jwtUtil;

    public VacanciesController(VacancyService vacancyService, JwtUtil jwtUtil) {
        this.vacancyService = vacancyService;
        this.jwtUtil = jwtUtil;
    }
    private static final Logger logger = LoggerFactory.getLogger(VacanciesController.class);
    @GetMapping
    public List<Vacancy> getAllVacancies() {
        List<Vacancy> vacancies = vacancyService.getAllVacancies();
        System.out.println("Отправляем вакансии: " + vacancies);
        return vacancies;
    }

    @PostMapping
    public ResponseEntity<?> createVacancy(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody Map<String, Object> vacancyData) {
        try {
            logger.info("Получен запрос на создание вакансии: position_title={}", vacancyData.get("position_title"));

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Токен отсутствует или неверный формат: {}", authHeader);
                return ResponseEntity.status(401).body("Токен отсутствует или неверный формат");
            }

            String token = authHeader.substring(7);
            logger.debug("Извлечен токен: {}", token);

            String email = jwtUtil.extractUsername(token);
            logger.debug("Извлечен email из токена: {}", email);

            if (!jwtUtil.isTokenValid(token, email)) {
                logger.warn("Недействительный или просроченный токен для email: {}", email);
                return ResponseEntity.status(401).body("Недействительный или просроченный токен");
            }

            String emailFromBody = (String) vacancyData.get("email");
            if (!email.equals(emailFromBody)) {
                logger.warn("Email в токене ({}) не совпадает с email в теле запроса ({})", email, emailFromBody);
                return ResponseEntity.status(403).body(Map.of("success", false, "error", "Несанкционированный доступ"));
            }

            Vacancy vacancy = new Vacancy();
            vacancy.setPosition((String) vacancyData.get("position_title"));
            vacancy.setDepartment((String) vacancyData.get("department"));
            vacancy.setRequirements((String) vacancyData.get("requirements"));
            vacancy.setDescription((String) vacancyData.get("description"));
            vacancy.setStatus((String) vacancyData.get("status"));
            vacancy.setSalary(Double.valueOf(vacancyData.get("salary").toString()));

            vacancyService.createVacancyWithEmployeeEmail(emailFromBody, vacancy);

            logger.info("Вакансия успешно создана: position_title={}", vacancy.getPosition());
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Ошибка при создании вакансии: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }


    @DeleteMapping("/vacancies")
    public ResponseEntity<?> deleteVacancy(@RequestBody Map<String, Object> data) {
        try {
            String email = (String) data.get("email");
            Integer id = Integer.parseInt(data.get("id").toString());

            vacancyService.deleteVacancyByEmailAndId(email, id);

            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/vacancies")
    public ResponseEntity<?> updateVacancy(@RequestBody Map<String, Object> vacancyData) {
        try {
            String email = (String) vacancyData.get("email");
            Integer vacancyId = Integer.parseInt(vacancyData.get("id").toString());

            Vacancy vacancy = new Vacancy();
            vacancy.setVacancies_id(vacancyId);
            vacancy.setPosition((String) vacancyData.get("position_title"));
            vacancy.setDepartment((String) vacancyData.get("department"));
            vacancy.setRequirements((String) vacancyData.get("requirements"));
            vacancy.setDescription((String) vacancyData.get("description"));
            vacancy.setStatus((String) vacancyData.get("status"));
            vacancy.setSalary(Double.valueOf(vacancyData.get("salary").toString()));

            vacancyService.updateVacancyWithEmployeeEmail(email, vacancy);

            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }


}
