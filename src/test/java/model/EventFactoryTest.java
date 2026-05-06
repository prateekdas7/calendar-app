package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/**
 * Comprehensive test suite for EventFactory.
 */
public class EventFactoryTest {

  @Test
  public void testCreateSingleEvent() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 1, 11, 0);

    CalendarEvent event = EventFactory.createSingleEvent("Meeting", start, end, null, null);

    assertEquals("Meeting", event.getSubject());
    assertEquals(start, event.getStartDateTime());
    assertEquals(end, event.getEndDateTime());
    assertNull(event.getSeriesId());
    assertFalse(event.isPartOfSeries());
    assertEquals("public", event.getStatus());
  }

  @Test
  public void testCreateSingleEventMultiDay() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 3, 17, 0);

    CalendarEvent event = EventFactory.createSingleEvent("Conference", start, end, null, null);

    assertEquals("Conference", event.getSubject());
    assertEquals(start, event.getStartDateTime());
    assertEquals(end, event.getEndDateTime());
  }

  @Test
  public void testCreateAllDayEvent() {
    LocalDate date = LocalDate.of(2025, 5, 1);

    CalendarEvent event = EventFactory.createAllDayEvent("Holiday", date);

    assertEquals("Holiday", event.getSubject());
    assertEquals(0, event.getStartDateTime().getHour());    // Changed from 8
    assertEquals(0, event.getStartDateTime().getMinute());
    assertEquals(23, event.getEndDateTime().getHour());     // Changed from 17
    assertEquals(59, event.getEndDateTime().getMinute());   // Changed from 0
    assertEquals(date, event.getStartDateTime().toLocalDate());
    assertEquals(date, event.getEndDateTime().toLocalDate());
  }

  @Test
  public void testCreateEventSeriesMwf() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MWF");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup", start, end, days, 6
    );

    assertEquals(6, series.size());

    String seriesId = series.get(0).getSeriesId();
    assertNotNull(seriesId);
    for (CalendarEvent event : series) {
      assertEquals(seriesId, event.getSeriesId());
      assertEquals("Standup", event.getSubject());
      assertEquals(9, event.getStartDateTime().getHour());
      assertEquals(0, event.getStartDateTime().getMinute());
    }
  }

  @Test
  public void testCreateEventSeriesDailyMtwrf() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MTWRF");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Daily Meeting", start, end, days, 10
    );

    assertEquals(10, series.size());

    LocalDate expectedDate = LocalDate.of(2025, 5, 5);
    for (CalendarEvent event : series) {
      while (!days.contains(expectedDate.getDayOfWeek())) {
        expectedDate = expectedDate.plusDays(1);
      }
      assertEquals(expectedDate, event.getStartDateTime().toLocalDate());
      expectedDate = expectedDate.plusDays(1);
    }
  }


  @Test
  public void testCreateEventSeriesWeekend() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 3, 10, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 3, 11, 0);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("SU");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Weekend Activity", start, end, days, 6
    );

    assertEquals(6, series.size());

    for (CalendarEvent event : series) {
      DayOfWeek day = event.getStartDateTime().getDayOfWeek();
      assertTrue(day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);
    }
  }

  @Test
  public void testCreateEventSeriesZeroOccurrences() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("M");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Meeting", start, end, days, 0
    );

    assertTrue(series.isEmpty());
  }

  @Test
  public void testCreateEventSeriesUntil() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MW");
    LocalDate untilDate = LocalDate.of(2025, 5, 14);

    List<CalendarEvent> series = EventFactory.createEventSeriesUntil(
        "Standup", start, end, days, untilDate
    );

    assertEquals(4, series.size());

    for (CalendarEvent event : series) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      assertFalse(eventDate.isAfter(untilDate));
    }
  }

  @Test
  public void testCreateEventSeriesUntilSameDay() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("M");
    LocalDate untilDate = LocalDate.of(2025, 5, 5);

    List<CalendarEvent> series = EventFactory.createEventSeriesUntil(
        "Meeting", start, end, days, untilDate
    );

    assertEquals(1, series.size());
  }

  @Test
  public void testCreateEventSeriesUntilBeforeStart() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("M");
    LocalDate untilDate = LocalDate.of(2025, 5, 1);

    List<CalendarEvent> series = EventFactory.createEventSeriesUntil(
        "Meeting", start, end, days, untilDate
    );

    assertTrue(series.isEmpty());
  }

  @Test
  public void testCreateEventSeriesUntilLongRange() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("M");
    LocalDate untilDate = LocalDate.of(2025, 12, 31);

    List<CalendarEvent> series = EventFactory.createEventSeriesUntil(
        "Weekly Meeting", start, end, days, untilDate
    );

    assertTrue(series.size() >= 34);
    assertTrue(series.size() <= 35);
  }

  @Test
  public void testCreateAllDayEventSeries() {
    LocalDate startDate = LocalDate.of(2025, 5, 10);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("S");

    List<CalendarEvent> series = EventFactory.createAllDayEventSeries(
        "Weekend Hike", startDate, days, 4
    );

    assertEquals(4, series.size());

    for (CalendarEvent event : series) {
      assertEquals(0, event.getStartDateTime().getHour());   // Changed from 8
      assertEquals(23, event.getEndDateTime().getHour());    // Changed from 17
      assertEquals(DayOfWeek.SATURDAY, event.getStartDateTime().getDayOfWeek());
    }
  }

  @Test
  public void testCreateAllDayEventSeriesUntil() {
    LocalDate startDate = LocalDate.of(2025, 5, 5);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MWF");
    LocalDate untilDate = LocalDate.of(2025, 5, 16);

    List<CalendarEvent> series = EventFactory.createAllDayEventSeriesUntil(
        "Training", startDate, days, untilDate
    );

    assertEquals(6, series.size());

    for (CalendarEvent event : series) {
      assertEquals(0, event.getStartDateTime().getHour());   // Changed from 8
      assertEquals(23, event.getEndDateTime().getHour());    // Changed from 17
    }
  }

  @Test
  public void testParseWeekdaysMonday() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("M");
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
  }

  @Test
  public void testParseWeekdaysTuesday() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("T");
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.TUESDAY));
  }

  @Test
  public void testParseWeekdaysWednesday() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("W");
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
  }

  @Test
  public void testParseWeekdaysThursday() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("R");
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.THURSDAY));
  }

  @Test
  public void testParseWeekdaysFriday() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("F");
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseWeekdaysSaturday() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("S");
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.SATURDAY));
  }

  @Test
  public void testParseWeekdaysSunday() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("U");
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.SUNDAY));
  }

  @Test
  public void testParseWeekdaysMwf() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MWF");
    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseWeekdaysMtwrf() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MTWRF");
    assertEquals(5, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.TUESDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.THURSDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseWeekdaysAllDays() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MTWRFSU");
    assertEquals(7, days.size());
  }

  @Test
  public void testParseWeekdaysLowercase() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("mwf");
    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseWeekdaysMixedCase() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MwF");
    assertEquals(3, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
    assertTrue(days.contains(DayOfWeek.WEDNESDAY));
    assertTrue(days.contains(DayOfWeek.FRIDAY));
  }

  @Test
  public void testParseWeekdaysDuplicates() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MMM");
    assertEquals(1, days.size());
    assertTrue(days.contains(DayOfWeek.MONDAY));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWeekdaysInvalidCharacter() {
    EventFactory.parseWeekdays("MXF");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParseWeekdaysInvalidNumber() {
    EventFactory.parseWeekdays("M123");
  }

  @Test
  public void testParseWeekdaysEmpty() {
    Set<DayOfWeek> days = EventFactory.parseWeekdays("");
    assertTrue(days.isEmpty());
  }

  @Test
  public void testSeriesHaveUniqueIds() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("M");

    List<CalendarEvent> series1 = EventFactory.createEventSeries(
        "Meeting1", start, end, days, 2
    );
    List<CalendarEvent> series2 = EventFactory.createEventSeries(
        "Meeting2", start, end, days, 2
    );

    String seriesId1 = series1.get(0).getSeriesId();
    String seriesId2 = series2.get(0).getSeriesId();

    assertNotEquals(seriesId1, seriesId2);
  }

  @Test
  public void testAllEventsInSeriesHaveSameId() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MWF");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup", start, end, days, 10
    );

    String firstId = series.get(0).getSeriesId();
    for (CalendarEvent event : series) {
      assertEquals(firstId, event.getSeriesId());
    }
  }

  @Test
  public void testSeriesPreservesTime() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 5, 14, 30);
    LocalDateTime end = LocalDateTime.of(2025, 5, 5, 15, 45);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("MW");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Meeting", start, end, days, 4
    );

    for (CalendarEvent event : series) {
      assertEquals(14, event.getStartDateTime().getHour());
      assertEquals(30, event.getStartDateTime().getMinute());
      assertEquals(15, event.getEndDateTime().getHour());
      assertEquals(45, event.getEndDateTime().getMinute());
    }
  }

  @Test
  public void testCreateSeriesStartingOnNonMatchingDay() {

    LocalDateTime start = LocalDateTime.of(2025, 5, 6, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 6, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("M");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Monday Meeting", start, end, days, 3
    );

    assertEquals(3, series.size());
    assertEquals(LocalDate.of(2025, 5, 12), series.get(0).getStartDateTime().toLocalDate());
  }

  @Test
  public void testCreateSeriesAcrossMonthBoundary() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 26, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 26, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("M");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Weekly", start, end, days, 3
    );

    assertEquals(3, series.size());
    assertEquals(5, series.get(0).getStartDateTime().getMonthValue());
    assertEquals(6, series.get(1).getStartDateTime().getMonthValue());
    assertEquals(6, series.get(2).getStartDateTime().getMonthValue());
  }

  @Test
  public void testCreateSeriesAcrossYearBoundary() {
    LocalDateTime start = LocalDateTime.of(2025, 12, 29, 9, 0);
    LocalDateTime end = LocalDateTime.of(2025, 12, 29, 9, 30);
    Set<DayOfWeek> days = EventFactory.parseWeekdays("M");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Weekly", start, end, days, 3
    );

    assertEquals(3, series.size());
    assertEquals(2025, series.get(0).getStartDateTime().getYear());
    assertEquals(2026, series.get(1).getStartDateTime().getYear());
    assertEquals(2026, series.get(2).getStartDateTime().getYear());
  }
}