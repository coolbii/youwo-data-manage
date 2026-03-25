package com.youwo.api.graphql;

import com.youwo.api.people.PeopleGraphqlController;
import com.youwo.api.people.PinRulesGraphqlController;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.leangen.graphql.GraphQLSchemaGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class SchemaExportMain {
  private static final String DEFAULT_OUTPUT =
      "api-java/src/main/resources/graphql/schema.graphqls";

  private SchemaExportMain() {}

  public static void main(String[] args) throws IOException {
    String output = args.length > 0 ? args[0] : DEFAULT_OUTPUT;

    GraphQLSchema schema = new GraphQLSchemaGenerator()
        .withOperationsFromType(PeopleGraphqlController.class)
        .withOperationsFromType(PinRulesGraphqlController.class)
        .generate();

    SchemaPrinter.Options options = SchemaPrinter.Options.defaultOptions()
        .includeScalarTypes(true)
        .includeSchemaDefinition(true);

    String header = "# GENERATED FILE. DO NOT EDIT MANUALLY.\n";
    String sdl = new SchemaPrinter(options).print(schema);

    Path outputPath = Paths.get(output);
    Files.createDirectories(outputPath.getParent());
    Files.writeString(outputPath, header + sdl);

    System.out.println("Generated GraphQL schema at " + outputPath.toAbsolutePath());
  }
}
