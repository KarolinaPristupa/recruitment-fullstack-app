package com.example.server.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password;
    private String photo;
}
