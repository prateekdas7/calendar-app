package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import org.junit.Test;

/**
 * Test suite for CalendarEvent class.
 */
public class CalendarEventTest {

  @Test
  public void testCreateEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        "Discussion", "Room 101", "public", null);

    assertEquals("Meeting", event.getSubject());
    assertEquals(start, event.getStartDateTime());
    assertEquals(end, event.getEndDateTime());
    assertEquals("Discussion", event.getDescription());
    assertEquals("Room 101", event.getLocation());
    assertEquals("public", event.getStatus());
    assertNull(event.getSeriesId());
    assertFalse(event.isPartOfSeries());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    new CalendarEvent(null, start, end, null, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptySubject() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    new CalendarEvent("", start, end, null, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullStartDateTime() {
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    new CalendarEvent("Meeting", null, end, null, null, null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEndBeforeStart() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 11, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 10, 0);
    new CalendarEvent("Meeting", start, end, null, null, null, null);
  }

  @Test
  public void testDefaultEndTime() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, null,
        null, null, null, null);

    assertEquals(17, event.getEndDateTime().getHour());
    assertEquals(0, event.getEndDateTime().getMinute());
  }

  @Test
  public void testDefaultStatus() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    assertEquals("public", event.getStatus());
  }

  @Test
  public void testWithSubject() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    CalendarEvent updated = event.withSubject("Important Meeting");

    assertEquals("Important Meeting", updated.getSubject());
    assertEquals("Meeting", event.getSubject());
  }


  @Test
  public void testWithEndDateTime() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    LocalDateTime newEnd = LocalDateTime.of(2025, 5, 1, 12, 0);
    CalendarEvent updated = event.withEndDateTime(newEnd);

    assertEquals(newEnd, updated.getEndDateTime());
  }

  @Test
  public void testWithDescription() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    CalendarEvent updated = event.withDescription("Team discussion");
    assertEquals("Team discussion", updated.getDescription());
  }

  @Test
  public void testWithLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    CalendarEvent updated = event.withLocation("Conference Room A");
    assertEquals("Conference Room A", updated.getLocation());
  }

  @Test
  public void testWithStatus() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    CalendarEvent updated = event.withStatus("private");
    assertEquals("private", updated.getStatus());
  }

  @Test
  public void testWithSeriesId() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    CalendarEvent updated = event.withSeriesId("series123");
    assertEquals("series123", updated.getSeriesId());
    assertTrue(updated.isPartOfSeries());
  }

  @Test
  public void testEquals() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end,
        null, null, null, null);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end,
        "Different description", "Different location", null, null);

    assertEquals(event1, event2);
  }

  @Test
  public void testNotEquals() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end,
        null, null, null, null);
    CalendarEvent event2 = new CalendarEvent("Different Meeting", start, end,
        null, null, null, null);

    assertNotEquals(event1, event2);
  }

  @Test
  public void testHashCode() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event1 = new CalendarEvent("Meeting", start, end,
        null, null, null, null);
    CalendarEvent event2 = new CalendarEvent("Meeting", start, end,
        "Different", "Different", null, null);

    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  public void testToString() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, "Room 101", null, null);

    String str = event.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("Room 101"));
  }

  @Test
  public void testImmutability() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);
    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    CalendarEvent modified = event.withSubject("New Subject");

    assertEquals("Meeting", event.getSubject());
    assertEquals("New Subject", modified.getSubject());
    assertNotSame(event, modified);
  }

  @Test
  public void testToStringWithLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, "Room 101", null, null);

    String str = event.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("starting on"));
    assertTrue(str.contains("ending on"));
    assertTrue(str.contains("at Room 101"));
  }

  @Test
  public void testToStringWithNullLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    String str = event.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("starting on"));
    assertTrue(str.contains("ending on"));
    assertFalse(str.contains(" at "));
  }

  @Test
  public void testToStringWithEmptyLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, "", null, null);

    String str = event.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("starting on"));
    assertTrue(str.contains("ending on"));
    assertFalse(str.contains(" at "));
  }

  @Test
  public void testToStringWithWhitespaceLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, "   ", null, null);

    String str = event.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains(" at    "));
  }

  @Test
  public void testToStringWithLongLocation() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event = new CalendarEvent("Meeting", start, end,
        null, "Building A, Floor 3, Conference Room 301", null, null);

    String str = event.toString();
    assertTrue(str.contains("Meeting"));
    assertTrue(str.contains("at Building A, Floor 3, Conference Room 301"));
  }

  @Test
  public void testToStringFormat() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent eventWithLocation = new CalendarEvent("Meeting", start, end,
        null, "Room 101", null, null);

    CalendarEvent eventWithoutLocation = new CalendarEvent("Meeting", start, end,
        null, null, null, null);

    String strWith = eventWithLocation.toString();
    String strWithout = eventWithoutLocation.toString();

    assertTrue(strWith.contains("Meeting"));
    assertTrue(strWithout.contains("Meeting"));

    assertTrue(strWith.contains("at Room 101"));
    assertFalse(strWithout.contains(" at "));
  }
}