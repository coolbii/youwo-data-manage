package com.youwo.api.homework;

import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLQuery;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HomeworkGraphqlController {
  private final HomeworkRepository repository;

  public HomeworkGraphqlController(HomeworkRepository repository) {
    this.repository = repository;
  }

  @GraphQLQuery(name = "homeworkList")
  public List<HomeworkPayload> homeworkList(
      @GraphQLArgument(name = "limit") Integer limit,
      @GraphQLArgument(name = "offset") Integer offset) {
    int safeLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), 200);
    int safeOffset = offset == null ? 0 : Math.max(offset, 0);

    return repository.findHomeworkList(safeLimit, safeOffset).stream()
        .map(HomeworkPayload::fromEntity)
        .toList();
  }

  @GraphQLQuery(name = "homework")
  public HomeworkPayload homework(@GraphQLArgument(name = "id") Integer id) {
    return repository.findById(id).map(HomeworkPayload::fromEntity).orElse(null);
  }

  @GraphQLMutation(name = "createHomework")
  public HomeworkPayload createHomework(
      @GraphQLArgument(name = "title") String title,
      @GraphQLArgument(name = "description") String description) {
    HomeworkEntity entity = new HomeworkEntity();
    entity.setTitle(title);
    entity.setDescription(description);

    return HomeworkPayload.fromEntity(repository.save(entity));
  }

  @GraphQLMutation(name = "updateHomework")
  public HomeworkPayload updateHomework(
      @GraphQLArgument(name = "id") Integer id,
      @GraphQLArgument(name = "title") String title,
      @GraphQLArgument(name = "description") String description,
      @GraphQLArgument(name = "completed") Boolean completed) {
    HomeworkEntity existing = repository.findById(id).orElse(null);
    if (existing == null) {
      return null;
    }

    if (title != null) {
      existing.setTitle(title);
    }

    if (description != null) {
      existing.setDescription(description);
    }

    if (completed != null) {
      existing.setCompleted(completed);
    }

    return HomeworkPayload.fromEntity(repository.save(existing));
  }

  @GraphQLMutation(name = "deleteHomework")
  public Boolean deleteHomework(@GraphQLArgument(name = "id") Integer id) {
    if (!repository.existsById(id)) {
      return false;
    }

    repository.deleteById(id);
    return true;
  }
}
