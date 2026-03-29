package com.youwo.api.auth;

import java.util.UUID;

public record AuthUserPayload(
    UUID id,
    String email,
    String createdAt,
    String updatedAt) {
  public static AuthUserPayload fromEntity(UserEntity entity) {
    return new AuthUserPayload(
        entity.getId(),
        entity.getEmail(),
        entity.getCreatedAt().toString(),
        entity.getUpdatedAt().toString());
  }
}
