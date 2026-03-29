package com.youwo.api.graphql;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public record GraphqlRequestContext(
    HttpServletRequest httpServletRequest,
    HttpServletResponse httpServletResponse) {
  public String header(String name) {
    if (httpServletRequest == null || name == null || name.isBlank()) {
      return null;
    }
    String value = httpServletRequest.getHeader(name);
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  public String cookie(String name) {
    if (httpServletRequest == null || name == null || name.isBlank()) {
      return null;
    }

    Cookie[] cookies = httpServletRequest.getCookies();
    if (cookies == null) {
      return null;
    }

    for (Cookie cookie : cookies) {
      if (name.equals(cookie.getName())) {
        String value = cookie.getValue();
        if (value == null || value.isBlank()) {
          return null;
        }
        return value;
      }
    }

    return null;
  }
}
