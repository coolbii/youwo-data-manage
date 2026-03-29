package com.youwo.api.auth;

public record AuthSessionPayload(
    AuthUserPayload user) {
  public static AuthSessionPayload from(UserEntity user) {
    return new AuthSessionPayload(
        AuthUserPayload.fromEntity(user));
  }
}
