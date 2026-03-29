package com.youwo.api.auth;

import java.time.Instant;

public record AuthTokenPair(
    String refreshToken,
    String refreshTokenId,
    Instant refreshTokenExpiresAt) {
}
