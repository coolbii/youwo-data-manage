package com.youwo.api.people;

import java.util.List;
import java.util.UUID;

public interface PersonRepositoryCustom {
  List<PersonEntity> findPeople(
      String search,
      PeopleSortField sortField,
      SortDirection direction,
      String afterSortValue,
      UUID afterId,
      int limit);

  long countPeople(String search);
}
