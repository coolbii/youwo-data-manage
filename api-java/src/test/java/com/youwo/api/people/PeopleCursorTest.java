package com.youwo.api.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class PeopleCursorTest {
  @Test
  void encodesAndDecodesOffsetCursor() {
    String cursor =
        PeopleCursor.encodeOffset(PeopleSortField.NAME, SortDirection.DESC, 25);

    PeopleCursor.DecodedOffsetCursor decoded = PeopleCursor.decodeOffset(cursor);

    assertEquals(PeopleSortField.NAME, decoded.sortField());
    assertEquals(SortDirection.DESC, decoded.direction());
    assertEquals(25, decoded.offset());
  }

  @Test
  void rejectsLegacyCursorInOffsetDecoder() {
    String legacyCursor = encodeLegacyCursor("NAME", "ASC", "Alice");

    assertThrows(IllegalArgumentException.class, () -> PeopleCursor.decodeOffset(legacyCursor));
  }

  private static String encodeLegacyCursor(String sortField, String direction, String sortValue) {
    String raw = String.join(
        "\n",
        "pc",
        sortField,
        direction,
        sortValue,
        java.util.UUID.randomUUID().toString());
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }
}
