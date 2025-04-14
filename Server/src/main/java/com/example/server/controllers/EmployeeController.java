package com.example.server.controllers;

import com.example.server.Models.Employee;
import com.example.server.Models.User;
import com.example.server.Service.EmployeeService;
import com.example.server.JWT.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final JwtUtil jwtUtil;

    public EmployeeController(EmployeeService employeeService, JwtUtil jwtUtil) {
        this.employeeService = employeeService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<Employee> getProfile(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtUtil.extractUsername(token.substring(7));
        Employee employee = employeeService.getEmployeeByEmail(email);

        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        User user = employee.getUser();
        if (user != null && user.getPhoto() != null && !user.getPhoto().isEmpty()) {
            user.setPhoto("http://localhost:1111/images/" + user.getPhoto());
        }

        return ResponseEntity.ok(employee);
    }
}
