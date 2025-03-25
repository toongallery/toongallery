package com.example.toongallery.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;



@Getter
public class UserUpdateRequest {

    @Email
    private String email;

    private String name;

    private String gender;
}
