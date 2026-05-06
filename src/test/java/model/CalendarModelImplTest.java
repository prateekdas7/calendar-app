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
 * Comprehensive test suite for CalendarModelImpl.
 */
public class CalendarModelImplTest {

  private CalendarModel calendar;

  /**
   * The calendar model instance used for testing.
   */
  @Before
  public void setUp() {
    calendar = new CalendarModelImpl();
  }

  @Test
  public void testAddSingleEvent() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    List<CalendarEvent> events = calendar.getAllEvents();

    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddNullEvent() {
    calendar.addEvent(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddDuplicateEvent() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2);
  }

  @Test
  public void testAddMultipleEvents() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MWF"),
        6
    );

    calendar.addEvents(series);
    assertEquals(6, calendar.getAllEvents().size());
  }

  @Test
  public void testFindEventsBySubjectAndStart() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    List<CalendarEvent> found = calendar.findEvents(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0)
    );

    assertEquals(1, found.size());
    assertEquals("Meeting", found.get(0).getSubject());
  }

  @Test
  public void testFindEventsNotFound() {
    List<CalendarEvent> found = calendar.findEvents(
        "NonExistent",
        LocalDateTime.of(2025, 5, 1, 10, 0)
    );

    assertTrue(found.isEmpty());
  }

  @Test
  public void testFindMultipleMatchingEvents() {
    CalendarEvent event1 = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, "series1"
    );

    CalendarEvent event2 = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, "series2"
    );

    calendar.addEvent(event1);
    try {
      calendar.addEvent(event2);
      fail("Should not allow duplicate events");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }


  @Test
  public void testUpdateEventSubject() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    CalendarEvent updated = event.withSubject("Important Meeting");
    calendar.updateEvent(event, updated);

    List<CalendarEvent> events = calendar.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("Important Meeting", events.get(0).getSubject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateNonExistentEvent() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    CalendarEvent updated = event.withSubject("New");
    calendar.updateEvent(event, updated);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateCreatingDuplicate() {
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

    CalendarEvent updated = new CalendarEvent(
        "Meeting1",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, null
    );

    calendar.updateEvent(event2, updated);
  }

  @Test
  public void testUpdateAllInSeries() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MW"),
        4
    );

    calendar.addEvents(series);

    CalendarEvent firstEvent = series.get(0);
    calendar.updateAllInSeries(firstEvent, "subject", "New Standup");

    List<CalendarEvent> allEvents = calendar.getAllEvents();
    for (CalendarEvent event : allEvents) {
      assertEquals("New Standup", event.getSubject());
    }
  }

  @Test
  public void testUpdateEventsFromThis() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MW"),
        4
    );

    calendar.addEvents(series);

    CalendarEvent secondEvent = series.get(1);
    calendar.updateEventsFromThis(secondEvent, "subject", "Modified");

    List<CalendarEvent> allEvents = calendar.getAllEvents();
    assertEquals("Standup", allEvents.get(0).getSubject());
    assertEquals("Modified", allEvents.get(1).getSubject());
    assertEquals("Modified", allEvents.get(2).getSubject());
    assertEquals("Modified", allEvents.get(3).getSubject());
  }

  @Test
  public void testUpdateSingleNonSeriesEvent() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    calendar.updateAllInSeries(event, "location", "Room 101");

    List<CalendarEvent> events = calendar.getAllEvents();
    assertEquals("Room 101", events.get(0).getLocation());
  }

  @Test
  public void testGetEventsOnDate() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Morning Meeting",
        LocalDateTime.of(2025, 5, 1, 9, 0),
        LocalDateTime.of(2025, 5, 1, 10, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Lunch",
        LocalDateTime.of(2025, 5, 1, 12, 0),
        LocalDateTime.of(2025, 5, 1, 13, 0), null, null
    );
    CalendarEvent event3 = EventFactory.createSingleEvent(
        "Other Day Meeting",
        LocalDateTime.of(2025, 5, 2, 10, 0),
        LocalDateTime.of(2025, 5, 2, 11, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    List<CalendarEvent> eventsOnMay1 = calendar.getEventsOnDate(LocalDate.of(2025, 5, 1));
    assertEquals(2, eventsOnMay1.size());
  }

  @Test
  public void testGetEventsOnDateEmpty() {
    List<CalendarEvent> events = calendar.getEventsOnDate(LocalDate.of(2025, 5, 1));
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsOnDateSorted() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Lunch",
        LocalDateTime.of(2025, 5, 1, 12, 0),
        LocalDateTime.of(2025, 5, 1, 13, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Morning Meeting",
        LocalDateTime.of(2025, 5, 1, 9, 0),
        LocalDateTime.of(2025, 5, 1, 10, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    List<CalendarEvent> events = calendar.getEventsOnDate(LocalDate.of(2025, 5, 1));
    assertEquals("Morning Meeting", events.get(0).getSubject());
    assertEquals("Lunch", events.get(1).getSubject());
  }

  @Test
  public void testGetEventsInRange() {
    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Event1",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Event2",
        LocalDateTime.of(2025, 5, 3, 10, 0),
        LocalDateTime.of(2025, 5, 3, 11, 0), null, null
    );
    CalendarEvent event3 = EventFactory.createSingleEvent(
        "Event3",
        LocalDateTime.of(2025, 5, 10, 10, 0),
        LocalDateTime.of(2025, 5, 10, 11, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2);
    calendar.addEvent(event3);

    List<CalendarEvent> inRange = calendar.getEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 5, 23, 59)
    );

    assertEquals(2, inRange.size());
  }

  @Test
  public void testGetEventsInRangeOverlap() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Conference",
        LocalDateTime.of(2025, 5, 1, 9, 0),
        LocalDateTime.of(2025, 5, 3, 17, 0), null, null
    );

    calendar.addEvent(event);

    List<CalendarEvent> events = calendar.getEventsOnDate(LocalDate.of(2025, 5, 2));
    assertEquals(1, events.size());
  }

  @Test
  public void testIsBusy() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);

    assertTrue(calendar.isBusy(LocalDateTime.of(2025, 5, 1, 10, 30)));
    assertFalse(calendar.isBusy(LocalDateTime.of(2025, 5, 1, 12, 0)));
  }

  @Test
  public void testIsBusyAtStartTime() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    assertTrue(calendar.isBusy(LocalDateTime.of(2025, 5, 1, 10, 0)));
  }

  @Test
  public void testIsBusyAtEndTime() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    assertTrue(calendar.isBusy(LocalDateTime.of(2025, 5, 1, 11, 0)));
  }

  @Test
  public void testIsAvailable() {
    assertFalse(calendar.isBusy(LocalDateTime.of(2025, 5, 1, 10, 0)));
  }

  @Test
  public void testGetEventsInSeries() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MWF"),
        6
    );

    calendar.addEvents(series);

    String seriesId = series.get(0).getSeriesId();
    List<CalendarEvent> retrieved = calendar.getEventsInSeries(seriesId);

    assertEquals(6, retrieved.size());
  }

  @Test
  public void testGetEventsInSeriesNullId() {
    List<CalendarEvent> events = calendar.getEventsInSeries(null);
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsInSeriesNonExistent() {
    List<CalendarEvent> events = calendar.getEventsInSeries("nonexistent");
    assertTrue(events.isEmpty());
  }

  @Test
  public void testComplexScenario() {
    CalendarEvent single = EventFactory.createSingleEvent(
        "Single Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );
    calendar.addEvent(single);

    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MWF"),
        3
    );
    calendar.addEvents(series);

    assertEquals(4, calendar.getAllEvents().size());

    List<CalendarEvent> may5 = calendar.getEventsOnDate(LocalDate.of(2025, 5, 5));
    assertEquals(1, may5.size());

    assertTrue(calendar.isBusy(LocalDateTime.of(2025, 5, 1, 10, 30)));
    assertTrue(calendar.isBusy(LocalDateTime.of(2025, 5, 5, 9, 15)));
  }

  @Test
  public void testUpdateEventDescription() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    calendar.updateAllInSeries(event, "description", "Important discussion");

    List<CalendarEvent> events = calendar.getAllEvents();
    assertEquals("Important discussion", events.get(0).getDescription());
  }

  @Test
  public void testUpdateEventLocation() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    calendar.updateAllInSeries(event, "location", "Conference Room A");

    List<CalendarEvent> events = calendar.getAllEvents();
    assertEquals("Conference Room A", events.get(0).getLocation());
  }

  @Test
  public void testUpdateEventStatus() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    calendar.updateAllInSeries(event, "status", "private");

    List<CalendarEvent> events = calendar.getAllEvents();
    assertEquals("private", events.get(0).getStatus());
  }

  @Test
  public void testUpdateEventStartTime() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    calendar.updateAllInSeries(event, "start", "2025-05-01T09:00:00");

    List<CalendarEvent> events = calendar.getAllEvents();
    assertEquals(LocalDateTime.of(2025, 5, 1, 9, 0), events.get(0).getStartDateTime());
  }

  @Test
  public void testUpdateEventEndTime() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    calendar.updateAllInSeries(event, "end", "2025-05-01T12:00:00");

    List<CalendarEvent> events = calendar.getAllEvents();
    assertEquals(LocalDateTime.of(2025, 5, 1, 12, 0), events.get(0).getEndDateTime());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateEventInvalidProperty() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    calendar.updateAllInSeries(event, "invalidProperty", "value");
  }

  @Test
  public void testUpdatePropertyCaseInsensitive() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);
    calendar.updateAllInSeries(event, "SUBJECT", "New Title");

    List<CalendarEvent> events = calendar.getAllEvents();
    assertEquals("New Title", events.get(0).getSubject());
  }

  @Test
  public void testUpdateSeriesDescription() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MW"),
        3
    );

    calendar.addEvents(series);
    calendar.updateAllInSeries(series.get(0), "description", "Daily sync meeting");

    List<CalendarEvent> allEvents = calendar.getAllEvents();
    for (CalendarEvent event : allEvents) {
      assertEquals("Daily sync meeting", event.getDescription());
    }
  }

  @Test
  public void testUpdateSeriesStatus() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MW"),
        3
    );

    calendar.addEvents(series);
    calendar.updateAllInSeries(series.get(0), "status", "tentative");

    List<CalendarEvent> allEvents = calendar.getAllEvents();
    for (CalendarEvent event : allEvents) {
      assertEquals("tentative", event.getStatus());
    }
  }

  @Test
  public void testUpdateSeriesLocation() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MW"),
        3
    );

    calendar.addEvents(series);
    calendar.updateAllInSeries(series.get(0), "location", "Virtual - Zoom");

    List<CalendarEvent> allEvents = calendar.getAllEvents();
    for (CalendarEvent event : allEvents) {
      assertEquals("Virtual - Zoom", event.getLocation());
    }
  }

  @Test
  public void testFindEventsMatchingSubjectOnly() {

    CalendarEvent event1 = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );
    CalendarEvent event2 = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 14, 0),
        LocalDateTime.of(2025, 5, 1, 15, 0), null, null
    );

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    List<CalendarEvent> found = calendar.findEvents(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0)
    );

    assertEquals(1, found.size());
    assertEquals(LocalDateTime.of(2025, 5, 1, 10, 0), found.get(0).getStartDateTime());
  }

  @Test
  public void testFindEventsNoMatchSubject() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);

    List<CalendarEvent> found = calendar.findEvents(
        "Different Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0)
    );

    assertTrue(found.isEmpty());
  }

  @Test
  public void testFindEventsNoMatchStartTime() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);

    List<CalendarEvent> found = calendar.findEvents(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 14, 0)
    );

    assertTrue(found.isEmpty());
  }

  @Test
  public void testFindEventsExactMatch() {
    CalendarEvent event = EventFactory.createSingleEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0), null, null
    );

    calendar.addEvent(event);

    List<CalendarEvent> found = calendar.findEvents(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0)
    );

    assertEquals(1, found.size());
    assertEquals("Meeting", found.get(0).getSubject());
    assertEquals(LocalDateTime.of(2025, 5, 1, 10, 0), found.get(0).getStartDateTime());
  }

  @Test
  public void testFindEventsInSeries() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MW"),
        3
    );

    calendar.addEvents(series);

    List<CalendarEvent> found = calendar.findEvents(
        "Standup",
        LocalDateTime.of(2025, 5, 7, 9, 0)
    );

    assertEquals(1, found.size());
    assertEquals(LocalDate.of(2025, 5, 7), found.get(0).getStartDateTime().toLocalDate());
  }
}





