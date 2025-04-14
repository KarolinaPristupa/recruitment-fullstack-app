package com.example.server.Repository;

import com.example.server.Models.Candidate;
import com.example.server.Models.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Integer> {
}
