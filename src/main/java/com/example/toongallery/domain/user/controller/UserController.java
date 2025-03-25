package com.example.toongallery.domain.user.controller;

import com.example.toongallery.domain.common.dto.AuthUser;
import com.example.toongallery.domain.user.dto.request.PasswordChangeRequest;
import com.example.toongallery.domain.user.dto.request.UserUpdateRequest;
import com.example.toongallery.domain.user.dto.response.UserResponse;
import com.example.toongallery.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @GetMapping("/myinfo")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.getUser(authUser.getUserId()));
    }

    @PatchMapping("/users")
    public ResponseEntity<UserResponse> updateUser(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(authUser.getUserId(), request));
    }

    @PatchMapping("/users/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody PasswordChangeRequest request
    ) {
        userService.updatePassword(authUser.getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal AuthUser authUser) {
        userService.deleteUser(authUser.getUserId());
        return ResponseEntity.ok().build();
    }
}
