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
import com.skybooker.auth.entity.AuthProvider;
import com.skybooker.auth.entity.Role;
import com.skybooker.auth.entity.User;
import com.skybooker.auth.exception.BadRequestException;
import com.skybooker.auth.exception.ResourceNotFoundException;
import com.skybooker.auth.exception.UnauthorizedException;
import com.skybooker.auth.repository.UserRepository;
import com.skybooker.auth.security.JwtService;
import com.skybooker.auth.security.TokenBlacklistService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new BadRequestException("Phone already registered");
        }
        if (request.passportNumber() != null && !request.passportNumber().isBlank()
                && userRepository.existsByPassportNumber(request.passportNumber())) {
            throw new BadRequestException("Passport number already registered");
        }

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setPassportNumber(blankToNull(request.passportNumber()));
        user.setNationality(blankToNull(request.nationality()));
        user.setRole(request.role() == null ? Role.PASSENGER : request.role());
        user.setProvider(AuthProvider.LOCAL);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is deactivated");
        }

        return buildAuthResponse(user);
    }

    @Override
    public ApiResponse logout(String authHeader) {
        String token = validateBearerHeader(authHeader);
        tokenBlacklistService.blacklist(token, jwtService.extractExpiration(token));
        return new ApiResponse("Logout successful");
    }

    @Override
    public TokenValidationResponse validateToken(String authHeader) {
        String token = validateBearerHeader(authHeader);
        if (tokenBlacklistService.isBlacklisted(token)) {
            return new TokenValidationResponse(false, null, null, null, "Token has been logged out");
        }

        UUID userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean valid = jwtService.isTokenValid(token, user) && Boolean.TRUE.equals(user.getIsActive());
        if (!valid) {
            return new TokenValidationResponse(false, user.getUserId(), user.getEmail(), user.getRole(), "Invalid token");
        }
        return new TokenValidationResponse(true, user.getUserId(), user.getEmail(), user.getRole(), "Token is valid");
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();
        if (tokenBlacklistService.isBlacklisted(refreshToken)) {
            throw new UnauthorizedException("Refresh token has been logged out");
        }
        if (jwtService.isTokenExpired(refreshToken) || !"REFRESH".equals(jwtService.extractTokenType(refreshToken))) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        User user = userRepository.findById(jwtService.extractUserId(refreshToken))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Account is deactivated");
        }

        return buildAuthResponse(user);
    }

    @Override
    public OAuth2LoginResponse oauth2LoginInfo() {
        return new OAuth2LoginResponse(
                "Open the Google OAuth2 authorization URL to continue login",
                "/oauth2/authorization/google",
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String authHeader) {
        return mapToResponse(getUserFromHeader(authHeader));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    @Override
    public UserResponse updateProfile(String authHeader, UpdateProfileRequest request) {
        User user = getUserFromHeader(authHeader);

        String newFullName = blankToNull(request.fullName());
        String newPhone = blankToNull(request.phone());
        String newPassport = blankToNull(request.passportNumber());
        String newNationality = blankToNull(request.nationality());

        if (newPhone != null && !newPhone.equals(user.getPhone()) && userRepository.existsByPhone(newPhone)) {
            throw new BadRequestException("Phone already registered");
        }

        if (newPassport != null && !newPassport.equals(user.getPassportNumber())
                && userRepository.existsByPassportNumber(newPassport)) {
            throw new BadRequestException("Passport number already registered");
        }

        if (newFullName != null) {
            user.setFullName(newFullName);
        }
        user.setPhone(newPhone);
        user.setPassportNumber(newPassport);
        user.setNationality(newNationality);

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public ApiResponse changePassword(String authHeader, ChangePasswordRequest request) {
        User user = getUserFromHeader(authHeader);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return new ApiResponse("Password changed successfully");
    }

    @Override
    public ApiResponse deactivateAccount(String authHeader) {
        User user = getUserFromHeader(authHeader);
        user.setIsActive(false);
        userRepository.save(user);
        return new ApiResponse("Account deactivated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        return userRepository.findAllByRole(role)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ApiResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(request.active());
        userRepository.save(user);
        return new ApiResponse("User status updated successfully", mapToResponse(user));
    }

    @Override
    public ApiResponse deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepository.delete(user);
        return new ApiResponse("User deleted successfully");
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getProvider(),
                user.getIsActive(),
                user.getPassportNumber(),
                user.getNationality(),
                user.getCreatedAt()
        );
    }

    private User getUserFromHeader(String authHeader) {
        String token = validateBearerHeader(authHeader);
        UUID userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (tokenBlacklistService.isBlacklisted(token) || !jwtService.isTokenValid(token, user)) {
            throw new UnauthorizedException("Invalid token");
        }
        return user;
    }

    private String validateBearerHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization header missing or invalid");
        }
        return authHeader.substring(7);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}

