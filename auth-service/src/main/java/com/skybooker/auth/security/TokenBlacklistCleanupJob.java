package com.skybooker.auth.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenBlacklistCleanupJob {

    private final TokenBlacklistService tokenBlacklistService;

    public TokenBlacklistCleanupJob(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Scheduled(fixedDelay = 300000)
    public void cleanupExpiredTokens() {
        tokenBlacklistService.cleanupExpired();
    }
}
