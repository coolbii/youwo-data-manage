package com.youwo.api.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
    String jwtSecret,
    long refreshTokenTtlSeconds,
    String sessionCookieName,
    String csrfCookieName,
    boolean cookieSecure) {
}
