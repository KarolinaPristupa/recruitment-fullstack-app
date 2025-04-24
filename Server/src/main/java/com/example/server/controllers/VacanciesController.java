package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Models.Vacancy;
import com.example.server.Service.VacancyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vacancies")
public class VacanciesController {

    private final VacancyService vacancyService;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(VacanciesController.class);

    public VacanciesController(VacancyService vacancyService, JwtUtil jwtUtil) {
        this.vacancyService = vacancyService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<Vacancy> getAllVacancies() {
        List<Vacancy> vacancies = vacancyService.getAllVacancies();
        logger.info("Отправляем вакансии: {}", vacancies.size());
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
            logger.error("Ошибка при удалении вакансии: {}", e.getMessage(), e);
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
            logger.error("Ошибка при обновлении вакансии: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/match")
    public ResponseEntity<?> matchVacancy(@RequestHeader("Authorization") String authHeader) {
        try {
            logger.info("Получен запрос на подбор вакансии");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Токен отсутствует или неверный формат: {}", authHeader);
                return ResponseEntity.status(401).body(Map.of("error", "Токен отсутствует или неверный формат"));
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            if (!jwtUtil.isTokenValid(token, email)) {
                logger.warn("Недействительный или просроченный токен для email: {}", email);
                return ResponseEntity.status(401).body(Map.of("error", "Недействительный или просроченный токен"));
            }

            Vacancy matchedVacancy = vacancyService.findBestMatchingVacancy(email);
            if (matchedVacancy == null) {
                logger.info("Подходящих вакансий не найдено для email: {}", email);
                return ResponseEntity.ok(null); // Возвращаем null, если нет подходящих вакансий
            }

            logger.info("Найдена подходящая вакансия: position={}", matchedVacancy.getPosition());
            return ResponseEntity.ok(matchedVacancy);
        } catch (RuntimeException e) {
            logger.error("Ошибка при подборе вакансии: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Внутренняя ошибка сервера при подборе вакансии: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Внутренняя ошибка сервера"));
        }
    }
}