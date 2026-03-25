package com.youwo.api.people;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;

@Component
public class PeopleGraphqlController {

    private final PersonRepository repository;

    public PeopleGraphqlController(PersonRepository repository) {
        this.repository = repository;
    }

    @GraphQLQuery(name = "peopleList")
    public PeopleConnectionPayload peopleList(
            @GraphQLArgument(name = "first") Integer first,
            @GraphQLArgument(name = "after") String after,
            @GraphQLArgument(name = "search") String search,
            @GraphQLArgument(name = "sortBy") PeopleSortField sortBy,
            @GraphQLArgument(name = "sortDirection") SortDirection sortDirection) {
        int safeFirst = first == null ? 100 : Math.min(Math.max(first, 1), 100);
        PeopleSortField safeSortBy = sortBy == null ? PeopleSortField.CREATED_AT : sortBy;
        SortDirection safeSortDir = sortDirection == null ? SortDirection.ASC : sortDirection;

        String afterSortValue = null;
        UUID afterId = null;
        if (after != null) {
            PeopleCursor.DecodedCursor decoded = PeopleCursor.decode(after);
            if (decoded.sortField() == safeSortBy && decoded.direction() == safeSortDir) {
                afterSortValue = decoded.sortValue();
                afterId = decoded.id();
            }
        }

        List<PersonEntity> fetched
                = repository.findPeople(search, safeSortBy, safeSortDir, afterSortValue, afterId,
                        safeFirst + 1);

        boolean hasNextPage = fetched.size() > safeFirst;
        List<PersonEntity> page = hasNextPage ? fetched.subList(0, safeFirst) : fetched;

        String endCursor = page.isEmpty() ? null
                : PeopleCursor.encode(safeSortBy, safeSortDir,
                        extractSortValue(page.getLast(), safeSortBy), page.getLast().getId());

        long totalCount = repository.countPeople(search);

        return new PeopleConnectionPayload(
                page.stream().map(PersonPayload::fromEntity).toList(),
                new PeoplePageInfoPayload(endCursor, hasNextPage),
                totalCount);
    }

    @GraphQLQuery(name = "person")
    public PersonPayload person(@GraphQLArgument(name = "id") UUID id) {
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
            @GraphQLArgument(name = "birthdate") String birthdate) {
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
            @GraphQLArgument(name = "birthdate") String birthdate) {
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
    public boolean deletePerson(@GraphQLArgument(name = "id") UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Person not found: " + id);
        }
        repository.deleteById(id);
        return true;
    }

    private String extractSortValue(PersonEntity entity, PeopleSortField sortField) {
        return switch (sortField) {
            case NAME ->
                entity.getName();
            case POSITION ->
                entity.getPosition();
            case LOCATION ->
                entity.getLocation();
            case BIRTHDATE ->
                entity.getBirthdate().toString();
            case CREATED_AT ->
                entity.getCreatedAt().toInstant().toString();
        };
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
}
