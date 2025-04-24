package com.example.server.Service;

import com.example.server.Models.Candidate;
import com.example.server.Models.Employee;
import com.example.server.Models.User;
import com.example.server.Models.Vacancy;
import com.example.server.Repository.CandidateRepository;
import com.example.server.Repository.EmployeeRepository;
import com.example.server.Repository.UserRepository;
import com.example.server.Repository.VacancyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VacancyService {

    private static final Logger logger = LoggerFactory.getLogger(VacancyService.class);

    @Autowired
    private VacancyRepository vacancyRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    public List<Vacancy> getAllVacancies() {
        return vacancyRepository.findAll();
    }

    public Vacancy getById(Integer id) {
        return vacancyRepository.findById(id).orElse(null);
    }

    public void createVacancyWithEmployeeEmail(String email, Vacancy vacancy) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким email не найден"));

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден для этого пользователя"));

        vacancy.setEmployee(employee);
        vacancyRepository.save(vacancy);
    }

    public void deleteVacancyByEmailAndId(String email, Integer vacancyId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new EntityNotFoundException("Вакансия не найдена"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким email не найден"));

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден для этого пользователя"));

        if (!vacancy.getEmployee().equals(employee)) {
            throw new RuntimeException("Этот пользователь не является владельцем вакансии");
        }

        vacancyRepository.delete(vacancy);
    }

    public void updateVacancyWithEmployeeEmail(String email, Vacancy vacancy) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с таким email не найден"));
        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден для этого пользователя"));

        Vacancy existingVacancy = vacancyRepository.findById(vacancy.getVacancies_id())
                .orElseThrow(() -> new RuntimeException("Вакансия с таким id не найдена"));

        if (!existingVacancy.getEmployee().equals(employee)) {
            throw new RuntimeException("Этот пользователь не является владельцем вакансии");
        }

        existingVacancy.setPosition(vacancy.getPosition());
        existingVacancy.setDepartment(vacancy.getDepartment());
        existingVacancy.setRequirements(vacancy.getRequirements());
        existingVacancy.setDescription(vacancy.getDescription());
        existingVacancy.setStatus(vacancy.getStatus());
        existingVacancy.setSalary(vacancy.getSalary());

        vacancyRepository.save(existingVacancy);
    }

    public Vacancy findById(Integer id) {
        return vacancyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));
    }

    public Vacancy findByPosition(String position) {
        return vacancyRepository.findByPosition(position)
                .orElseThrow(() -> {
                    logger.error("Вакансия не найдена: position={}", position);
                    return new RuntimeException("Вакансия не найдена");
                });
    }

    public Vacancy findBestMatchingVacancy(String email) {
        logger.info("Поиск подходящей вакансии для email: {}", email);

        // Находим кандидата по email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с email " + email + " не найден"));

        Candidate candidate = candidateRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Кандидат не найден для пользователя " + email));

        if (candidate.getResume() == null || candidate.getResume().getSkills() == null || candidate.getResume().getSkills().isEmpty()) {
            logger.warn("У кандидата отсутствует резюме или навыки: email={}", email);
            throw new RuntimeException("У кандидата отсутствует резюме или навыки");
        }

        // Получаем навыки кандидата
        String skills = candidate.getResume().getSkills().toLowerCase();
        Set<String> candidateSkills = Arrays.stream(skills.split("[,\\s]+"))
                .map(String::trim)
                .filter(skill -> !skill.isEmpty())
                .collect(Collectors.toSet());

        // Получаем только активные вакансии
        List<Vacancy> activeVacancies = vacancyRepository.findByStatus("Активно");
        if (activeVacancies.isEmpty()) {
            logger.info("Активные вакансии отсутствуют");
            return null;
        }

        Vacancy bestMatch = null;
        int maxMatches = -1;

        // Сравниваем навыки с требованиями каждой вакансии
        for (Vacancy vacancy : activeVacancies) {
            if (vacancy.getRequirements() == null || vacancy.getRequirements().isEmpty()) {
                continue;
            }

            String requirements = vacancy.getRequirements().toLowerCase();
            Set<String> requirementWords = Arrays.stream(requirements.split("[,\\s]+"))
                    .map(String::trim)
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.toSet());

            // Подсчитываем совпадения
            int matches = 0;
            for (String skill : candidateSkills) {
                if (requirementWords.contains(skill)) {
                    matches++;
                }
            }

            if (matches > maxMatches) {
                maxMatches = matches;
                bestMatch = vacancy;
            }
        }

        if (bestMatch == null) {
            logger.info("Подходящих вакансий не найдено для навыков: {}", skills);
        } else {
            logger.info("Найдена вакансия с {} совпадениями: position={}", maxMatches, bestMatch.getPosition());
        }

        return bestMatch;
    }
}