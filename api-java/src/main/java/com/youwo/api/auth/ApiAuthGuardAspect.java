package com.youwo.api.auth;

import com.youwo.api.graphql.GraphqlRequestContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ApiAuthGuardAspect {
  private final AuthSessionService authSessionService;

  public ApiAuthGuardAspect(AuthSessionService authSessionService) {
    this.authSessionService = authSessionService;
  }

  @Around(
      "@within(com.youwo.api.auth.AuthGuarded)"
          + " && execution(public * *(..))"
          + " && !@annotation(com.youwo.api.auth.PublicApi)")
  public Object requireAuthentication(ProceedingJoinPoint joinPoint) throws Throwable {
    GraphqlRequestContext requestContext = resolveRequestContext(joinPoint.getArgs());
    authSessionService.requireAuthenticatedUser(requestContext);
    return joinPoint.proceed();
  }

  private static GraphqlRequestContext resolveRequestContext(Object[] args) {
    if (args != null) {
      for (Object arg : args) {
        if (arg instanceof GraphqlRequestContext requestContext) {
          return requestContext;
        }
      }
    }

    RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
    if (attributes instanceof ServletRequestAttributes servletAttributes) {
      HttpServletRequest request = servletAttributes.getRequest();
      HttpServletResponse response = servletAttributes.getResponse();
      return new GraphqlRequestContext(request, response);
    }

    throw new IllegalStateException("HTTP request context is required for auth guard");
  }
}
