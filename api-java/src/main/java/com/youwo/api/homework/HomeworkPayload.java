package com.youwo.api.homework;

public record HomeworkPayload(
    Integer id,
    String title,
    String description,
    Boolean completed,
    String createdAt,
    String updatedAt) {
  public static HomeworkPayload fromEntity(HomeworkEntity entity) {
    return new HomeworkPayload(
        entity.getId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getCompleted(),
        entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString(),
        entity.getUpdatedAt() == null ? null : entity.getUpdatedAt().toString());
  }
}
