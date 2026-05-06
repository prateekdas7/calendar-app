package model;

import static org.junit.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test suite for MultiCalendarModelImpl.
 */
public class MultiCalendarModelImplTest {

  private MultiCalendarModelImpl multi;
  private MultiCalendarModelImpl multi2; // For copy events tests

  /**
   * The multi calendar model instance used for testing.
   */
  @Before
  public void setUp() {
    multi = new MultiCalendarModelImpl();
    multi2 = new MultiCalendarModelImpl();
  }

  @Test
  public void testCreateEmptyCalendar() {
    multi.createCalendar("Week1");
    CalendarModel week1 = multi.getCalendar("Week1");
    assertTrue(week1.getAllEvents().isEmpty());
  }

  @Test
  public void testCreateCalendarWithNullName() {
    try {
      multi.createCalendar(null);
      fail("Expected IllegalArgumentException for creating Calendar with null name");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testCreateCalendarWithEmptyName() {
    try {
      multi.createCalendar("");
      fail("Expected IllegalArgumentException for creating Calendar with empty name");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testCreateCalendarWithTimeZone() {
    ZoneId europe = ZoneId.of("Europe/Paris");
    multi.createCalendar("Trip to Europe", europe);

    assertEquals(europe, multi.getTimeZone("Trip to Europe"));
  }

  @Test
  public void testCreateDuplicateCalendar() {
    multi.createCalendar("fall25");

    try {
      multi.createCalendar("FALL25");
      fail("Expected IllegalArgumentException for creating a calendar with duplicate name.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testDeleteCalendar() {
    multi.createCalendar("Should be Deleted");
    multi.deleteCalendar("Should be Deleted");

    assertFalse(multi.existCalendar("Should be Deleted"));

    try {
      multi.getCalendar("Should be Deleted");
      fail("Expected IllegalArgumentException for retrieving a deleted calendar.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testDeleteCalendarNonExisting() {
    try {
      multi.deleteCalendar("Should be Deleted");
      fail("Expected IllegalArgumentException for deleting a non-existing calendar.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testRenameCalendar() {
    multi.createCalendar("Shower");
    multi.renameCalendar("Shower", "Swimming");

    assertNotNull(multi.getCalendar("Swimming"));
    assertTrue(multi.existCalendar("Swimming"));

    try {
      multi.getCalendar("Shower");
      fail("Expected IllegalArgumentException for getting a non-existing calendar.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testRenameCalendarNonExisting() {
    try {
      multi.createCalendar("Shower");
      multi.renameCalendar("non-existing-calendar", "Shower");
      fail("Expected IllegalArgumentException for renaming a non-existing calendar.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testRenameCalendarWithSameName() {
    multi.createCalendar("Math Class");
    multi.renameCalendar("Math Class", "MATH class");

    assertTrue(multi.existCalendar("Math Class"));
    assertTrue(multi.existCalendar("MATH Class"));
  }

  @Test
  public void testRenameCalendarToExistingName() {
    multi.createCalendar("oldCalendar");
    multi.createCalendar("newCalendar");

    try {
      multi.renameCalendar("OLDCALENDAR", "NEWCALENDAR");
      fail("Expected IllegalArgumentException for renaming calendar to an existing name.");

    } catch (IllegalArgumentException e) {
      // ok
    }

    assertNotNull(multi.getCalendar("oldCalendar"));
    assertNotNull(multi.getCalendar("newCalendar"));
  }

  @Test
  public void testGetDefaultTimeZone() {
    multi.createCalendar("Dinner");
    assertEquals(ZoneId.of("America/New_York"), multi.getTimeZone("Dinner"));
  }

  @Test
  public void testGetTimeZoneWithNonExistingCalendar() {
    try {
      multi.getTimeZone("non-existing-calendar");
      fail("Expected IllegalArgumentException for getting the timezone of a calendar"
          + "that does not exist.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testSetTimeZoneWithNonExistingCalendar() {
    try {
      multi.setTimeZone("non-existing-calendar", ZoneId.of("America/New_York"));
      fail("Expected IllegalArgumentException for setting the timezone of a calendar"
          + "that does not exist.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testSetTimeZoneWithEmptyCalendar() {
    multi.createCalendar("No events");

    multi.setTimeZone("No events", ZoneId.of("Australia/Sydney"));
    ZoneId sydney = ZoneId.of("Australia/Sydney");

    assertEquals(sydney, multi.getTimeZone("No events"));
  }

  @Test
  public void testSetTimeZone() {
    multi.createCalendar("exam");
    CalendarModel exam = multi.getCalendar("exam");

    LocalDateTime startTime =
        LocalDateTime.of(2025, 12, 3, 9, 0);
    LocalDateTime endTime =
        LocalDateTime.of(2025, 12, 3, 10, 0);
    exam.addEvent(EventFactory.createSingleEvent("Review Notes", startTime, endTime, null, null));

    multi.setTimeZone("exam", ZoneId.of("America/Los_Angeles"));

    CalendarModel examUpdated = multi.getCalendar("exam");
    List<CalendarEvent> events = examUpdated.getAllEvents();
    CalendarEvent event = events.get(0);
    assertEquals(LocalDateTime.of(2025, 12, 3, 6, 0),
        event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 12, 3, 7, 0),
        event.getEndDateTime());
  }

  @Test
  public void testSetSameTimeZone() {
    multi.createCalendar("assignment5");
    CalendarModel assignment5 = multi.getCalendar("assignment5");

    multi.setTimeZone("assignment5", ZoneId.of("America/New_York"));
    ZoneId newyork = ZoneId.of("America/New_York");

    assertEquals(newyork, multi.getTimeZone("assignment5"));
  }

  @Test
  public void testGetCalendar() {
    multi.createCalendar("Dance Studio");
    assertNotNull(multi.getCalendar("Dance Studio"));
    assertTrue(multi.existCalendar("Dance Studio"));
  }

  @Test
  public void testGetNonExistingCalendar() {
    try {
      multi.getCalendar("non-existing-calendar");
      fail("Expected IllegalArgumentException for getting a non-existing calendar.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  // ======================= 4. Copying Events =================================

  @Test
  public void testCopyEventNoSourceCalendar() {
    multi2.createCalendar("Pilate Exercises");

    try {
      multi2.copyEvent("Yoga Exercises", "First Day",
          LocalDateTime.of(2025, 12, 12, 7, 15),
          "Pilate Exercises",
          LocalDateTime.of(2026, 1, 12, 7, 15));
      fail("Expected IllegalArgumentException for copying from a non-existing calendar.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testCopyEventNoTargetCalendar() {
    multi2.createCalendar("Tennis Exercises");
    CalendarModel tennis = multi2.getCalendar("Tennis Exercises");

    LocalDateTime startTime =
        LocalDateTime.of(2025, 12, 12, 7, 15);
    LocalDateTime endTime =
        LocalDateTime.of(2025, 12, 12, 8, 15);
    tennis.addEvent(EventFactory.createSingleEvent("First Day", startTime, endTime, null, null));

    try {
      multi2.copyEvent("Tennis Exercises", "Firsy Day",
          LocalDateTime.of(2025, 12, 12, 7, 15),
          "Badminton Exercises",
          LocalDateTime.of(2026, 1, 12, 7, 15));
      fail("Expected IllegalArgumentException for copying to a non-existing calendar.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testCopyEventNoSourceEvent() {
    multi2.createCalendar("Badminton Exercises");
    multi2.createCalendar("GYM");

    try {
      multi2.copyEvent("Badminton Exercises", "First Day",
          LocalDateTime.of(2025, 12, 12, 7, 15),
          "GYM",
          LocalDateTime.of(2026, 1, 12, 7, 15));
      fail("Expected IllegalArgumentException for copying a non-existing event.");

    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testCopyEvent() {
    multi2.createCalendar("week1");
    multi2.createCalendar("week2");
    CalendarModel week1 = multi2.getCalendar("week1");

    LocalDateTime startTime =
        LocalDateTime.of(2025, 12, 3, 9, 0);
    LocalDateTime endTime =
        LocalDateTime.of(2025, 12, 3, 10, 0);
    week1.addEvent(EventFactory.createSingleEvent("Module 1", startTime, endTime, null, null));

    multi2.copyEvent("week1", "Module 1", startTime,
        "week2",
        LocalDateTime.of(2025, 12, 12, 12, 0));

    CalendarModel week2 = multi2.getCalendar("week2");
    CalendarEvent copiedEvent = week2.getAllEvents().get(0);

    assertEquals("Module 1", copiedEvent.getSubject());
    assertEquals(LocalDateTime.of(
        2025, 12, 12, 12, 0), copiedEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(
        2025, 12, 12, 13, 0), copiedEvent.getEndDateTime());
  }

  @Test
  public void testCopyEventSeries() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 11, 3, 9, 0), // Monday
        LocalDateTime.of(2025, 11, 3, 9, 30),
        EventFactory.parseWeekdays("MWF"),
        6
    );

    multi2.createCalendar("Standup Workout");
    CalendarModel standup = multi2.getCalendar("Standup Workout");

    standup.addEvents(series);
    CalendarEvent event = standup.getAllEvents().get(0);
    String oldSeriesId = event.getSeriesId();
    assertEquals(6, standup.getAllEvents().size());
    assertNotNull(oldSeriesId);

    multi2.createCalendar("Sitdown Workout");
    CalendarModel sitdown = multi2.getCalendar("Sitdown Workout");

    multi2.copyEvent("Standup Workout", "Standup",
        LocalDateTime.of(2025, 11, 7, 9, 0), // Wednesday
        "Sitdown Workout",
        LocalDateTime.of(2025, 12, 7, 9, 0));

    List<CalendarEvent> copiedEvents = sitdown.getAllEvents();
    assertEquals(6, copiedEvents.size());

    List<Integer> days = new ArrayList<>(Arrays.asList(3, 5, 7, 10, 12, 14));
    String newSeriesId = null;
    for (int i = 0; i < copiedEvents.size(); i++) {
      CalendarEvent e = copiedEvents.get(i);

      assertEquals("Standup", e.getSubject());
      assertEquals(LocalDateTime.of(
          2025, 12, days.get(i), 9, 0), e.getStartDateTime());
      assertEquals(LocalDateTime.of(
          2025, 12, days.get(i), 9, 30), e.getEndDateTime());

      if (newSeriesId == null) {
        newSeriesId = e.getSeriesId();

        assertNotNull(newSeriesId);
        assertNotEquals(oldSeriesId, newSeriesId);

      } else {
        assertEquals(newSeriesId, e.getSeriesId());
      }
    }

    assertEquals(6, standup.getAllEvents().size());
  }

  @Test
  public void testCopyEventsOnNoSourceCalendar() {
    multi2.createCalendar("Target");

    try {
      multi2.copyEventsOn("NonExistent",
          LocalDate.of(2025, 1, 15),
          "Target",
          LocalDate.of(2025, 2, 15));
      fail("Expected IllegalArgumentException for non-existent source calendar");
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void testCopyEventsOnNoTargetCalendar() {
    multi2.createCalendar("Source");

    try {
      multi2.copyEventsOn("Source",
          LocalDate.of(2025, 1, 15),
          "NonExistent",
          LocalDate.of(2025, 2, 15));
      fail("Expected IllegalArgumentException for non-existent target calendar");
    } catch (IllegalArgumentException e) {
      //ok
    }
  }

  @Test
  public void testCopyEventsOnEmptyDate() {
    multi2.createCalendar("Source");
    multi2.createCalendar("Target");

    multi2.copyEventsOn("Source",
        LocalDate.of(2025, 1, 15),
        "Target",
        LocalDate.of(2025, 2, 15));

    CalendarModel target = multi2.getCalendar("Target");
    assertTrue(target.getAllEvents().isEmpty());
  }

  @Test
  public void testCopyEventsOnSingleEvent() {
    multi2.createCalendar("Source");
    multi2.createCalendar("Target");

    CalendarModel source = multi2.getCalendar("Source");
    source.addEvent(EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0), null, null
    ));

    multi2.copyEventsOn("Source",
        LocalDate.of(2025, 1, 15),
        "Target",
        LocalDate.of(2025, 2, 20));

    CalendarModel target = multi2.getCalendar("Target");
    List<CalendarEvent> events = target.getAllEvents();

    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
    assertEquals(LocalDateTime.of(2025, 2, 20, 10, 0),
        events.get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 2, 20, 11, 0),
        events.get(0).getEndDateTime());
  }

  @Test
  public void testCopyEventsOnMultipleEvents() {
    multi2.createCalendar("Source");
    multi2.createCalendar("Target");

    CalendarModel source = multi2.getCalendar("Source");
    source.addEvent(EventFactory.createSingleEvent(
        "Morning Meeting",
        LocalDateTime.of(2025, 1, 15, 9, 0),
        LocalDateTime.of(2025, 1, 15, 10, 0), null, null
    ));
    source.addEvent(EventFactory.createSingleEvent(
        "Lunch",
        LocalDateTime.of(2025, 1, 15, 12, 0),
        LocalDateTime.of(2025, 1, 15, 13, 0), null, null
    ));
    source.addEvent(EventFactory.createSingleEvent(
        "Afternoon Meeting",
        LocalDateTime.of(2025, 1, 15, 15, 0),
        LocalDateTime.of(2025, 1, 15, 16, 0), null, null
    ));

    multi2.copyEventsOn("Source",
        LocalDate.of(2025, 1, 15),
        "Target",
        LocalDate.of(2025, 3, 10));

    CalendarModel target = multi2.getCalendar("Target");
    List<CalendarEvent> events = target.getAllEvents();

    assertEquals(3, events.size());
    assertEquals("Morning Meeting", events.get(0).getSubject());
    assertEquals(LocalDateTime.of(2025, 3, 10, 9, 0),
        events.get(0).getStartDateTime());
    assertEquals("Lunch", events.get(1).getSubject());
    assertEquals(LocalDateTime.of(2025, 3, 10, 12, 0),
        events.get(1).getStartDateTime());
    assertEquals("Afternoon Meeting", events.get(2).getSubject());
    assertEquals(LocalDateTime.of(2025, 3, 10, 15, 0),
        events.get(2).getStartDateTime());
  }

  @Test
  public void testCopyEventsOnWithTimezoneConversion() {
    multi2.createCalendar("NYC", ZoneId.of("America/New_York"));
    multi2.createCalendar("LA", ZoneId.of("America/Los_Angeles"));

    CalendarModel nyc = multi2.getCalendar("NYC");
    nyc.addEvent(EventFactory.createSingleEvent(
        "Conference Call",
        LocalDateTime.of(2025, 1, 15, 14, 0), // 2pm EST
        LocalDateTime.of(2025, 1, 15, 15, 0), null, null
    ));

    multi2.copyEventsOn("NYC",
        LocalDate.of(2025, 1, 15),
        "LA",
        LocalDate.of(2025, 1, 15));

    CalendarModel la = multi2.getCalendar("LA");
    List<CalendarEvent> events = la.getAllEvents();

    assertEquals(1, events.size());
    assertEquals("Conference Call", events.get(0).getSubject());

    assertEquals(LocalDateTime.of(2025, 1, 15, 11, 0),
        events.get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 1, 15, 12, 0),
        events.get(0).getEndDateTime());
  }

  @Test
  public void testCopyEventsOnSpanningMultipleDays() {
    multi2.createCalendar("Source");
    multi2.createCalendar("Target");

    CalendarModel source = multi2.getCalendar("Source");
    // Event that starts on Jan 15 and ends on Jan 16
    source.addEvent(EventFactory.createSingleEvent(
        "Overnight Event",
        LocalDateTime.of(2025, 1, 15, 22, 0),
        LocalDateTime.of(2025, 1, 16, 2, 0), null, null
    ));

    multi2.copyEventsOn("Source",
        LocalDate.of(2025, 1, 15),
        "Target",
        LocalDate.of(2025, 2, 10));

    CalendarModel target = multi2.getCalendar("Target");
    List<CalendarEvent> events = target.getAllEvents();

    assertEquals(1, events.size());
    assertEquals("Overnight Event", events.get(0).getSubject());
    assertEquals(LocalDateTime.of(2025, 2, 10, 22, 0),
        events.get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 2, 11, 2, 0),
        events.get(0).getEndDateTime());
  }

  @Test
  public void testCopyEventsOnPartialSeries() {
    multi2.createCalendar("Source");
    multi2.createCalendar("Target");

    CalendarModel source = multi2.getCalendar("Source");
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Daily Standup",
        LocalDateTime.of(2025, 1, 13, 9, 0), // Monday
        LocalDateTime.of(2025, 1, 13, 9, 30),
        EventFactory.parseWeekdays("MTWRF"), // Mon-Fri
        10 // 10 occurrences
    );
    source.addEvents(series);

    final String originalSeriesId = series.get(0).getSeriesId();

    multi2.copyEventsOn("Source",
        LocalDate.of(2025, 1, 15),
        "Target",
        LocalDate.of(2025, 2, 15));

    CalendarModel target = multi2.getCalendar("Target");
    List<CalendarEvent> copiedEvents = target.getAllEvents();

    assertEquals(1, copiedEvents.size());
    assertEquals("Daily Standup", copiedEvents.get(0).getSubject());

    String newSeriesId = copiedEvents.get(0).getSeriesId();
    assertNotNull(newSeriesId);

    assertNotEquals(originalSeriesId, newSeriesId);
  }

  @Test
  public void testCopyEventsInBetweenNoSourceCalendar() {
    multi2.createCalendar("Target");

    try {
      multi2.copyEventsInBetween("NonExistent",
          LocalDate.of(2025, 1, 15),
          LocalDate.of(2025, 1, 20),
          "Target",
          LocalDate.of(2025, 2, 15));
      fail("Expected IllegalArgumentException for non-existent source calendar");
    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testCopyEventsInBetweenNoTargetCalendar() {
    multi2.createCalendar("Source");

    try {
      multi2.copyEventsInBetween("Source",
          LocalDate.of(2025, 1, 15),
          LocalDate.of(2025, 1, 20),
          "NonExistent",
          LocalDate.of(2025, 2, 15));
      fail("Expected IllegalArgumentException for non-existent target calendar");
    } catch (IllegalArgumentException e) {
      // ok
    }
  }

  @Test
  public void testCopyEventsInBetweenEmptyRange() {
    multi2.createCalendar("Source");
    multi2.createCalendar("Target");

    // No events in this range
    multi2.copyEventsInBetween("Source",
        LocalDate.of(2025, 1, 15),
        LocalDate.of(2025, 1, 20),
        "Target",
        LocalDate.of(2025, 2, 15));

    CalendarModel target = multi2.getCalendar("Target");
    assertTrue(target.getAllEvents().isEmpty());
  }

  @Test
  public void testCopyEventsInBetweenSingleDay() {
    multi2.createCalendar("Source");
    multi2.createCalendar("Target");

    CalendarModel source = multi2.getCalendar("Source");
    source.addEvent(EventFactory.createSingleEvent(
        "Event1",
        LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0), null, null
    ));

    multi2.copyEventsInBetween("Source",
        LocalDate.of(2025, 1, 15),
        LocalDate.of(2025, 1, 15), // Same day
        "Target",
        LocalDate.of(2025, 2, 20));

    CalendarModel target = multi2.getCalendar("Target");
    assertEquals(1, target.getAllEvents().size());
    assertEquals("Event1", target.getAllEvents().get(0).getSubject());
    assertEquals(LocalDateTime.of(2025, 2, 20, 10, 0),
        target.getAllEvents().get(0).getStartDateTime());
  }

  @Test
  public void testCopyEventsInBetweenMultipleDays() {
    multi2.createCalendar("Source");
    multi2.createCalendar("Target");

    CalendarModel source = multi2.getCalendar("Source");

    // Add events on different days
    source.addEvent(EventFactory.createSingleEvent(
        "Day1 Event",
        LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0), null, null
    ));
    source.addEvent(EventFactory.createSingleEvent(
        "Day2 Event",
        LocalDateTime.of(2025, 1, 16, 14, 0),
        LocalDateTime.of(2025, 1, 16, 15, 0), null, null
    ));
    source.addEvent(EventFactory.createSingleEvent(
        "Day3 Event",
        LocalDateTime.of(2025, 1, 17, 9, 0),
        LocalDateTime.of(2025, 1, 17, 10, 0), null, null
    ));
    // This one is outside the range
    source.addEvent(EventFactory.createSingleEvent(
        "Day4 Event",
        LocalDateTime.of(2025, 1, 18, 9, 0),
        LocalDateTime.of(2025, 1, 18, 10, 0), null, null
    ));

    multi2.copyEventsInBetween("Source",
        LocalDate.of(2025, 1, 15),
        LocalDate.of(2025, 1, 17), // Inclusive
        "Target",
        LocalDate.of(2025, 3, 1));

    CalendarModel target = multi2.getCalendar("Target");
    List<CalendarEvent> events = target.getAllEvents();

    assertEquals(3, events.size());
    assertEquals("Day1 Event", events.get(0).getSubject());
    assertEquals(LocalDateTime.of(2025, 3, 1, 10, 0),
        events.get(0).getStartDateTime());

    assertEquals("Day2 Event", events.get(1).getSubject());
    assertEquals(LocalDateTime.of(2025, 3, 2, 14, 0),
        events.get(1).getStartDateTime());

    assertEquals("Day3 Event", events.get(2).getSubject());
    assertEquals(LocalDateTime.of(2025, 3, 3, 9, 0),
        events.get(2).getStartDateTime());
  }

  @Test
  public void testCopyEventsInBetweenWithTimezones() {
    multi2.createCalendar("Tokyo", ZoneId.of("Asia/Tokyo"));
    multi2.createCalendar("London", ZoneId.of("Europe/London"));

    CalendarModel tokyo = multi2.getCalendar("Tokyo");
    tokyo.addEvent(EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 1, 15, 15, 0), // 3pm JST
        LocalDateTime.of(2025, 1, 15, 16, 0), null, null
    ));

    multi2.copyEventsInBetween("Tokyo",
        LocalDate.of(2025, 1, 15),
        LocalDate.of(2025, 1, 15),
        "London",
        LocalDate.of(2025, 2, 15));

    CalendarModel london = multi2.getCalendar("London");
    List<CalendarEvent> events = london.getAllEvents();

    assertEquals(1, events.size());
    // 3pm JST = 6am GMT
    assertEquals(LocalDateTime.of(2025, 2, 15, 6, 0),
        events.get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 2, 15, 7, 0),
        events.get(0).getEndDateTime());
  }

  @Test
  public void testCopyEventsInBetweenSameCalendar() {
    multi2.createCalendar("MyCalendar");

    CalendarModel cal = multi2.getCalendar("MyCalendar");
    cal.addEvent(EventFactory.createSingleEvent(
        "Original Event",
        LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0), null, null
    ));

    // Copy within same calendar
    multi2.copyEventsInBetween("MyCalendar",
        LocalDate.of(2025, 1, 15),
        LocalDate.of(2025, 1, 15),
        "MyCalendar",
        LocalDate.of(2025, 2, 15));

    CalendarModel calAfter = multi2.getCalendar("MyCalendar");
    List<CalendarEvent> events = calAfter.getAllEvents();

    // Should have both original and copied event
    assertEquals(2, events.size());
    assertEquals("Original Event", events.get(0).getSubject());
    assertEquals(LocalDateTime.of(2025, 1, 15, 10, 0),
        events.get(0).getStartDateTime());
    assertEquals("Original Event", events.get(1).getSubject());
    assertEquals(LocalDateTime.of(2025, 2, 15, 10, 0),
        events.get(1).getStartDateTime());
  }

  @Test
  public void testCopyEventsInBetweenOverlappingRange() {
    multi2.createCalendar("Source");
    multi2.createCalendar("Target");

    CalendarModel source = multi2.getCalendar("Source");

    // Event that overlaps with range boundary
    source.addEvent(EventFactory.createSingleEvent(
        "Boundary Event",
        LocalDateTime.of(2025, 1, 14, 23, 0), // Before range start
        LocalDateTime.of(2025, 1, 15, 1, 0), null, null
    ));

    source.addEvent(EventFactory.createSingleEvent(
        "Fully Inside",
        LocalDateTime.of(2025, 1, 15, 10, 0),
        LocalDateTime.of(2025, 1, 15, 11, 0), null, null
    ));

    multi2.copyEventsInBetween("Source",
        LocalDate.of(2025, 1, 15),
        LocalDate.of(2025, 1, 16),
        "Target",
        LocalDate.of(2025, 2, 1));

    CalendarModel target = multi2.getCalendar("Target");
    List<CalendarEvent> events = target.getAllEvents();

    // Both events should be copied (boundary event overlaps with range)
    assertEquals(2, events.size());
  }


  @Test
  public void testSetTimeZoneWithMultipleEvents() {
    multi.createCalendar("BusyCalendar");
    CalendarModel busy = multi.getCalendar("BusyCalendar");

    // Add multiple events
    busy.addEvent(EventFactory.createSingleEvent(
        "Morning",
        LocalDateTime.of(2025, 1, 15, 9, 0),
        LocalDateTime.of(2025, 1, 15, 10, 0), null, null
    ));
    busy.addEvent(EventFactory.createSingleEvent(
        "Afternoon",
        LocalDateTime.of(2025, 1, 15, 14, 0),
        LocalDateTime.of(2025, 1, 15, 15, 0), null, null
    ));
    busy.addEvent(EventFactory.createSingleEvent(
        "Evening",
        LocalDateTime.of(2025, 1, 15, 18, 0),
        LocalDateTime.of(2025, 1, 15, 19, 0), null, null
    ));

    // Change from EST to PST
    multi.setTimeZone("BusyCalendar", ZoneId.of("America/Los_Angeles"));

    CalendarModel updated = multi.getCalendar("BusyCalendar");
    List<CalendarEvent> events = updated.getAllEvents();

    assertEquals(3, events.size());

    // 9am EST = 6am PST
    assertEquals(LocalDateTime.of(2025, 1, 15, 6, 0),
        events.get(0).getStartDateTime());
    // 2pm EST = 11am PST
    assertEquals(LocalDateTime.of(2025, 1, 15, 11, 0),
        events.get(1).getStartDateTime());
    // 6pm EST = 3pm PST
    assertEquals(LocalDateTime.of(2025, 1, 15, 15, 0),
        events.get(2).getStartDateTime());
  }

  @Test
  public void testSetTimeZonePreservesSeriesIds() {
    multi.createCalendar("SeriesCalendar");
    CalendarModel cal = multi.getCalendar("SeriesCalendar");

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Recurring",
        LocalDateTime.of(2025, 1, 13, 10, 0),
        LocalDateTime.of(2025, 1, 13, 11, 0),
        EventFactory.parseWeekdays("MW"),
        4
    );
    cal.addEvents(series);

    String originalSeriesId = series.get(0).getSeriesId();

    multi.setTimeZone("SeriesCalendar", ZoneId.of("Europe/Paris"));

    CalendarModel updated = multi.getCalendar("SeriesCalendar");
    List<CalendarEvent> updatedEvents = updated.getAllEvents();

    // All events should still have the same series ID
    for (CalendarEvent event : updatedEvents) {
      assertEquals(originalSeriesId, event.getSeriesId());
    }
  }
}
