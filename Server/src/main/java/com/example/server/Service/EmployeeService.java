package com.example.server.Service;

import com.example.server.DTO.EmployeeDTO;
import com.example.server.Models.Employee;
import com.example.server.Repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<EmployeeDTO> getAllEmployeesWithUserDetails() {
        List<Employee> employees = employeeRepository.findAllWithUser();
        return employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setPosition(employee.getPosition());
        dto.setDepartment(employee.getDepartment());

        if (employee.getUser() != null) {
            dto.setFirstName(employee.getUser().getFirstName());
            dto.setLastName(employee.getUser().getLastName());
            dto.setPhone(employee.getUser().getPhone());
            dto.setPhoto(employee.getUser().getPhoto());
        }

        return dto;
    }

    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByUserEmail(email).orElse(null);
    }
}