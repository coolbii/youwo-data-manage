package com.youwo.api.people;

import java.util.UUID;

public record PinRulePayload(
    UUID id,
    UUID personId,
    String personName,
    int targetPosition,
    boolean enabled,
    String createdAt,
    String updatedAt) {

  public static PinRulePayload fromEntity(PinRuleEntity entity) {
    return new PinRulePayload(
        entity.getId(),
        entity.getPerson().getId(),
        entity.getPerson().getName(),
        entity.getTargetPosition(),
        entity.isEnabled(),
        entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
        entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString());
  }
}
