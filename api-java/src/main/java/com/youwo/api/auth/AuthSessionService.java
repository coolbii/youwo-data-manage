package com.youwo.api.auth;

import com.youwo.api.graphql.GraphqlRequestContext;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthSessionService {
  public static final String CSRF_HEADER_NAME = "X-CSRF-Token";
  private static final int CSRF_TOKEN_SIZE_BYTES = 32;
  private static final String DEFAULT_SESSION_COOKIE_NAME = "__Host-sid";
  private static final String DEFAULT_CSRF_COOKIE_NAME = "__Host-csrf";

  private final UserRepository userRepository;
  private final JwtTokenService jwtTokenService;
  private final AuthProperties authProperties;
  private final SecureRandom secureRandom;

  public AuthSessionService(
      UserRepository userRepository,
      JwtTokenService jwtTokenService,
      AuthProperties authProperties) {
    this.userRepository = userRepository;
    this.jwtTokenService = jwtTokenService;
    this.authProperties = authProperties;
    this.secureRandom = new SecureRandom();
  }

  public AuthSessionPayload issueSession(UserEntity user, GraphqlRequestContext requestContext) {
    AuthTokenPair tokenPair = jwtTokenService.issueTokenPair(user);
    user.setRefreshTokenHash(hashTokenId(tokenPair.refreshTokenId()));
    userRepository.save(user);

    HttpServletResponse response = requireHttpServletResponse(requestContext);
    String csrfToken = generateCsrfToken();
    writeSessionCookie(response, tokenPair.refreshToken(), authProperties.refreshTokenTtlSeconds());
    writeCsrfCookie(response, csrfToken, authProperties.refreshTokenTtlSeconds());
    return AuthSessionPayload.from(user);
  }

  public UserEntity requireAuthenticatedUser(GraphqlRequestContext requestContext) {
    String sessionToken = getSessionToken(requestContext);
    if (sessionToken == null || sessionToken.isBlank()) {
      throw new IllegalArgumentException("Authentication is required");
    }

    AuthTokenClaims claims = jwtTokenService.parseRefreshToken(sessionToken);
    UserEntity user = userRepository.findById(claims.userId())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!user.getEmail().equalsIgnoreCase(claims.email())) {
      throw new IllegalArgumentException("Token subject is invalid");
    }

    if (!safeEquals(user.getRefreshTokenHash(), hashTokenId(claims.tokenId()))) {
      throw new IllegalArgumentException("Session token is invalid");
    }

    return user;
  }

  public void validateMutationCsrf(GraphqlRequestContext requestContext) {
    String sessionToken = getSessionToken(requestContext);
    if (sessionToken == null || sessionToken.isBlank()) {
      return;
    }

    String csrfCookieToken = getCookieToken(requestContext, resolveCsrfCookieName());
    String csrfHeaderToken = getHeader(requestContext, CSRF_HEADER_NAME);
    if (!safeEquals(csrfCookieToken, csrfHeaderToken)) {
      throw new IllegalArgumentException("CSRF token is invalid");
    }
  }

  public void clearSessionCookies(GraphqlRequestContext requestContext) {
    HttpServletResponse response = requireHttpServletResponse(requestContext);
    writeSessionCookie(response, "", 0L);
    writeCsrfCookie(response, "", 0L);
  }

  private String getSessionToken(GraphqlRequestContext requestContext) {
    return getCookieToken(requestContext, resolveSessionCookieName());
  }

  private String getCookieToken(GraphqlRequestContext requestContext, String cookieName) {
    if (requestContext == null || cookieName == null || cookieName.isBlank()) {
      return null;
    }
    return requestContext.cookie(cookieName);
  }

  private String getHeader(GraphqlRequestContext requestContext, String headerName) {
    if (requestContext == null || headerName == null || headerName.isBlank()) {
      return null;
    }
    return requestContext.header(headerName);
  }

  private HttpServletResponse requireHttpServletResponse(GraphqlRequestContext requestContext) {
    if (requestContext == null || requestContext.httpServletResponse() == null) {
      throw new IllegalStateException("HTTP response context is missing");
    }
    return requestContext.httpServletResponse();
  }

  private void writeSessionCookie(HttpServletResponse response, String value, long maxAgeSeconds) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        buildCookie(resolveSessionCookieName(), value, true, maxAgeSeconds).toString());
  }

  private void writeCsrfCookie(HttpServletResponse response, String value, long maxAgeSeconds) {
    response.addHeader(
        HttpHeaders.SET_COOKIE,
        buildCookie(resolveCsrfCookieName(), value, false, maxAgeSeconds).toString());
  }

  private ResponseCookie buildCookie(
      String name, String value, boolean httpOnly, long maxAgeSeconds) {
    return ResponseCookie.from(name, value)
        .httpOnly(httpOnly)
        .secure(authProperties.cookieSecure())
        .sameSite("Strict")
        .path("/")
        .maxAge(maxAgeSeconds)
        .build();
  }

  private String resolveSessionCookieName() {
    String configured = authProperties.sessionCookieName();
    return configured == null || configured.isBlank()
        ? DEFAULT_SESSION_COOKIE_NAME
        : configured;
  }

  private String resolveCsrfCookieName() {
    String configured = authProperties.csrfCookieName();
    return configured == null || configured.isBlank()
        ? DEFAULT_CSRF_COOKIE_NAME
        : configured;
  }

  private String generateCsrfToken() {
    byte[] entropy = new byte[CSRF_TOKEN_SIZE_BYTES];
    secureRandom.nextBytes(entropy);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(entropy);
  }

  private static String hashTokenId(String tokenId) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(tokenId.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private static boolean safeEquals(String expected, String actual) {
    if (expected == null || actual == null) {
      return false;
    }
    return MessageDigest.isEqual(
        expected.getBytes(StandardCharsets.UTF_8),
        actual.getBytes(StandardCharsets.UTF_8));
  }
}
