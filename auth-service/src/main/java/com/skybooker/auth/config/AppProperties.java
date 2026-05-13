package com.skybooker.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application")
public class AppProperties {

    private final OAuth2 oauth2 = new OAuth2();

    public OAuth2 getOauth2() {
        return oauth2;
    }

    public static class OAuth2 {
        private String authorizedRedirectUri = "http://localhost:4200/oauth2/redirect";

        public String getAuthorizedRedirectUri() {
            return authorizedRedirectUri;
        }

        public void setAuthorizedRedirectUri(String authorizedRedirectUri) {
            this.authorizedRedirectUri = authorizedRedirectUri;
        }
    }
}
