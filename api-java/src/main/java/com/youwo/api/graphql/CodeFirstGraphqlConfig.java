package com.youwo.api.graphql;

import com.youwo.api.auth.AuthGraphqlController;
import com.youwo.api.people.PeopleGraphqlController;
import com.youwo.api.people.PinRulesGraphqlController;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeFirstGraphqlConfig {
  @Bean
  public GraphQLSchema graphQLSchema(
      AuthGraphqlController authGraphqlController,
      PeopleGraphqlController peopleGraphqlController,
      PinRulesGraphqlController pinRulesGraphqlController) {
    return new GraphQLSchemaGenerator()
        .withOperationsFromSingleton(authGraphqlController, AuthGraphqlController.class)
        .withOperationsFromSingleton(peopleGraphqlController, PeopleGraphqlController.class)
        .withOperationsFromSingleton(pinRulesGraphqlController, PinRulesGraphqlController.class)
        .generate();
  }

  @Bean
  public GraphQL graphQL(GraphQLSchema graphQLSchema) {
    return GraphQL.newGraphQL(graphQLSchema).build();
  }
}
