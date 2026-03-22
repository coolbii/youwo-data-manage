package com.youwo.api.graphql;

import com.youwo.api.homework.HomeworkGraphqlController;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CodeFirstGraphqlConfig {
  @Bean
  public GraphQLSchema graphQLSchema(HomeworkGraphqlController homeworkGraphqlController) {
    return new GraphQLSchemaGenerator()
        .withOperationsFromSingleton(homeworkGraphqlController)
        .generate();
  }

  @Bean
  public GraphQL graphQL(GraphQLSchema graphQLSchema) {
    return GraphQL.newGraphQL(graphQLSchema).build();
  }
}
