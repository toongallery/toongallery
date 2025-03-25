package com.example.toongallery.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordChangeRequest {
    @NotBlank
    private String oldPassword;
    @NotBlank
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하로 입력해야 합니다.")
    @Pattern(regexp = ".*\\d.*", message = "비밀번호는 숫자를 포함해야 합니다.")
    @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*", message = "비밀번호는 특수기호를 포함해야 합니다.")
    private String newPassword;
    @NotBlank
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하로 입력해야 합니다.")
    @Pattern(regexp = ".*\\d.*", message = "비밀번호는 숫자를 포함해야 합니다.")
    @Pattern(regexp = ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*", message = "비밀번호는 특수기호를 포함해야 합니다.")
    private String newCheckPassword;
}
