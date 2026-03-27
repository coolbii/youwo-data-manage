import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client/core';

function resolveGraphqlEndpoint(): string {
  return '/api/graphql';
}

export const apolloClient = new ApolloClient({
  cache: new InMemoryCache(),
  link: new HttpLink({
    uri: resolveGraphqlEndpoint(),
  }),
});
