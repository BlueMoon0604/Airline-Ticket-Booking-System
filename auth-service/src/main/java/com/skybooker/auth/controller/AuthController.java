package com.skybooker.auth.controller;

import com.skybooker.auth.dto.ApiResponse;
import com.skybooker.auth.dto.AuthResponse;
import com.skybooker.auth.dto.ChangePasswordRequest;
import com.skybooker.auth.dto.LoginRequest;
import com.skybooker.auth.dto.OAuth2LoginResponse;
import com.skybooker.auth.dto.RefreshTokenRequest;
import com.skybooker.auth.dto.RegisterRequest;
import com.skybooker.auth.dto.TokenValidationResponse;
import com.skybooker.auth.dto.UpdateProfileRequest;
import com.skybooker.auth.dto.UpdateUserStatusRequest;
import com.skybooker.auth.dto.UserResponse;
import com.skybooker.auth.entity.Role;
import com.skybooker.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Authentication and user profile APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Login using email and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Logout current user")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return ResponseEntity.ok(authService.logout(authHeader));
    }

    @Operation(summary = "Validate JWT token")
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return ResponseEntity.ok(authService.validateToken(authHeader));
    }

    @Operation(summary = "Refresh JWT access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "Get Google OAuth2 login entry point")
    @GetMapping("/oauth2/login-info")
    public ResponseEntity<OAuth2LoginResponse> oauth2LoginInfo() {
        return ResponseEntity.ok(authService.oauth2LoginInfo());
    }

    @Operation(summary = "Get logged-in user profile")
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return ResponseEntity.ok(authService.getProfile(authHeader));
    }

    @Operation(summary = "Update logged-in user profile")
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(authService.updateProfile(authHeader, request));
    }

    @Operation(summary = "Change user password")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(authService.changePassword(authHeader, request));
    }

    @Operation(summary = "Deactivate logged-in account")
    @PutMapping("/deactivate")
    public ResponseEntity<ApiResponse> deactivate(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) {
        return ResponseEntity.ok(authService.deactivateAccount(authHeader));
    }

    @Operation(summary = "Get all users")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(@RequestParam(required = false) Role role) {
        return ResponseEntity.ok(role == null ? authService.getAllUsers() : authService.getUsersByRole(role));
    }

    @Operation(summary = "Get user by id")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.getUserById(userId));
    }

    @Operation(summary = "Suspend or reactivate user")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ResponseEntity.ok(authService.updateUserStatus(userId, request));
    }

    @Operation(summary = "Delete user permanently")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.deleteUser(userId));
    }
}
