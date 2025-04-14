package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Models.User;
import com.example.server.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("username");
        String password = credentials.get("password");

        Optional<Map<String, String>> authResult = userService.authenticateUser(email, password);

        if (authResult != null) {
            return ResponseEntity.ok(authResult);
        } else {
            return ResponseEntity.status(401).body("Неверный email или пароль");
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");

        boolean isUpdated = userService.updatePassword(email, newPassword);
        if (isUpdated) {
            return ResponseEntity.ok("Пароль успешно изменен");
        } else {
            return ResponseEntity.status(400).body("Ошибка при изменении пароля");
        }
    }

}
