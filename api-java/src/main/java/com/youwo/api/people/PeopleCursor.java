package com.youwo.api.people;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class PeopleCursor {
  private static final String PREFIX = "pc";
  private static final String SEP = "\n";

  private PeopleCursor() {}

  public static String encode(
      PeopleSortField sortField, SortDirection direction, String sortValue, UUID id) {
    String raw = String.join(SEP, PREFIX, sortField.name(), direction.name(), sortValue,
        id.toString());
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  public static DecodedCursor decode(String cursor) {
    try {
      String raw =
          new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
      String[] parts = raw.split(SEP, 5);
      if (parts.length != 5 || !PREFIX.equals(parts[0])) {
        throw new IllegalArgumentException("Invalid cursor format.");
      }
      PeopleSortField sortField = PeopleSortField.valueOf(parts[1]);
      SortDirection direction = SortDirection.valueOf(parts[2]);
      String sortValue = parts[3];
      UUID id = UUID.fromString(parts[4]);
      return new DecodedCursor(sortField, direction, sortValue, id);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Invalid cursor value.", e);
    }
  }

  public record DecodedCursor(
      PeopleSortField sortField, SortDirection direction, String sortValue, UUID id) {}
}
