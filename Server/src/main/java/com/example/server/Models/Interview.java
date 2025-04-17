package com.example.server.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interview_id")
    @JsonProperty("id")
    private Integer interviewId;

    @Column(name = "date")
    private LocalDateTime date;

    @Column(name = "result")
    @JsonIgnore
    private String result; // Принят, Отклонён, Ожидание

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancies_id")
    @JsonIgnore
    private Vacancy vacancy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    @JsonIgnore
    private Candidate candidate;

    @JsonProperty("position")
    public String getPosition() {
        return vacancy != null ? vacancy.getPosition() : null;
    }

    @JsonProperty("hrFirstName")
    public String getHrFirstName() {
        return vacancy != null && vacancy.getEmployee() != null && vacancy.getEmployee().getUser() != null
                ? vacancy.getEmployee().getUser().getFirstName()
                : null;
    }

    @JsonProperty("hrLastName")
    public String getHrLastName() {
        return vacancy != null && vacancy.getEmployee() != null && vacancy.getEmployee().getUser() != null
                ? vacancy.getEmployee().getUser().getLastName()
                : null;
    }

    @JsonProperty("candidateFirstName")
    public String getCandidateFirstName() {
        return candidate != null && candidate.getUser() != null
                ? candidate.getUser().getFirstName()
                : null;
    }

    @JsonProperty("candidateLastName")
    public String getCandidateLastName() {
        return candidate != null && candidate.getUser() != null
                ? candidate.getUser().getLastName()
                : null;
    }
}