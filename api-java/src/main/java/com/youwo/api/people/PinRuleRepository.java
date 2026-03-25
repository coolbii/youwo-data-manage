package com.youwo.api.people;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PinRuleRepository extends JpaRepository<PinRuleEntity, UUID> {
  @Query("SELECT pr FROM PinRuleEntity pr JOIN FETCH pr.person ORDER BY pr.targetPosition ASC")
  List<PinRuleEntity> findAllWithPerson();
}
