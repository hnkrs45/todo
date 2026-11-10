package com.app.todo.service;

import com.app.todo.dto.response.UserProfileResponse;
import com.app.todo.model.User;
import com.app.todo.repository.TodoRepository;
import com.app.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;

    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        long totalTodos = todoRepository.findByUserId(userId).size();

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .isVerified(user.isVerified())
                .subscriptionPlan(user.getSubscriptionPlan())
                .totalTodos(totalTodos)
                .createdAt(user.getCreatedAt())
                .build();
    }

    public void upgradePlan(String userId, String plan) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setSubscriptionPlan(plan.toUpperCase());
        userRepository.save(user);
    }
}