package com.youwo.api.people;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepositoryImpl implements PersonRepositoryCustom {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<PersonEntity> findPeople(
      String search,
      PeopleSortField sortField,
      SortDirection direction,
      String afterSortValue,
      UUID afterId,
      int limit) {
    StringBuilder jpql = new StringBuilder("SELECT p FROM PersonEntity p WHERE 1=1");
    Map<String, Object> params = new HashMap<>();

    appendSearchFilter(jpql, params, search);
    appendCursorFilter(jpql, params, sortField, direction, afterSortValue, afterId);
    appendOrderBy(jpql, sortField, direction);

    TypedQuery<PersonEntity> query =
        entityManager.createQuery(jpql.toString(), PersonEntity.class);
    params.forEach(query::setParameter);
    query.setMaxResults(limit);
    return query.getResultList();
  }

  @Override
  public long countPeople(String search) {
    StringBuilder jpql = new StringBuilder("SELECT COUNT(p) FROM PersonEntity p WHERE 1=1");
    Map<String, Object> params = new HashMap<>();
    appendSearchFilter(jpql, params, search);

    TypedQuery<Long> query = entityManager.createQuery(jpql.toString(), Long.class);
    params.forEach(query::setParameter);
    return query.getSingleResult();
  }

  private void appendSearchFilter(
      StringBuilder jpql, Map<String, Object> params, String search) {
    if (search != null && !search.isBlank()) {
      jpql.append(
          " AND (LOWER(p.name) LIKE :search"
              + " OR LOWER(p.position) LIKE :search"
              + " OR LOWER(p.location) LIKE :search)");
      params.put("search", "%" + search.trim().toLowerCase() + "%");
    }
  }

  private void appendCursorFilter(
      StringBuilder jpql,
      Map<String, Object> params,
      PeopleSortField sortField,
      SortDirection direction,
      String afterSortValue,
      UUID afterId) {
    if (afterSortValue == null || afterId == null) {
      return;
    }
    String col = toJpqlPath(sortField);
    String cmp = direction == SortDirection.ASC ? ">" : "<";
    jpql.append(" AND (")
        .append(col).append(" ").append(cmp).append(" :sv")
        .append(" OR (").append(col).append(" = :sv AND p.id ").append(cmp).append(" :aid))");
    params.put("sv", parseSortValue(sortField, afterSortValue));
    params.put("aid", afterId);
  }

  private void appendOrderBy(
      StringBuilder jpql, PeopleSortField sortField, SortDirection direction) {
    String dir = direction.name();
    jpql.append(" ORDER BY ")
        .append(toJpqlPath(sortField)).append(" ").append(dir)
        .append(", p.id ").append(dir);
  }

  private String toJpqlPath(PeopleSortField field) {
    return switch (field) {
      case NAME -> "p.name";
      case POSITION -> "p.position";
      case LOCATION -> "p.location";
      case BIRTHDATE -> "p.birthdate";
      case CREATED_AT -> "p.createdAt";
    };
  }

  private Object parseSortValue(PeopleSortField field, String value) {
    return switch (field) {
      case NAME, POSITION, LOCATION -> value;
      case BIRTHDATE -> LocalDate.parse(value);
      case CREATED_AT -> OffsetDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
    };
  }
}
