package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Additional test cases to improve code coverage for CalendarModelImpl.
 */
public class CalendarModelImplCoverageTest {

  private CalendarModel calendar;

  /**
   * Sets up the test fixture before each test method.
   * Initializes a fresh CalendarModelImpl instance to ensure test isolation.
   */
  @Before
  public void setUp() {
    calendar = new CalendarModelImpl();
  }

  // ============ All-Day Event Tests ============

  @Test
  public void testAddAllDayEvent() {
    CalendarEvent allDay = EventFactory.createAllDayEvent("Holiday", LocalDate.of(2025, 5, 1));
    calendar.addEvent(allDay);

    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testMultipleAllDayEventsOnSameDay() {
    CalendarEvent allDay1 = EventFactory.createAllDayEvent("Holiday", LocalDate.of(2025, 5, 1));
    CalendarEvent allDay2 = EventFactory.createAllDayEvent("Birthday", LocalDate.of(2025, 5, 1));

    calendar.addEvent(allDay1);
    calendar.addEvent(allDay2); // Should NOT throw - all-day events can coexist

    assertEquals(2, calendar.getAllEvents().size());
  }

  @Test
  public void testAllDayEventWithTimedEvent() {
    CalendarEvent allDay = EventFactory.createAllDayEvent("Holiday", LocalDate.of(2025, 5, 1));
    CalendarEvent timed = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(allDay);
    calendar.addEvent(timed); // Should NOT throw - all-day and timed can coexist

    assertEquals(2, calendar.getAllEvents().size());
  }

  @Test
  public void testTimedEventWithAllDayEvent() {
    CalendarEvent timed = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );
    CalendarEvent allDay = EventFactory.createAllDayEvent("Holiday", LocalDate.of(2025, 5, 1));

    calendar.addEvent(timed);
    calendar.addEvent(allDay); // Should NOT throw

    assertEquals(2, calendar.getAllEvents().size());
  }

  @Test
  public void testUpdateAllDayEventWithTimedEvent() {
    CalendarEvent allDay = EventFactory.createAllDayEvent("Holiday", LocalDate.of(2025, 5, 1));
    calendar.addEvent(allDay);

    CalendarEvent timed = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.updateEvent(allDay, timed); // Should succeed
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testUpdateTimedEventToAllDay() {
    CalendarEvent timed = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );
    calendar.addEvent(timed);

    CalendarEvent allDay = EventFactory.createAllDayEvent("Holiday", LocalDate.of(2025, 5, 1));
    calendar.updateEvent(timed, allDay); // Should succeed

    assertEquals(1, calendar.getAllEvents().size());
  }

  // ============ Event Overlap Tests ============

  @Test(expected = IllegalArgumentException.class)
  public void testOverlappingTimedEvents() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Meeting1",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Meeting2",
        LocalDateTime.of(2025, 5, 1, 10, 30),
        LocalDateTime.of(2025, 5, 1, 11, 30), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2); // Should throw - overlaps
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEventStartsDuringExisting() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Meeting1",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 12, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Meeting2",
        LocalDateTime.of(2025, 5, 1, 11, 0),
        LocalDateTime.of(2025, 5, 1, 13, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2); // Should throw
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEventEndsAfterExistingStarts() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Meeting1",
        LocalDateTime.of(2025, 5, 1, 12, 0),
        LocalDateTime.of(2025, 5, 1, 14, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Meeting2",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 13, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2); // Should throw
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEventCompletelyContains() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Meeting1",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 12, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Meeting2",
        LocalDateTime.of(2025, 5, 1, 9, 0),
        LocalDateTime.of(2025, 5, 1, 13, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2); // Should throw - event2 contains event1
  }

  @Test
  public void testBackToBackEvents() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Meeting1",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Meeting2",
        LocalDateTime.of(2025, 5, 1, 11, 0),
        LocalDateTime.of(2025, 5, 1, 12, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2); // Should NOT throw - exact boundary

    assertEquals(2, calendar.getAllEvents().size());
  }

  // ============ Update Event Error Recovery Tests ============

  @Test
  public void testUpdateEventConflictRestoresOriginal() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Meeting1",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Meeting2",
        LocalDateTime.of(2025, 5, 1, 14, 0),
        LocalDateTime.of(2025, 5, 1, 15, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    // Try to update event2 to overlap with event1
    CalendarEvent conflicting = EventFactory.createSingleEvent(
        "Updated",
        LocalDateTime.of(2025, 5, 1, 10, 30),
        LocalDateTime.of(2025, 5, 1, 11, 30), null, null
    );

    try {
      calendar.updateEvent(event2, conflicting);
      fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Original event2 should still be in calendar
      List<CalendarEvent> events = calendar.getAllEvents();
      assertEquals(2, events.size());
      assertTrue(events.contains(event2));
    }
  }

  // ============ Get Events On Date - Multi-day Events ============

  @Test
  public void testGetEventsOnDateSpanningMultipleDays() {
    CalendarEvent multiDay = EventFactory.createSingleEvent(
        "Conference",
        LocalDateTime.of(2025, 5, 1, 9, 0),
        LocalDateTime.of(2025, 5, 3, 17, 0), null, null
    );

    calendar.addEvent(multiDay);

    // Should appear on all three days
    assertEquals(1, calendar.getEventsOnDate(LocalDate.of(2025, 5, 1)).size());
    assertEquals(1, calendar.getEventsOnDate(LocalDate.of(2025, 5, 2)).size());
    assertEquals(1, calendar.getEventsOnDate(LocalDate.of(2025, 5, 3)).size());
    assertEquals(0, calendar.getEventsOnDate(LocalDate.of(2025, 5, 4)).size());
  }

  @Test
  public void testGetEventsOnDateAllDayEvent() {
    CalendarEvent allDay = EventFactory.createAllDayEvent("Holiday", LocalDate.of(2025, 5, 1));
    calendar.addEvent(allDay);

    List<CalendarEvent> events = calendar.getEventsOnDate(LocalDate.of(2025, 5, 1));
    assertEquals(1, events.size());
  }

  // ============ Get Events In Range Tests ============

  @Test
  public void testGetEventsInRangeEmpty() {
    List<CalendarEvent> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 5, 23, 59)
    );
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsInRangeSorted() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Event3",
        LocalDateTime.of(2025, 5, 3, 10, 0),
        LocalDateTime.of(2025, 5, 3, 11, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Event1",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    List<CalendarEvent> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 5, 23, 59)
    );

    assertEquals(2, events.size());
    assertEquals("Event1", events.get(0).getSubject());
    assertEquals("Event3", events.get(1).getSubject());
  }

  @Test
  public void testGetEventsInRangePartialOverlap() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 12, 0), null, null
    );

    calendar.addEvent(event);

    // Range overlaps with part of event
    List<CalendarEvent> events = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 11, 0),
        LocalDateTime.of(2025, 5, 1, 13, 0)
    );

    assertEquals(1, events.size());
  }

  // ============ Is Busy Edge Cases ============

  @Test
  public void testIsBusyBeforeEvent() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    assertFalse(calendar.isBusy(LocalDateTime.of(2025, 5, 1, 9, 59)));
  }

  @Test
  public void testIsBusyAfterEvent() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    assertFalse(calendar.isBusy(LocalDateTime.of(2025, 5, 1, 11, 1)));
  }

  @Test
  public void testIsBusyWithAllDayEvent() {
    CalendarEvent allDay = EventFactory.createAllDayEvent("Holiday", LocalDate.of(2025, 5, 1));
    calendar.addEvent(allDay);

    assertTrue(calendar.isBusy(LocalDateTime.of(2025, 5, 1, 12, 0)));
  }

  // ============ Series Update Edge Cases ============

  @Test
  public void testUpdateEventsFromThisOnSingleEvent() {
    CalendarEvent single = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(single);
    calendar.updateEventsFromThis(single, "subject", "Updated");

    assertEquals("Updated", calendar.getAllEvents().get(0).getSubject());
  }

  @Test
  public void testUpdateEventsFromThisLastEvent() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MW"),
        3
    );

    calendar.addEvents(series);

    CalendarEvent lastEvent = series.get(2);
    calendar.updateEventsFromThis(lastEvent, "subject", "Final Standup");

    List<CalendarEvent> allEvents = calendar.getAllEvents();
    assertEquals("Standup", allEvents.get(0).getSubject());
    assertEquals("Standup", allEvents.get(1).getSubject());
    assertEquals("Final Standup", allEvents.get(2).getSubject());
  }

  @Test
  public void testUpdateEventsFromThisFirstEvent() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MW"),
        3
    );

    calendar.addEvents(series);

    CalendarEvent firstEvent = series.get(0);
    calendar.updateEventsFromThis(firstEvent, "subject", "New Standup");

    List<CalendarEvent> allEvents = calendar.getAllEvents();
    assertEquals("New Standup", allEvents.get(0).getSubject());
    assertEquals("New Standup", allEvents.get(1).getSubject());
    assertEquals("New Standup", allEvents.get(2).getSubject());
  }

  // ============ Get Events In Series Edge Cases ============

  @Test
  public void testGetEventsInSeriesSorted() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MWF"),
        5
    );

    calendar.addEvents(series);

    String seriesId = series.get(0).getSeriesId();
    List<CalendarEvent> retrieved = calendar.getEventsInSeries(seriesId);

    // Verify they're sorted by start time
    for (int i = 0; i < retrieved.size() - 1; i++) {
      assertTrue(retrieved.get(i).getStartDateTime()
          .isBefore(retrieved.get(i + 1).getStartDateTime()));
    }
  }

  // ============ Additional Update Property Tests ============

  @Test
  public void testUpdateAllInSeriesWithAllProperties() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MW"),
        2
    );

    calendar.addEvents(series);

    // Update multiple properties
    calendar.updateAllInSeries(series.get(0), "subject", "New Standup");
    calendar.updateAllInSeries(series.get(0), "location", "Room 100");
    calendar.updateAllInSeries(series.get(0), "description", "Daily meeting");
    calendar.updateAllInSeries(series.get(0), "status", "confirmed");

    List<CalendarEvent> allEvents = calendar.getAllEvents();
    for (CalendarEvent event : allEvents) {
      assertEquals("New Standup", event.getSubject());
      assertEquals("Room 100", event.getLocation());
      assertEquals("Daily meeting", event.getDescription());
      assertEquals("confirmed", event.getStatus());
    }
  }

  // ============ Event Boundary Tests ============

  @Test
  public void testGetEventsOnDateStartsAtMidnight() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Midnight Meeting",
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 1, 1, 0), null, null
    );

    calendar.addEvent(event);
    assertEquals(1, calendar.getEventsOnDate(LocalDate.of(2025, 5, 1)).size());
  }

  @Test
  public void testGetEventsOnDateEndsBeforeMidnight() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Late Meeting",
        LocalDateTime.of(2025, 5, 1, 23, 0),
        LocalDateTime.of(2025, 5, 1, 23, 59), null, null
    );

    calendar.addEvent(event);
    assertEquals(1, calendar.getEventsOnDate(LocalDate.of(2025, 5, 1)).size());
    assertEquals(0, calendar.getEventsOnDate(LocalDate.of(2025, 5, 2)).size());
  }
}