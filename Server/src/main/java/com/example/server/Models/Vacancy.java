package com.example.server.Models;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "vacancies")
public class Vacancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer vacancies_id;

    @Column(name = "position_title")
    private String position;
    private String department;
    private String requirements;
    private String description;
    private String status;
    private Double salary;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
