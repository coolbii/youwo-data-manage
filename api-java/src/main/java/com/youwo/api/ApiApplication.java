package com.youwo.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.GraphQlAutoConfiguration;
import org.springframework.boot.autoconfigure.graphql.servlet.GraphQlWebMvcAutoConfiguration;

@SpringBootApplication(exclude = {
    FlywayAutoConfiguration.class,
    GraphQlAutoConfiguration.class,
    GraphQlWebMvcAutoConfiguration.class
})
public class ApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(ApiApplication.class, args);
  }
}
