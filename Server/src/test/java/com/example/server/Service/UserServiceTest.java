package com.example.server.Service;

import com.example.server.DTO.RegistrationDTO;
import com.example.server.JWT.JwtUtil;
import com.example.server.Models.User;
import com.example.server.Repository.UserRepository;
import com.example.server.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsJwtAndRole() {
        String email = "test@example.com";
        String password = "pass123";
        String hashed = HashUtil.hashString(password);
        User user = new User();
        user.setEmail(email);
        user.setHashPassword(hashed);
        user.setRole("Кандидат");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(email, "Кандидат")).thenReturn("mocked-token");

        Optional<Map<String, String>> result = userService.authenticateUser(email, password);

        assertTrue(result.isPresent());
        assertEquals("mocked-token", result.get().get("jwt"));
    }

    @Test
    void registerUser_AlreadyExists_ReturnsErrorMessage() {
        RegistrationDTO dto = new RegistrationDTO();
        dto.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        String result = userService.registerUser(dto, null);
        assertEquals("Пользователь с таким email уже существует", result);
    }

    @Test
    void updatePassword_UserExists_UpdatesPassword() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        boolean result = userService.updatePassword("test@example.com", "newPassword");

        assertTrue(result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser_ValidToken_UpdatesFields() {
        String token = "token";
        String email = "test@example.com";

        User user = new User();
        user.setEmail(email);

        User updated = new User();
        updated.setFirstName("John");

        when(jwtUtil.extractUsername(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> result = userService.updateUser(token, updated);

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void uploadUserPhoto_ValidPhoto_SavesFileAndUpdatesUser() {
        String email = "photo@example.com";
        User user = new User();
        user.setEmail(email);

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", "fake-image-content".getBytes());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        String result = userService.uploadUserPhoto(email, file);

        assertNotNull(result);
        verify(userRepository, times(1)).save(user);
    }
}
