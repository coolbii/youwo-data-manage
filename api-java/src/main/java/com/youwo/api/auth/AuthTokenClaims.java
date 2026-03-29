package com.youwo.api.auth;

import java.time.Instant;
import java.util.UUID;

public record AuthTokenClaims(
    UUID userId,
    String email,
    String tokenId,
    Instant issuedAt,
    Instant expiresAt) {
}
