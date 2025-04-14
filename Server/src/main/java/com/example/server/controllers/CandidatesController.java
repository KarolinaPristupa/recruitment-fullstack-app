package com.example.server.controllers;

import com.example.server.Models.Candidate;
import com.example.server.Service.CandidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
public class CandidatesController {
    private final CandidateService candidateService;

    @GetMapping
    public ResponseEntity<List<Candidate>> getAllCandidates() {
        List<Candidate> candidates = candidateService.getAllCandidates();

        for (Candidate candidate : candidates) {
            if (candidate.getUser() != null && candidate.getUser().getPhoto() != null
                    && !candidate.getUser().getPhoto().isEmpty()) {
                candidate.getUser().setPhoto("http://localhost:1111/images/" + candidate.getUser().getPhoto());
            }
        }

        return ResponseEntity.ok(candidates);
    }
}
