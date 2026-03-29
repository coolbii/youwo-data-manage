package com.youwo.api.people;

import com.youwo.api.auth.AuthSessionService;
import com.youwo.api.graphql.GraphqlRequestContext;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PinRulesGraphqlController {
  private final PinRuleRepository pinRuleRepository;
  private final PersonRepository personRepository;
  private final AuthSessionService authSessionService;

  public PinRulesGraphqlController(
      PinRuleRepository pinRuleRepository,
      PersonRepository personRepository,
      AuthSessionService authSessionService) {
    this.pinRuleRepository = pinRuleRepository;
    this.personRepository = personRepository;
    this.authSessionService = authSessionService;
  }

  @GraphQLQuery(name = "pinRules")
  public List<PinRulePayload> pinRules(
      @GraphQLArgument(name = "scopeTotal") Integer scopeTotal,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    requireAuthentication(requestContext);

    List<PinRuleEntity> rules = pinRuleRepository.findAllWithPerson();
    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements =
        resolvePlacements(rules, scopeTotal);

    return rules.stream()
        .map((rule) -> PinRulePayload.fromEntity(rule, placements.get(rule.getId())))
        .toList();
  }

  @GraphQLMutation(name = "createPinRule")
  @Transactional
  public PinRulePayload createPinRule(
      @GraphQLArgument(name = "personId") UUID personId,
      @GraphQLArgument(name = "targetPosition") int targetPosition,
      @GraphQLArgument(name = "scopeTotal") Integer scopeTotal,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    requireAuthentication(requestContext);

    if (targetPosition < 1) {
      throw new IllegalArgumentException("targetPosition must be >= 1");
    }
    PersonEntity person =
        personRepository
            .findById(personId)
            .orElseThrow(() -> new IllegalArgumentException("Person not found: " + personId));
    PinRuleEntity rule = new PinRuleEntity();
    rule.setPerson(person);
    rule.setTargetPosition(targetPosition);
    PinRuleEntity saved = pinRuleRepository.save(rule);
    return resolvePayload(saved.getId(), scopeTotal);
  }

  @GraphQLMutation(name = "updatePinRule")
  @Transactional
  public PinRulePayload updatePinRule(
      @GraphQLArgument(name = "id") UUID id,
      @GraphQLArgument(name = "targetPosition") Integer targetPosition,
      @GraphQLArgument(name = "enabled") Boolean enabled,
      @GraphQLArgument(name = "scopeTotal") Integer scopeTotal,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    requireAuthentication(requestContext);

    PinRuleEntity rule =
        pinRuleRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Pin rule not found: " + id));
    if (targetPosition != null) {
      if (targetPosition < 1) {
        throw new IllegalArgumentException("targetPosition must be >= 1");
      }
      rule.setTargetPosition(targetPosition);
    }
    if (enabled != null) {
      rule.setEnabled(enabled);
    }
    PinRuleEntity saved = pinRuleRepository.save(rule);
    return resolvePayload(saved.getId(), scopeTotal);
  }

  @GraphQLMutation(name = "deletePinRule")
  @Transactional
  public boolean deletePinRule(
      @GraphQLArgument(name = "id") UUID id,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    requireAuthentication(requestContext);

    if (!pinRuleRepository.existsById(id)) {
      throw new IllegalArgumentException("Pin rule not found: " + id);
    }
    pinRuleRepository.deleteById(id);
    return true;
  }

  private PinRulePayload resolvePayload(UUID ruleId, Integer scopeTotal) {
    // NOTE(YW-028): this mutation response still computes placement by loading all pin rules.
    // It's acceptable at current scale and keeps returned state deterministic post-mutation.
    List<PinRuleEntity> rules = pinRuleRepository.findAllWithPerson();
    Map<UUID, PinRulePlacementResolver.PinRulePlacement> placements =
        resolvePlacements(rules, scopeTotal);

    return rules.stream()
        .filter((rule) -> ruleId.equals(rule.getId()))
        .findFirst()
        .map((rule) -> PinRulePayload.fromEntity(rule, placements.get(rule.getId())))
        .orElseThrow(() -> new IllegalStateException("Pin rule missing after save: " + ruleId));
  }

  private Map<UUID, PinRulePlacementResolver.PinRulePlacement> resolvePlacements(
      List<PinRuleEntity> rules, Integer scopeTotal) {
    int totalSize = resolvePlacementScopeTotal(scopeTotal);
    List<PinRulePlacementResolver.PinRuleCandidate> candidates = rules.stream()
        .map((rule) ->
            new PinRulePlacementResolver.PinRuleCandidate(
                rule.getId(),
                rule.getTargetPosition(),
                rule.isEnabled(),
                rule.getCreatedAt()))
        .toList();

    return PinRulePlacementResolver.resolve(candidates, totalSize);
  }

  private int resolvePlacementScopeTotal(Integer scopeTotal) {
    if (scopeTotal != null) {
      if (scopeTotal < 0) {
        throw new IllegalArgumentException("scopeTotal must be >= 0");
      }
      return scopeTotal;
    }

    // Fallback for callers that do not provide query context yet.
    // This uses global people count and may differ from filtered list totals.
    // YW-028 will make list-pipeline context the default placement scope.
    return (int) Math.min(Integer.MAX_VALUE, personRepository.count());
  }

  private void requireAuthentication(GraphqlRequestContext requestContext) {
    authSessionService.requireAuthenticatedUser(requestContext);
  }
}
