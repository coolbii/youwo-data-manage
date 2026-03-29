package com.youwo.api.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {
  private static final String CLAIM_TOKEN_TYPE = "type";
  private static final String TOKEN_TYPE_REFRESH = "refresh";
  private static final String ISSUER = "youwo";
  private static final String AUDIENCE = "youwo-api";

  private final AuthProperties authProperties;
  private final Clock clock;
  private final SecretKey signingKey;

  public JwtTokenService(AuthProperties authProperties, Clock clock) {
    this.authProperties = authProperties;
    this.clock = clock;

    String secret = authProperties.jwtSecret();
    if (secret == null || secret.length() < 32) {
      throw new IllegalStateException("app.auth.jwt-secret must be at least 32 characters");
    }
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public AuthTokenPair issueTokenPair(UserEntity user) {
    Instant now = Instant.now(clock);
    Instant refreshExpiresAt = now.plusSeconds(authProperties.refreshTokenTtlSeconds());
    String refreshTokenId = UUID.randomUUID().toString();

    String refreshToken =
        buildToken(user, TOKEN_TYPE_REFRESH, now, refreshExpiresAt, refreshTokenId);

    return new AuthTokenPair(refreshToken, refreshTokenId, refreshExpiresAt);
  }

  public AuthTokenClaims parseRefreshToken(String token) {
    return parseToken(token, TOKEN_TYPE_REFRESH);
  }

  private String buildToken(
      UserEntity user,
      String tokenType,
      Instant issuedAt,
      Instant expiresAt,
      String tokenId) {
    io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
        .issuer(ISSUER)
        .audience().add(AUDIENCE).and()
        .subject(user.getId().toString())
        .claim(CLAIM_TOKEN_TYPE, tokenType)
        .claim("email", user.getEmail())
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(expiresAt))
        .signWith(signingKey);

    if (tokenId != null && !tokenId.isBlank()) {
      builder.id(tokenId);
    }

    return builder.compact();
  }

  private AuthTokenClaims parseToken(String token, String expectedType) {
    try {
      Claims claims = Jwts.parser()
          .requireIssuer(ISSUER)
          .verifyWith(signingKey)
          .clock(() -> Date.from(Instant.now(clock)))
          .build()
          .parseSignedClaims(token)
          .getPayload();

      Set<String> audience = claims.getAudience();
      if (audience == null || !audience.contains(AUDIENCE)) {
        throw new IllegalArgumentException("Token audience is invalid");
      }

      String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
      if (!expectedType.equals(tokenType)) {
        throw new IllegalArgumentException("Token type is invalid");
      }

      String subject = claims.getSubject();
      if (subject == null || subject.isBlank()) {
        throw new IllegalArgumentException("Token subject is missing");
      }

      String email = claims.get("email", String.class);
      if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Token email is missing");
      }

      String tokenId = claims.getId();
      if (TOKEN_TYPE_REFRESH.equals(expectedType)
          && (tokenId == null || tokenId.isBlank())) {
        throw new IllegalArgumentException("Refresh token id is missing");
      }

      Date issuedAt = claims.getIssuedAt();
      Date expiresAt = claims.getExpiration();
      if (issuedAt == null || expiresAt == null) {
        throw new IllegalArgumentException("Token timing is missing");
      }

      return new AuthTokenClaims(
          UUID.fromString(subject),
          email,
          tokenId,
          issuedAt.toInstant(),
          expiresAt.toInstant());
    } catch (JwtException | IllegalArgumentException exception) {
      throw new IllegalArgumentException("Token is invalid or expired", exception);
    }
  }
}
