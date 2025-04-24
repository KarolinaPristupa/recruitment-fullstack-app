package com.example.server.Repository;

import com.example.server.Models.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ResponseRepository extends JpaRepository<Response, Integer> {
    Optional<Response> findByNotification_NotificationId(Integer notificationId);
    @Query("SELECT COUNT(v) FROM Vacancy v WHERE v.vacancies_id = ?1")
    double countByVacancyId(Integer vacancyId);
}