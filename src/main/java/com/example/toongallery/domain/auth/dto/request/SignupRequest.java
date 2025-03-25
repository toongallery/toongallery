package com.example.toongallery.domain.auth.dto.request;

import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String name;
    @NotBlank
    private String userRole;
    @NotNull
    private LocalDate birthDate;
    @NotBlank
    private String gender;
    @NotBlank
    private String userStatus;
}
