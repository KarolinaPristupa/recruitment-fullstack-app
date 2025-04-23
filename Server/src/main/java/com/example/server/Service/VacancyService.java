package com.example.server.Service;
import com.example.server.Models.Employee;
import com.example.server.Models.User;
import com.example.server.Models.Vacancy;
import com.example.server.Repository.EmployeeRepository;
import com.example.server.Repository.UserRepository;
import com.example.server.Repository.VacancyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VacancyService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);
    @Autowired
    private VacancyRepository vacancyRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;
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
                .orElseThrow(() -> new EntityNotFoundException("Vacancy not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User with email not found"));

        Employee employee = employeeRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employee not found for this user"));

        if (!vacancy.getEmployee().equals(employee)) {
            throw new RuntimeException("This user is not the owner of the vacancy");
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

    public Vacancy findByPosition(String position){
        return vacancyRepository.findByPosition(position)
                .orElseThrow(() -> {
            logger.error("Vacancy not found: vacancyName={}", position);
            return new RuntimeException("Vacancy not found");
        });
    }
}

