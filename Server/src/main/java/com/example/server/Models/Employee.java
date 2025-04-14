package com.example.server.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private int employeeId;

    private String position;
    private String department;

    @OneToOne
    @JoinColumn(name = "users_id", referencedColumnName = "users_id")
    @JsonIgnoreProperties("employee") // Игнорируем поле "employee" в User, чтобы избежать рекурсии
    private User user;
}