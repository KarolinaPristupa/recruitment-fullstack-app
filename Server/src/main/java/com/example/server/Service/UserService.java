package com.example.server.Service;

import com.example.server.DTO.RegistrationDTO;
import com.example.server.JWT.JwtUtil;
import com.example.server.Models.User;
import com.example.server.Repository.UserRepository;
import com.example.server.HashUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String IMAGE_DIR = "C:/Desktop/Учеба/6 СЕМ/recruitment/Server/src/main/resources/static/images/";
    public Optional<Map<String, String>> authenticateUser(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String hashedPassword = HashUtil.hashString(password);

            if (user.getPassword().equals(hashedPassword)) {
                String token = jwtUtil.generateToken(email, user.getRole()); // Передаем роль
                return Optional.of(Map.of("jwt", token, "role", user.getRole()));
            }
        }
        return Optional.empty();
    }


    public String registerUser(RegistrationDTO request, MultipartFile photoFile) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "Пользователь с таким email уже существует";
        }

        String fileName = null;
        if (photoFile != null && !photoFile.isEmpty()) {
            fileName = savePhoto(photoFile);
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setHashPassword(HashUtil.hashString(request.getPassword()));
        user.setRole("Кандидат");
        user.setPhoto(fileName);

        userRepository.save(user);
        return "Пользователь зарегистрирован успешно";
    }

    private String savePhoto(MultipartFile file) {
        try {
            String extension = getFileExtension(file.getOriginalFilename());
            String newFileName = UUID.randomUUID().toString() + "." + extension;
            File destination = new File(IMAGE_DIR + newFileName);

            file.transferTo(destination);
            return newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении фото", e);
        }
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    public boolean updatePassword(String email, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setHashPassword(HashUtil.hashString(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Optional<User> updateUser(String token, User updatedUser) {
        String email = jwtUtil.extractUsername(token);
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (updatedUser.getFirstName() != null) user.setFirstName(updatedUser.getFirstName());
            if (updatedUser.getLastName() != null) user.setLastName(updatedUser.getLastName());
            if (updatedUser.getPhone() != null) user.setPhone(updatedUser.getPhone());

            // Важно: записываем в БД только имя файла!
            if (updatedUser.getPhoto() != null && !updatedUser.getPhoto().startsWith("http")) {
                user.setPhoto(updatedUser.getPhoto());
            }

            userRepository.save(user);
            return Optional.of(user);
        }

        return Optional.empty();
    }


    public String uploadUserPhoto(String email, MultipartFile file) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new EntityNotFoundException("Пользователь не найден");
        }

        try {
            if (file == null || file.isEmpty()) {
                System.out.println("Ошибка: файл пустой или null!");
                throw new RuntimeException("Файл не был загружен");
            }

            System.out.println("Загружаем фото для пользователя: " + email);
            System.out.println("Оригинальное имя файла: " + file.getOriginalFilename());

            String extension = getFileExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + "." + extension;
            File destination = new File(IMAGE_DIR + fileName);

            System.out.println("Сохраняем файл как: " + destination.getAbsolutePath());
            file.transferTo(destination);

            User user = optionalUser.get();
            user.setPhoto(fileName);
            userRepository.save(user);

            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при сохранении фото", e);
        }
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
