package org.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String displayName;
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
    private Integer age;
    private String phoneNumber;
}
