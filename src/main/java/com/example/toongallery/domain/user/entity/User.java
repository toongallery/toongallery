package com.example.toongallery.domain.user.entity;

import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.common.entity.BaseEntity;
import com.example.toongallery.domain.user.enums.Gender;
import com.example.toongallery.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at is null")
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private String name;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private LocalDateTime deletedAt;

    public User(String email, String password, String name, LocalDate birthDate, Gender gender, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.userRole = userRole;
        this.deletedAt = null;
    }

    private User(Long id, String email, UserRole userRole) {
        this.id = id;
        this.email = email;
        this.userRole = userRole;
    }

    public static User fromAuthUser(AuthUser authUser) {
        return new User(authUser.getUserId(), authUser.getEmail(), authUser.getUserRole());
    }


    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateGender(Gender gender) {
        this.gender = gender;
    }

    public void changePassword(String encodePassword) {
        this.password = encodePassword;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
