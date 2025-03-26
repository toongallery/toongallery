package com.example.toongallery.domain.auth.dto.request;

import jakarta.validation.constraints.*;
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
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하로 입력해야 합니다.")
    @Pattern(regexp = ".*\\d.*", message = "비밀번호는 숫자를 포함해야 합니다.")
    @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*", message = "비밀번호는 특수기호를 포함해야 합니다.")
    private String password;
    @NotBlank
    private String name;
    @NotBlank
    private String userRole;
    @NotNull
    private LocalDate birthDate;
    @NotBlank
    private String gender;
}
