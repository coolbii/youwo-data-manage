package com.youwo.api.people;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PinRulePlacementResolver {
  private static final Comparator<PinRuleCandidate> RULE_PRIORITY =
      Comparator.comparingInt(PinRuleCandidate::targetIndex)
          .thenComparing(
              PinRuleCandidate::createdAt,
              Comparator.nullsLast(Comparator.naturalOrder()))
          .thenComparing(PinRuleCandidate::ruleId, Comparator.nullsLast(Comparator.naturalOrder()));

  private PinRulePlacementResolver() {}

  public static Map<UUID, PinRulePlacement> resolve(
      List<PinRuleCandidate> candidates, int totalSize) {
    int normalizedTotal = Math.max(0, totalSize);
    Map<UUID, PinRulePlacement> placements = new HashMap<>();
    Set<Integer> occupied = new HashSet<>();

    for (PinRuleCandidate candidate : candidates.stream().sorted(RULE_PRIORITY).toList()) {
      UUID ruleId = candidate.ruleId();
      if (ruleId == null) {
        continue;
      }

      int targetIndex = Math.max(1, candidate.targetIndex());
      if (!candidate.enabled()) {
        placements.put(ruleId, new PinRulePlacement(targetIndex, null, PinRuleState.INACTIVE));
        continue;
      }

      if (normalizedTotal < 1) {
        placements.put(ruleId, new PinRulePlacement(targetIndex, null, PinRuleState.NO_MATCH));
        continue;
      }

      int effectiveIndex = Math.min(targetIndex, normalizedTotal);
      while (occupied.contains(effectiveIndex) && effectiveIndex < normalizedTotal) {
        effectiveIndex += 1;
      }

      if (occupied.contains(effectiveIndex)) {
        placements.put(ruleId, new PinRulePlacement(targetIndex, null, PinRuleState.NO_MATCH));
        continue;
      }

      occupied.add(effectiveIndex);
      PinRuleState state =
          targetIndex > normalizedTotal ? PinRuleState.CLAMPED : PinRuleState.ACTIVE;
      placements.put(ruleId, new PinRulePlacement(targetIndex, effectiveIndex, state));
    }

    return placements;
  }

  public record PinRuleCandidate(
      UUID ruleId,
      int targetIndex,
      boolean enabled,
      OffsetDateTime createdAt) {}

  public record PinRulePlacement(
      int targetIndex,
      Integer effectiveIndex,
      PinRuleState state) {}
}
