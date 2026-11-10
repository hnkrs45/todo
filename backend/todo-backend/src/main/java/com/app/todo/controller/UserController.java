package com.app.todo.controller;

import com.app.todo.dto.response.ApiResponse;
import com.app.todo.dto.response.UserProfileResponse;
import com.app.todo.security.CustomUserDetails;
import com.app.todo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(userService.getProfile(currentUser.getId()));
    }

    @PostMapping("/upgrade")
    public ResponseEntity<ApiResponse> upgradePlan(
            @RequestParam String plan,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        userService.upgradePlan(currentUser.getId(), plan);
        return ResponseEntity.ok(new ApiResponse(true, "Plan upgraded successfully to " + plan.toUpperCase()));
    }

    // Example admin-only endpoint
    @GetMapping("/admin/ping")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> adminPing() {
        return ResponseEntity.ok(new ApiResponse(true, "Admin route accessed successfully."));
    }
}