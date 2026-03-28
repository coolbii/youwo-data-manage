package com.youwo.api.people;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PeopleListPinInjector {
  private PeopleListPinInjector() {}

  public static PinInjectedPage injectAndPage(
      List<PersonEntity> orderedPeople,
      List<PinRuleEntity> pinRules,
      int first,
      int afterOffset) {
    int safeFirst = Math.max(first, 1);
    int safeAfterOffset = Math.max(afterOffset, 0);
    int totalCount = orderedPeople.size();

    if (totalCount == 0 || safeAfterOffset >= totalCount) {
      return new PinInjectedPage(List.of(), totalCount, false, null);
    }

    List<PersonEntity> merged = injectPinnedPeople(orderedPeople, pinRules);
    int endOffset = Math.min(safeAfterOffset + safeFirst, merged.size());
    boolean hasNextPage = endOffset < merged.size();

    return new PinInjectedPage(
        List.copyOf(merged.subList(safeAfterOffset, endOffset)),
        totalCount,
        hasNextPage,
        endOffset);
  }

  private static List<PersonEntity> injectPinnedPeople(
      List<PersonEntity> orderedPeople, List<PinRuleEntity> pinRules) {
    int totalCount = orderedPeople.size();
    Set<UUID> resultPersonIds = new HashSet<>();
    for (PersonEntity person : orderedPeople) {
      UUID personId = person.getId();
      if (personId != null) {
        resultPersonIds.add(personId);
      }
    }

    List<PinRuleEntity> matchedRules = pinRules.stream()
        .filter((rule) -> rule.getPerson() != null && rule.getPerson().getId() != null)
        .filter((rule) -> resultPersonIds.contains(rule.getPerson().getId()))
        .toList();

    List<PinRulePlacementResolver.PinRuleCandidate> candidates = matchedRules.stream()
        .map((rule) ->
            new PinRulePlacementResolver.PinRuleCandidate(
                rule.getId(),
                rule.getTargetPosition(),
                rule.isEnabled(),
                rule.getCreatedAt()))
        .toList();

    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements =
        PinRulePlacementResolver.resolve(candidates, totalCount);

    Map<Integer, PersonEntity> pinnedByIndex = new HashMap<>();
    Set<UUID> pinnedPersonIds = new HashSet<>();
    for (PinRuleEntity rule : matchedRules) {
      UUID ruleId = rule.getId();
      if (ruleId == null) {
        continue;
      }
      PinRulePlacementResolver.PinRulePlacement placement = placements.get(ruleId);
      if (placement == null || placement.effectiveIndex() == null) {
        continue;
      }
      PersonEntity person = rule.getPerson();
      UUID personId = person.getId();
      if (personId == null) {
        continue;
      }
      if (pinnedByIndex.putIfAbsent(placement.effectiveIndex(), person) == null) {
        pinnedPersonIds.add(personId);
      }
    }

    Deque<PersonEntity> remaining = new ArrayDeque<>();
    for (PersonEntity person : orderedPeople) {
      UUID personId = person.getId();
      if (personId == null || !pinnedPersonIds.contains(personId)) {
        remaining.addLast(person);
      }
    }

    List<PersonEntity> merged = new ArrayList<>(totalCount);
    for (int index = 1; index <= totalCount; index += 1) {
      PersonEntity pinned = pinnedByIndex.get(index);
      if (pinned != null) {
        merged.add(pinned);
      } else if (!remaining.isEmpty()) {
        merged.add(remaining.removeFirst());
      }
    }

    return merged;
  }

  public record PinInjectedPage(
      List<PersonEntity> nodes,
      int totalCount,
      boolean hasNextPage,
      Integer endOffset) {}
}
