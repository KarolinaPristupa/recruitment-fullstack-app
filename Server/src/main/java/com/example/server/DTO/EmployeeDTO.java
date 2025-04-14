package com.example.server.DTO;


import lombok.Data;

@Data
public class EmployeeDTO {
    private int employeeId;
    private String position;
    private String department;
    private String firstName;
    private String lastName;
    private String phone;
    private String photo;
}