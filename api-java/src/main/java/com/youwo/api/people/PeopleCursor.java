package com.youwo.api.people;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class PeopleCursor {
  private static final String OFFSET_PREFIX = "pc2";
  private static final String SEP = "\n";

  private PeopleCursor() {}

  public static String encodeOffset(
      PeopleSortField sortField, SortDirection direction, int offset) {
    String raw =
        String.join(
            SEP, OFFSET_PREFIX, sortField.name(), direction.name(), String.valueOf(offset));
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  public static DecodedOffsetCursor decodeOffset(String cursor) {
    try {
      String raw =
          new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
      String[] parts = raw.split(SEP, 4);
      if (parts.length != 4 || !OFFSET_PREFIX.equals(parts[0])) {
        throw new IllegalArgumentException("Invalid cursor format.");
      }
      PeopleSortField sortField = PeopleSortField.valueOf(parts[1]);
      SortDirection direction = SortDirection.valueOf(parts[2]);
      int offset = Integer.parseInt(parts[3]);
      if (offset < 0) {
        throw new IllegalArgumentException("Invalid cursor offset.");
      }
      return new DecodedOffsetCursor(sortField, direction, offset);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Invalid cursor value.", e);
    }
  }

  public record DecodedOffsetCursor(
      PeopleSortField sortField, SortDirection direction, int offset) {}
}
