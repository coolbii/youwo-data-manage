package com.youwo.api.auth;

import com.youwo.api.graphql.GraphqlRequestContext;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLMutation;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import io.leangen.graphql.annotations.GraphQLRootContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AuthGraphqlController {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthSessionService authSessionService;

  public AuthGraphqlController(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthSessionService authSessionService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authSessionService = authSessionService;
  }

  @GraphQLMutation(name = "login")
  @Transactional
  public AuthSessionPayload login(
      @GraphQLArgument(name = "email") @GraphQLNonNull String email,
      @GraphQLArgument(name = "password") @GraphQLNonNull String password,
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    String normalizedEmail = normalizeEmail(email);
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("password is required");
    }

    UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
        .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid email or password");
    }

    return authSessionService.issueSession(user, requestContext);
  }

  @GraphQLMutation(name = "logout")
  @Transactional
  public boolean logout(
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    UserEntity user = authSessionService.requireAuthenticatedUser(requestContext);
    user.setRefreshTokenHash(null);
    userRepository.save(user);
    authSessionService.clearSessionCookies(requestContext);
    return true;
  }

  @GraphQLQuery(name = "me")
  @Transactional(readOnly = true)
  public AuthUserPayload me(
      @GraphQLRootContext("") GraphqlRequestContext requestContext) {
    UserEntity user = authSessionService.requireAuthenticatedUser(requestContext);
    return AuthUserPayload.fromEntity(user);
  }

  private static String normalizeEmail(String email) {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("email is required");
    }
    return email.trim().toLowerCase();
  }
}
