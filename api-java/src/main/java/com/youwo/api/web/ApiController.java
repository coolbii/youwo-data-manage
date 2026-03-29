package com.youwo.api.web;

import com.youwo.api.auth.AuthGuarded;
import com.youwo.api.auth.PublicApi;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AuthGuarded
public class ApiController {
  @GetMapping("/health")
  @PublicApi
  public Map<String, String> health() {
    return Map.of("status", "ok");
  }

  @GetMapping
  public Map<String, String> info() {
    return Map.of(
        "message", "Youwo Data Manage API is running.",
        "graphqlEndpoint", "/api/graphql");
  }
}
