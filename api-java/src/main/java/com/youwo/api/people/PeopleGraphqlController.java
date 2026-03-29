package com.youwo.api.people;

import com.youwo.api.auth.AuthSessionService;
import com.youwo.api.graphql.GraphqlRequestContext;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PeopleGraphqlController {
  private final PersonRepository repository;
  private final PinRuleRepository pinRuleRepository;
  private final AuthSessionService authSessionService;

  public PeopleGraphqlController(
      PersonRepository repository,
      PinRuleRepository pinRuleRepository,
      AuthSessionService authSessionService) {
    this.repository = repository;
    this.pinRuleRepository = pinRuleRepository;
    this.authSessionService = authSessionService;
  }

  @GraphQLQuery(name = "peopleList")
  public PeopleConnectionPayload peopleList(
      @GraphQLArgument(name = "first") Integer first,
      @GraphQLArgument(name = "after") String after,
      @GraphQLArgument(name = "search") String search,
      @GraphQLArgument(name = "sortBy") PeopleSortField sortBy,
      @GraphQLArgument(name = "sortDirection") SortDirection sortDirection,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    requireAuthentication(requestContext);

    int safeFirst = first == null ? 100 : Math.min(Math.max(first, 1), 100);
    PeopleSortField safeSortBy = sortBy == null ? PeopleSortField.CREATED_AT : sortBy;
    SortDirection safeSortDir = sortDirection == null ? SortDirection.ASC : sortDirection;

    int afterOffset = 0;
    if (after != null) {
      try {
        PeopleCursor.DecodedOffsetCursor decoded = PeopleCursor.decodeOffset(after);
        if (decoded.sortField() == safeSortBy && decoded.direction() == safeSortDir) {
          afterOffset = decoded.offset();
        }
      } catch (IllegalArgumentException ignored) {
        // Invalid or legacy cursor format, restart from first page.
      }
    }

    // NOTE(YW-039): this intentionally loads the full scoped result for deterministic
    // pin injection and pagination continuity; optimize in the scale/perf phase.
    List<PersonEntity> orderedPeople = repository.findPeople(search, safeSortBy, safeSortDir);
    List<PinRuleEntity> pinRules = pinRuleRepository.findAllWithPerson();
    PeopleListPinInjector.PinInjectedPage page =
        PeopleListPinInjector.injectAndPage(orderedPeople, pinRules, safeFirst, afterOffset);

    String endCursor = page.endOffset() == null
        ? null
        : PeopleCursor.encodeOffset(safeSortBy, safeSortDir, page.endOffset());

    return new PeopleConnectionPayload(
        page.nodes().stream().map(PersonPayload::fromEntity).toList(),
        new PeoplePageInfoPayload(endCursor, page.hasNextPage()),
        page.totalCount());
  }

  @GraphQLQuery(name = "person")
  public PersonPayload person(
      @GraphQLArgument(name = "id") UUID id,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    requireAuthentication(requestContext);

    return repository.findById(id)
        .map(PersonPayload::fromEntity)
        .orElseThrow(() -> new IllegalArgumentException("Person not found: " + id));
  }

  @GraphQLMutation(name = "createPerson")
  @Transactional
  public PersonPayload createPerson(
      @GraphQLArgument(name = "name") String name,
      @GraphQLArgument(name = "position") String position,
      @GraphQLArgument(name = "location") String location,
      @GraphQLArgument(name = "birthdate") String birthdate,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    requireAuthentication(requestContext);

    validatePersonFields(name, position, location, birthdate);
    PersonEntity entity = new PersonEntity();
    entity.setName(name.trim());
    entity.setPosition(position.trim());
    entity.setLocation(location.trim());
    entity.setBirthdate(LocalDate.parse(birthdate));
    return PersonPayload.fromEntity(repository.save(entity));
  }

  @GraphQLMutation(name = "updatePerson")
  @Transactional
  public PersonPayload updatePerson(
      @GraphQLArgument(name = "id") UUID id,
      @GraphQLArgument(name = "name") String name,
      @GraphQLArgument(name = "position") String position,
      @GraphQLArgument(name = "location") String location,
      @GraphQLArgument(name = "birthdate") String birthdate,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    requireAuthentication(requestContext);

    PersonEntity entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Person not found: " + id));
    if (name != null) {
      entity.setName(name.trim());
    }
    if (position != null) {
      entity.setPosition(position.trim());
    }
    if (location != null) {
      entity.setLocation(location.trim());
    }
    if (birthdate != null) {
      entity.setBirthdate(LocalDate.parse(birthdate));
    }
    return PersonPayload.fromEntity(repository.save(entity));
  }

  @GraphQLMutation(name = "deletePerson")
  @Transactional
  public boolean deletePerson(
      @GraphQLArgument(name = "id") UUID id,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    requireAuthentication(requestContext);

    if (!repository.existsById(id)) {
      throw new IllegalArgumentException("Person not found: " + id);
    }
    repository.deleteById(id);
    return true;
  }

  private void validatePersonFields(
      String name, String position, String location, String birthdate) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name is required");
    }
    if (position == null || position.isBlank()) {
      throw new IllegalArgumentException("position is required");
    }
    if (location == null || location.isBlank()) {
      throw new IllegalArgumentException("location is required");
    }
    if (birthdate == null || birthdate.isBlank()) {
      throw new IllegalArgumentException("birthdate is required");
    }
  }

  private void requireAuthentication(GraphqlRequestContext requestContext) {
    authSessionService.requireAuthenticatedUser(requestContext);
  }
}
