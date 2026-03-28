package com.youwo.api.people;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class PersonRepositoryImpl implements PersonRepositoryCustom {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public List<PersonEntity> findPeople(
      String search, PeopleSortField sortField, SortDirection direction) {
    StringBuilder jpql = new StringBuilder("SELECT p FROM PersonEntity p WHERE 1=1");
    Map<String, Object> params = new HashMap<>();

    appendSearchFilter(jpql, params, search);
    appendOrderBy(jpql, sortField, direction);

    TypedQuery<PersonEntity> query =
        entityManager.createQuery(jpql.toString(), PersonEntity.class);
    params.forEach(query::setParameter);
    return query.getResultList();
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
}
