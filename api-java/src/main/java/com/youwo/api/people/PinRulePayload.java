package com.youwo.api.people;

import io.leangen.graphql.annotations.GraphQLNonNull;
import java.util.UUID;

public record PinRulePayload(
    UUID id,
    UUID personId,
    String personName,
    int targetPosition,
    int targetIndex,
    Integer effectiveIndex,
    @GraphQLNonNull PinRuleState state,
    boolean enabled,
    String createdAt,
    String updatedAt) {

  public static PinRulePayload fromEntity(
      PinRuleEntity entity, PinRulePlacementResolver.PinRulePlacement placement) {
    int targetIndex = placement != null
        ? placement.targetIndex()
        : entity.getTargetPosition();
    Integer effectiveIndex = placement != null ? placement.effectiveIndex() : null;
    PinRuleState state = placement != null
        ? placement.state()
        : (entity.isEnabled() ? PinRuleState.NO_MATCH : PinRuleState.INACTIVE);

    return new PinRulePayload(
        entity.getId(),
        entity.getPerson().getId(),
        entity.getPerson().getName(),
        entity.getTargetPosition(),
        targetIndex,
        effectiveIndex,
        state,
        entity.isEnabled(),
        entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
        entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString());
  }

  public static PinRulePayload fromEntity(PinRuleEntity entity) {
    return fromEntity(entity, null);
  }
}
