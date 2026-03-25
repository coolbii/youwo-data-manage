package com.youwo.api.people;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

public record PersonPayload(
    UUID id,
    String name,
    String position,
    String location,
    Integer age,
    String birthdate,
    String createdAt,
    String updatedAt) {

  public static PersonPayload fromEntity(PersonEntity entity) {
    return new PersonPayload(
        entity.getId(),
        entity.getName(),
        entity.getPosition(),
        entity.getLocation(),
        calculateAge(entity.getBirthdate()),
        entity.getBirthdate() == null ? null : entity.getBirthdate().toString(),
        entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
        entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString());
  }

  private static Integer calculateAge(LocalDate birthdate) {
    if (birthdate == null) {
      return null;
    }
    LocalDate today = LocalDate.now();
    if (birthdate.isAfter(today)) {
      return null;
    }
    return Period.between(birthdate, today).getYears();
  }
}
