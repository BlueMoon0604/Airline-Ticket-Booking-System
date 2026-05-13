package com.skybooker.auth.security;

import com.skybooker.auth.entity.AuthProvider;
import com.skybooker.auth.entity.Role;
import com.skybooker.auth.entity.User;
import com.skybooker.auth.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        OAuth2UserInfo userInfo = new OAuth2UserInfo(oAuth2User.getAttributes());

        User user = userRepository.findByEmail(userInfo.getEmail())
                .map(existing -> updateExistingUser(existing, userInfo))
                .orElseGet(() -> registerNewUser(userInfo));

        return new DefaultOAuth2User(
                List.of(() -> "ROLE_" + user.getRole().name()),
                enrichAttributes(oAuth2User, user),
                "email"
        );
    }

    private User registerNewUser(OAuth2UserInfo userInfo) {
        User user = new User();
        user.setFullName(userInfo.getName());
        user.setEmail(userInfo.getEmail());
        user.setPasswordHash(UUID.randomUUID().toString());
        user.setPhone("OAUTH-" + UUID.randomUUID().toString().substring(0, 8));
        user.setRole(Role.PASSENGER);
        user.setProvider(AuthProvider.GOOGLE);
        user.setIsActive(true);
        return userRepository.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo userInfo) {
        user.setFullName(userInfo.getName());
        user.setProvider(AuthProvider.GOOGLE);
        return userRepository.save(user);
    }

    private java.util.Map<String, Object> enrichAttributes(OAuth2User oAuth2User, User user) {
        java.util.Map<String, Object> attrs = new java.util.HashMap<>(oAuth2User.getAttributes());
        attrs.put("userId", user.getUserId().toString());
        attrs.put("role", user.getRole().name());
        return attrs;
    }
}
