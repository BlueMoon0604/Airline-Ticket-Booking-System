package com.skybooker.auth.service;

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

import java.util.List;
import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    ApiResponse logout(String authHeader);

    TokenValidationResponse validateToken(String authHeader);

    AuthResponse refreshToken(RefreshTokenRequest request);

    OAuth2LoginResponse oauth2LoginInfo();

    UserResponse getProfile(String authHeader);

    UserResponse getUserById(UUID userId);

    UserResponse updateProfile(String authHeader, UpdateProfileRequest request);

    ApiResponse changePassword(String authHeader, ChangePasswordRequest request);

    ApiResponse deactivateAccount(String authHeader);

    List<UserResponse> getAllUsers();

    List<UserResponse> getUsersByRole(Role role);

    ApiResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request);

    ApiResponse deleteUser(UUID userId);
}
