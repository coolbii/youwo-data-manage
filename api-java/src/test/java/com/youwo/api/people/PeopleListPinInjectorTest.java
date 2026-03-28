package com.youwo.api.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PeopleListPinInjectorTest {
  @Test
  void injectsPinsAndKeepsCursorContinuityAcrossPages() {
    PersonEntity alice = person("00000000-0000-0000-0000-000000000001", "Alice");
    PersonEntity bob = person("00000000-0000-0000-0000-000000000002", "Bob");
    PersonEntity carol = person("00000000-0000-0000-0000-000000000003", "Carol");
    PersonEntity dave = person("00000000-0000-0000-0000-000000000004", "Dave");

    List<PersonEntity> ordered = List.of(alice, bob, carol, dave);
    List<PinRuleEntity> rules = List.of(
        rule("10000000-0000-0000-0000-000000000001", bob, 1, true, "2026-01-01T00:00:00Z"));

    PeopleListPinInjector.PinInjectedPage firstPage =
        PeopleListPinInjector.injectAndPage(ordered, rules, 2, 0);

    assertEquals(List.of("Bob", "Alice"), names(firstPage.nodes()));
    assertEquals(4, firstPage.totalCount());
    assertTrue(firstPage.hasNextPage());
    assertEquals(2, firstPage.endOffset());

    PeopleListPinInjector.PinInjectedPage secondPage =
        PeopleListPinInjector.injectAndPage(ordered, rules, 2, firstPage.endOffset());

    assertEquals(List.of("Carol", "Dave"), names(secondPage.nodes()));
    assertEquals(4, secondPage.totalCount());
    assertFalse(secondPage.hasNextPage());
    assertEquals(4, secondPage.endOffset());
  }

  @Test
  void ignoresPinRulesForPeopleNotInCurrentResult() {
    PersonEntity alice = person("00000000-0000-0000-0000-000000000011", "Alice");
    PersonEntity bob = person("00000000-0000-0000-0000-000000000012", "Bob");
    PersonEntity zoe = person("00000000-0000-0000-0000-000000000013", "Zoe");

    List<PersonEntity> ordered = List.of(alice, bob);
    List<PinRuleEntity> rules = List.of(
        rule("10000000-0000-0000-0000-000000000011", zoe, 1, true, "2026-01-01T00:00:00Z"));

    PeopleListPinInjector.PinInjectedPage page =
        PeopleListPinInjector.injectAndPage(ordered, rules, 10, 0);

    assertEquals(List.of("Alice", "Bob"), names(page.nodes()));
    assertEquals(2, page.totalCount());
    assertFalse(page.hasNextPage());
    assertEquals(2, page.endOffset());
  }

  @Test
  void keepsNaturalOrderWhenRuleIsDisabled() {
    PersonEntity alice = person("00000000-0000-0000-0000-000000000021", "Alice");
    PersonEntity bob = person("00000000-0000-0000-0000-000000000022", "Bob");
    PersonEntity carol = person("00000000-0000-0000-0000-000000000023", "Carol");

    PeopleListPinInjector.PinInjectedPage page = PeopleListPinInjector.injectAndPage(
        List.of(alice, bob, carol),
        List.of(rule("10000000-0000-0000-0000-000000000021", carol, 1, false, "2026-01-01T00:00:00Z")),
        10,
        0);

    assertEquals(List.of("Alice", "Bob", "Carol"), names(page.nodes()));
  }

  @Test
  void clampsTargetBeyondTotalToResultEnd() {
    PersonEntity alice = person("00000000-0000-0000-0000-000000000031", "Alice");
    PersonEntity bob = person("00000000-0000-0000-0000-000000000032", "Bob");
    PersonEntity carol = person("00000000-0000-0000-0000-000000000033", "Carol");

    PeopleListPinInjector.PinInjectedPage page = PeopleListPinInjector.injectAndPage(
        List.of(alice, bob, carol),
        List.of(rule("10000000-0000-0000-0000-000000000031", alice, 99, true, "2026-01-01T00:00:00Z")),
        10,
        0);

    assertEquals(List.of("Bob", "Carol", "Alice"), names(page.nodes()));
  }

  @Test
  void shiftsConflictingTargetsByPriority() {
    PersonEntity alice = person("00000000-0000-0000-0000-000000000041", "Alice");
    PersonEntity bob = person("00000000-0000-0000-0000-000000000042", "Bob");
    PersonEntity carol = person("00000000-0000-0000-0000-000000000043", "Carol");
    PersonEntity dave = person("00000000-0000-0000-0000-000000000044", "Dave");

    List<PinRuleEntity> rules = List.of(
        rule("10000000-0000-0000-0000-000000000041", bob, 1, true, "2026-01-01T00:00:00Z"),
        rule("10000000-0000-0000-0000-000000000042", carol, 1, true, "2026-01-01T00:00:01Z"));

    PeopleListPinInjector.PinInjectedPage page = PeopleListPinInjector.injectAndPage(
        List.of(alice, bob, carol, dave),
        rules,
        10,
        0);

    assertEquals(List.of("Bob", "Carol", "Alice", "Dave"), names(page.nodes()));
  }

  @Test
  void keepsStableOutputWhenOverflowRuleBecomesNoMatch() {
    PersonEntity alice = person("00000000-0000-0000-0000-000000000051", "Alice");
    PersonEntity bob = person("00000000-0000-0000-0000-000000000052", "Bob");
    PersonEntity carol = person("00000000-0000-0000-0000-000000000053", "Carol");

    List<PinRuleEntity> rules = List.of(
        rule("10000000-0000-0000-0000-000000000051", bob, 1, true, "2026-01-01T00:00:00Z"),
        rule("10000000-0000-0000-0000-000000000052", carol, 2, true, "2026-01-01T00:00:01Z"),
        rule("10000000-0000-0000-0000-000000000053", alice, 3, true, "2026-01-01T00:00:02Z"),
        rule("10000000-0000-0000-0000-000000000054", alice, 9, true, "2026-01-01T00:00:03Z"));

    PeopleListPinInjector.PinInjectedPage page = PeopleListPinInjector.injectAndPage(
        List.of(alice, bob, carol),
        rules,
        10,
        0);

    assertEquals(List.of("Bob", "Carol", "Alice"), names(page.nodes()));
    assertEquals(3, page.nodes().size());
  }

  @Test
  void returnsEmptyWhenAfterOffsetIsOutOfRange() {
    PersonEntity alice = person("00000000-0000-0000-0000-000000000061", "Alice");
    PersonEntity bob = person("00000000-0000-0000-0000-000000000062", "Bob");

    PeopleListPinInjector.PinInjectedPage page = PeopleListPinInjector.injectAndPage(
        List.of(alice, bob),
        List.of(),
        10,
        2);

    assertTrue(page.nodes().isEmpty());
    assertEquals(2, page.totalCount());
    assertFalse(page.hasNextPage());
    assertNull(page.endOffset());
  }

  @Test
  void returnsEmptyForEmptyInputList() {
    PeopleListPinInjector.PinInjectedPage page =
        PeopleListPinInjector.injectAndPage(List.of(), List.of(), 10, 0);

    assertTrue(page.nodes().isEmpty());
    assertEquals(0, page.totalCount());
    assertFalse(page.hasNextPage());
    assertNull(page.endOffset());
  }

  private static List<String> names(List<PersonEntity> nodes) {
    return nodes.stream().map(PersonEntity::getName).toList();
  }

  private static PersonEntity person(String id, String name) {
    PersonEntity person = new PersonEntity();
    person.setName(name);
    person.setPosition("Engineer");
    person.setLocation("Taipei");
    person.setBirthdate(LocalDate.parse("1990-01-01"));
    setField(person, "id", UUID.fromString(id));
    return person;
  }

  private static PinRuleEntity rule(
      String id, PersonEntity person, int targetPosition, boolean enabled, String createdAt) {
    PinRuleEntity rule = new PinRuleEntity();
    rule.setPerson(person);
    rule.setTargetPosition(targetPosition);
    rule.setEnabled(enabled);
    setField(rule, "id", UUID.fromString(id));
    setField(rule, "createdAt", OffsetDateTime.parse(createdAt));
    return rule;
  }

  private static void setField(Object target, String fieldName, Object value) {
    try {
      Field field = target.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(target, value);
    } catch (ReflectiveOperationException ex) {
      throw new IllegalStateException("Unable to set field: " + fieldName, ex);
    }
  }
}
