package com.skybooker.auth.security;

import java.util.Map;

public class OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getEmail() {
        return (String) attributes.get("email");
    }

    public String getName() {
        return (String) attributes.getOrDefault("name", "Google User");
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
