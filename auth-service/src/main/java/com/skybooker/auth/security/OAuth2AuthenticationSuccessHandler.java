package com.skybooker.auth.security;

import com.skybooker.auth.config.AppProperties;
import com.skybooker.auth.dto.AuthResponse;
import com.skybooker.auth.entity.User;
import com.skybooker.auth.exception.ResourceNotFoundException;
import com.skybooker.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AppProperties appProperties;

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository,
                                              JwtService jwtService,
                                              AppProperties appProperties) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.appProperties = appProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("OAuth user not found"));

        AuthResponse authResponse = new AuthResponse(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user)
        );

        String targetUrl = UriComponentsBuilder.fromUriString(appProperties.getOauth2().getAuthorizedRedirectUri())
                .queryParam("accessToken", authResponse.accessToken())
                .queryParam("refreshToken", authResponse.refreshToken())
                .queryParam("email", authResponse.email())
                .queryParam("role", authResponse.role())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
