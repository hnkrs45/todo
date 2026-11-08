package com.app.todo.service;

import com.app.todo.dto.request.LoginRequest;
import com.app.todo.dto.request.RegisterRequest;
import com.app.todo.dto.response.ApiResponse;
import com.app.todo.dto.response.AuthResponse;
import com.app.todo.model.Role;
import com.app.todo.model.User;
import com.app.todo.repository.UserRepository;
import com.app.todo.security.CustomUserDetails;
import com.app.todo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    public ApiResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return new ApiResponse(false, "Username is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return new ApiResponse(false, "Email is already registered!");
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername().trim().toLowerCase())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.ROLE_USER))
                .isVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(Instant.now().plus(24, ChronoUnit.HOURS))
                .subscriptionPlan("FREE")
                .build();

        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return new ApiResponse(true, "Registration successful! Please check your email to verify your account.");
    }

    public ApiResponse verifyAccount(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElse(null);

        if (user == null) {
            return new ApiResponse(false, "Invalid verification token.");
        }

        if (user.getVerificationTokenExpiry() != null && user.getVerificationTokenExpiry().isBefore(Instant.now())) {
            return new ApiResponse(false, "Verification token has expired. Please request a new one.");
        }

        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        return new ApiResponse(true, "Account verified successfully. You can now log in.");
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by either username or email to extract correct username for spring security principal
        User existingUser = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Invalid username/email or password"));

        if (!existingUser.isVerified()) {
            throw new DisabledException("Account is not verified. Please verify your email before logging in.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        existingUser.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .roles(roles)
                .build();
    }
}