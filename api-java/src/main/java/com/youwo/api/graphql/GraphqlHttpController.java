package com.youwo.api.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("${app.graphql.path:${spring.graphql.path:/api/graphql}}")
public class GraphqlHttpController {
  private final GraphQL graphQL;

  public GraphqlHttpController(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @PostMapping
  public Map<String, Object> execute(@RequestBody GraphqlRequest request) {
    if (request == null || request.query() == null || request.query().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GraphQL query is required");
    }

    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
        .query(request.query())
        .operationName(request.operationName())
        .variables(request.safeVariables())
        .build();

    ExecutionResult result = graphQL.execute(executionInput);
    return result.toSpecification();
  }

  public record GraphqlRequest(
      String query,
      String operationName,
      Map<String, Object> variables) {
    public Map<String, Object> safeVariables() {
      if (variables == null) {
        return Collections.emptyMap();
      }
      return variables;
    }
  }
}
