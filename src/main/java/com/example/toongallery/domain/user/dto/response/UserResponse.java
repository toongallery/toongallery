package com.example.toongallery.domain.user.dto.response;

import com.example.toongallery.domain.user.entity.User;
import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.enums.UserRole;
import lombok.Getter;


import java.time.LocalDate;

@Getter
public class UserResponse{
    private final Long id;
    private final String email;
    private final String name;
    private final LocalDate birthDate;
    private final Gender gender;
    private final UserRole userRole;

    public UserResponse(Long id, String email, String name, LocalDate birthDate, Gender gender, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.userRole = userRole;
    }

    public static UserResponse of(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getBirthDate(), user.getGender(), user.getUserRole());
    }
}
