package com.example.server.Repository;
import com.example.server.Models.Vacancy;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VacancyRepository extends JpaRepository<Vacancy, Integer> {
    @Query("SELECT v FROM Vacancy v JOIN FETCH v.employee")
    List<Vacancy> findAllWithEmployee();

    Optional<Vacancy> findByPosition(String position);

}

