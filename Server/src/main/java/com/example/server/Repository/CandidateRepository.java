package com.example.server.Repository;

import com.example.server.Models.Candidate;
import com.example.server.Models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Integer> {
    @EntityGraph(attributePaths = {"resume"}, type = EntityGraph.EntityGraphType.LOAD)
    Optional<Candidate> findByUser(User user);

    @Modifying
    @Query("DELETE FROM Candidate c WHERE c.user = :user")
    void deleteByUser(@Param("user") User user);

}
