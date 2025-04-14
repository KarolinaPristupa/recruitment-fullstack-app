package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Models.Candidate;
import com.example.server.Models.Resume;
import com.example.server.Models.User;
import com.example.server.Service.CandidateService;
import com.example.server.Service.ResumeService;
import com.example.server.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/candidate")
@RequiredArgsConstructor
public class CandidateController {
    private final CandidateService candidateService;
    private final UserService userService;
    private final ResumeService resumeService;
    private final JwtUtil jwtUtil;

    @GetMapping()
    public ResponseEntity<Candidate> getProfile(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = jwtUtil.extractUsername(token.substring(7));

        Candidate candidate = candidateService.getCandidateProfileOrEmpty(email);

        if (candidate.getResume() == null) {
            candidate.setResume(new Resume()); // Создаём пустое резюме, если его нет
        }

        if (candidate.getUser() != null && candidate.getUser().getPhoto() != null
                && !candidate.getUser().getPhoto().isEmpty()) {
            candidate.getUser().setPhoto("http://localhost:1111/images/" + candidate.getUser().getPhoto());
        }

        return ResponseEntity.ok(candidate);
    }

    @PostMapping("/resume")
    public ResponseEntity<?> createResume(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> resumeData) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        String email = jwtUtil.extractUsername(token.substring(7));

        try {
            Candidate candidate = candidateService.getOrCreateCandidate(email);

            Resume resume = resumeService.createResume(candidate, resumeData);

            return ResponseEntity.status(HttpStatus.CREATED).body(candidate);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PutMapping("/resume")
    public ResponseEntity<?> updateResume(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> resumeData) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        String email = jwtUtil.extractUsername(token.substring(7));

        System.out.println("Updating resume for email: " + email);

        try {
            Candidate candidate = candidateService.getOrCreateCandidate(email); // Гарантируем, что кандидат есть
            Candidate updatedCandidate = resumeService.updateResume(email, resumeData);
            System.out.println("Updated candidate: " + updatedCandidate);
            return ResponseEntity.ok(updatedCandidate);
        } catch (EntityNotFoundException ex) {
            System.out.println("Candidate not found for email: " + email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @DeleteMapping("/resume")
    public ResponseEntity<?> deleteResume(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        String email = jwtUtil.extractUsername(token.substring(7));

        try {
            candidateService.deleteCandidateWithResume(email);
            return ResponseEntity.ok("Резюме и связанный кандидат удалены");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestHeader("Authorization") String authHeader, @RequestBody User user) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authHeader.substring(7);
        Optional<User> updatedUser = userService.updateUser(token, user);
        return updatedUser.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadPhoto(@RequestHeader("Authorization") String authHeader,
                                         @RequestParam("photo") MultipartFile file) {
        if (!authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);

        try {
            String fileName = userService.uploadUserPhoto(email, file);
            return ResponseEntity.ok(Map.of("fileName", fileName));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка загрузки файла");
        }
    }
}
