// EmployeeController.java
package com.example.server.controllers;

import com.example.server.Models.Employee;
import com.example.server.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.server.DTO.EmployeeDTO;
import java.util.List;

@RestController
@RequestMapping("/api/about")
public class AboutController {

    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public List<EmployeeDTO> getAllEmployees() {
        List<EmployeeDTO> employees = employeeService.getAllEmployeesWithUserDetails();


        employees.forEach(employee -> {
            if (employee.getPhoto() != null && !employee.getPhoto().isEmpty()) {
                employee.setPhoto("http://localhost:1111/images/" + employee.getPhoto());
            }
        });

        return employees;
    }
}