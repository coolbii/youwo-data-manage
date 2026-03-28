package com.youwo.api.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PinRulePlacementResolverTest {
  @Test
  void resolvesActiveWhenTargetWithinRange() {
    UUID ruleId = uuid("00000000-0000-0000-0000-000000000001");
    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements = PinRulePlacementResolver.resolve(
        List.of(candidate(ruleId, 2, true, "2026-01-01T00:00:00Z")),
        5);

    PinRulePlacementResolver.PinRulePlacement placement = placements.get(ruleId);
    assertNotNull(placement);
    assertEquals(2, placement.targetIndex());
    assertEquals(2, placement.effectiveIndex());
    assertEquals(PinRuleState.ACTIVE, placement.state());
  }

  @Test
  void resolvesClampedWhenTargetExceedsTotal() {
    UUID ruleId = uuid("00000000-0000-0000-0000-000000000002");
    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements = PinRulePlacementResolver.resolve(
        List.of(candidate(ruleId, 8, true, "2026-01-01T00:00:00Z")),
        3);

    PinRulePlacementResolver.PinRulePlacement placement = placements.get(ruleId);
    assertNotNull(placement);
    assertEquals(8, placement.targetIndex());
    assertEquals(3, placement.effectiveIndex());
    assertEquals(PinRuleState.CLAMPED, placement.state());
  }

  @Test
  void resolvesCollisionByShiftingLowerPriorityRule() {
    UUID first = uuid("00000000-0000-0000-0000-000000000003");
    UUID second = uuid("00000000-0000-0000-0000-000000000004");

    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements = PinRulePlacementResolver.resolve(
        List.of(
            candidate(first, 2, true, "2026-01-01T00:00:00Z"),
            candidate(second, 2, true, "2026-01-02T00:00:00Z")),
        4);

    assertEquals(2, placements.get(first).effectiveIndex());
    assertEquals(PinRuleState.ACTIVE, placements.get(first).state());
    assertEquals(3, placements.get(second).effectiveIndex());
    assertEquals(PinRuleState.ACTIVE, placements.get(second).state());
  }

  @Test
  void resolvesOverflowAsNoMatchWhenAllSlotsAreTaken() {
    UUID r1 = uuid("00000000-0000-0000-0000-000000000005");
    UUID r2 = uuid("00000000-0000-0000-0000-000000000006");
    UUID r3 = uuid("00000000-0000-0000-0000-000000000007");
    UUID r4 = uuid("00000000-0000-0000-0000-000000000008");

    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements = PinRulePlacementResolver.resolve(
        List.of(
            candidate(r1, 1, true, "2026-01-01T00:00:00Z"),
            candidate(r2, 2, true, "2026-01-01T00:00:01Z"),
            candidate(r3, 3, true, "2026-01-01T00:00:02Z"),
            candidate(r4, 5, true, "2026-01-01T00:00:03Z")),
        3);

    assertEquals(1, placements.get(r1).effectiveIndex());
    assertEquals(2, placements.get(r2).effectiveIndex());
    assertEquals(3, placements.get(r3).effectiveIndex());
    assertNull(placements.get(r4).effectiveIndex());
    assertEquals(PinRuleState.NO_MATCH, placements.get(r4).state());
  }

  @Test
  void resolvesDisabledAsInactive() {
    UUID ruleId = uuid("00000000-0000-0000-0000-000000000009");
    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements = PinRulePlacementResolver.resolve(
        List.of(candidate(ruleId, 4, false, "2026-01-01T00:00:00Z")),
        10);

    PinRulePlacementResolver.PinRulePlacement placement = placements.get(ruleId);
    assertNotNull(placement);
    assertEquals(4, placement.targetIndex());
    assertNull(placement.effectiveIndex());
    assertEquals(PinRuleState.INACTIVE, placement.state());
  }

  @Test
  void resolvesNoMatchWhenResultSetIsEmpty() {
    UUID ruleId = uuid("00000000-0000-0000-0000-000000000010");
    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements = PinRulePlacementResolver.resolve(
        List.of(candidate(ruleId, 1, true, "2026-01-01T00:00:00Z")),
        0);

    PinRulePlacementResolver.PinRulePlacement placement = placements.get(ruleId);
    assertNotNull(placement);
    assertEquals(1, placement.targetIndex());
    assertNull(placement.effectiveIndex());
    assertEquals(PinRuleState.NO_MATCH, placement.state());
  }

  @Test
  void resolvesTieByRuleIdWhenTargetAndCreatedAtAreEqual() {
    UUID first = uuid("00000000-0000-0000-0000-000000000011");
    UUID second = uuid("00000000-0000-0000-0000-000000000012");

    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements = PinRulePlacementResolver.resolve(
        List.of(
            candidate(second, 1, true, "2026-01-01T00:00:00Z"),
            candidate(first, 1, true, "2026-01-01T00:00:00Z")),
        2);

    assertEquals(1, placements.get(first).effectiveIndex());
    assertEquals(2, placements.get(second).effectiveIndex());
  }

  private static PinRulePlacementResolver.PinRuleCandidate candidate(
      UUID ruleId, int targetIndex, boolean enabled, String createdAt) {
    return new PinRulePlacementResolver.PinRuleCandidate(
        ruleId,
        targetIndex,
        enabled,
        OffsetDateTime.parse(createdAt));
  }

  private static UUID uuid(String value) {
    return UUID.fromString(value);
  }
}
