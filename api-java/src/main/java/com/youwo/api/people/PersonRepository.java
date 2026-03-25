package com.youwo.api.people;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<PersonEntity, UUID>, PersonRepositoryCustom {}
