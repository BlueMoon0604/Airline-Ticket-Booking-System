package com.skybooker.auth.service;

import com.skybooker.auth.dto.RefreshTokenRequest;
import com.skybooker.auth.dto.RegisterRequest;
import com.skybooker.auth.entity.AuthProvider;
import com.skybooker.auth.entity.Role;
import com.skybooker.auth.entity.User;
import com.skybooker.auth.exception.UnauthorizedException;
import com.skybooker.auth.repository.UserRepository;
import com.skybooker.auth.security.JwtService;
import com.skybooker.auth.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "Test User",
                "user@example.com",
                "Password1",
                "9876543210",
                "P1234567",
                "INDIAN",
                Role.PASSENGER
        );
    }

    @Test
    void registerShouldEncodePasswordAndReturnTokens() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.phone())).thenReturn(false);
        when(userRepository.existsByPassportNumber(registerRequest.passportNumber())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(UUID.randomUUID());
            return user;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        var response = authService.register(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getProvider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void refreshTokenShouldThrowForBlacklistedToken() {
        when(tokenBlacklistService.isBlacklisted(anyString())).thenReturn(true);

        assertThrows(
                UnauthorizedException.class,
                () -> authService.refreshToken(new RefreshTokenRequest("refresh-token"))
        );
    }
}
