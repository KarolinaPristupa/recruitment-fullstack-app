package com.example.server.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "responses")
public class Response {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer responseId;

    @ManyToOne
    @JoinColumn(name = "candidate_id", referencedColumnName = "candidate_id")
    private Candidate candidate;

    @ManyToOne
    @JoinColumn(name = "vacancy_id", referencedColumnName = "vacancies_id")
    private Vacancy vacancy;

    @OneToOne
    @JoinColumn(name = "notification_id", referencedColumnName = "notification_id")
    private Notification notification;
}