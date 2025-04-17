package com.example.server.Repository;

import com.example.server.Models.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InterviewRepository extends JpaRepository<Interview, Integer> {
    @Query("SELECT i FROM Interview i " +
            "JOIN i.candidate c JOIN c.user cu " +
            "JOIN i.vacancy v JOIN v.employee e JOIN e.user eu " +
            "WHERE cu.email = :email OR eu.email = :email")
    List<Interview> findByUserEmail(@Param("email") String email);
}