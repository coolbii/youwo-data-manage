package com.youwo.api.people;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PinRulesGraphqlController {
  private final PinRuleRepository pinRuleRepository;
  private final PersonRepository personRepository;

  public PinRulesGraphqlController(
      PinRuleRepository pinRuleRepository, PersonRepository personRepository) {
    this.pinRuleRepository = pinRuleRepository;
    this.personRepository = personRepository;
  }

  @GraphQLQuery(name = "pinRules")
  public List<PinRulePayload> pinRules() {
    return pinRuleRepository.findAllWithPerson().stream()
        .map(PinRulePayload::fromEntity)
        .toList();
  }

  @GraphQLMutation(name = "createPinRule")
  @Transactional
  public PinRulePayload createPinRule(
      @GraphQLArgument(name = "personId") UUID personId,
      @GraphQLArgument(name = "targetPosition") int targetPosition) {
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
    return PinRulePayload.fromEntity(pinRuleRepository.save(rule));
  }

  @GraphQLMutation(name = "updatePinRule")
  @Transactional
  public PinRulePayload updatePinRule(
      @GraphQLArgument(name = "id") UUID id,
      @GraphQLArgument(name = "targetPosition") Integer targetPosition,
      @GraphQLArgument(name = "enabled") Boolean enabled) {
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
    return PinRulePayload.fromEntity(pinRuleRepository.save(rule));
  }

  @GraphQLMutation(name = "deletePinRule")
  @Transactional
  public boolean deletePinRule(@GraphQLArgument(name = "id") UUID id) {
    if (!pinRuleRepository.existsById(id)) {
      throw new IllegalArgumentException("Pin rule not found: " + id);
    }
    pinRuleRepository.deleteById(id);
    return true;
  }
}
