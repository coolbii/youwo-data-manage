package com.youwo.api.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {
  @GetMapping("/health")
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }

  @GetMapping
  public Map<String, String> info() {
    return Map.of(
        "message", "Youwo Homework API is running.",
        "graphqlEndpoint", "/api/graphql");
  }
}
