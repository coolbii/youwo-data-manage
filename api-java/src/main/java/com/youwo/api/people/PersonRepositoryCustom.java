package com.youwo.api.people;

import java.util.List;

public interface PersonRepositoryCustom {
  List<PersonEntity> findPeople(
      String search, PeopleSortField sortField, SortDirection direction);
}
