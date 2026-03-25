package com.youwo.api.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("${app.graphql.path:${spring.graphql.path:/api/graphql}}")
public class GraphqlHttpController {
  private static final String PLAYGROUND_HTML = """
      <!doctype html>
      <html lang="en">
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>GraphQL Sandbox</title>
        <style>
          html, body, #sandbox { width: 100%; height: 100%; margin: 0; }
        </style>
      </head>
      <body>
        <div id="sandbox"></div>
        <script src="https://embeddable-sandbox.cdn.apollographql.com/_latest/embeddable-sandbox.umd.production.min.js"></script>
        <script>
          const endpoint = window.location.pathname.replace(/\\/playground$/, "");
          new window.EmbeddedSandbox({
            target: "#sandbox",
            endpoint,
            initialState: {
              pollForSchemaUpdates: false
            }
          });
        </script>
      </body>
      </html>
      """;

  private final GraphQL graphQL;

  public GraphqlHttpController(GraphQL graphQL) {
    this.graphQL = graphQL;
  }

  @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
  public String playgroundAtRoot() {
    return PLAYGROUND_HTML;
  }

  @GetMapping(value = "/playground", produces = MediaType.TEXT_HTML_VALUE)
  public String playground() {
    return PLAYGROUND_HTML;
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
