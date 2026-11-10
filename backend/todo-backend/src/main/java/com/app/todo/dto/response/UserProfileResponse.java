package com.app.todo.dto.response;

import com.app.todo.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String id;
    private String username;
    private String name;
    private String email;
    private Set<Role> roles;
    private boolean isVerified;
    private String subscriptionPlan;
    private long totalTodos;
    private Instant createdAt;
}