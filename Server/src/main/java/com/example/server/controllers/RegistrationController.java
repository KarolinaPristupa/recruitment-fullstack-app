package com.example.server.controllers;

import com.example.server.DTO.RegistrationDTO;
import com.example.server.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class RegistrationController {
    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<?> registerUser(
            @RequestPart("user") RegistrationDTO request,
            @RequestPart(value = "photo", required = false) MultipartFile photoFile) {

        String response = userService.registerUser(request, photoFile);

        if (response.equals("Пользователь с таким email уже существует")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
