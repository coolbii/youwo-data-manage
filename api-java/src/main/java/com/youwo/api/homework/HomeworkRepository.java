package com.youwo.api.homework;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HomeworkRepository extends JpaRepository<HomeworkEntity, Integer> {
  @Query(
      value = """
        select *
        from homework
        order by created_at desc
        limit :limit
        offset :offset
      """,
      nativeQuery = true)
  List<HomeworkEntity> findHomeworkList(@Param("limit") int limit, @Param("offset") int offset);
}
