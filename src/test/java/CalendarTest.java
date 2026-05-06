import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.CalendarEvent;
import model.CalendarModel;
import model.CalendarModelImpl;
import model.EventFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for CalendarModel implementation.
 */
public class CalendarTest {
  private CalendarModel calendar;

  /**
   * Sets up a new calendar instance before each test.
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
  public void testFindEvents() {
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
  public void testUpdateEvent() {
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
  public void testCreateEventSeries() {
    List<CalendarEvent> series = EventFactory.createEventSeries(
        "Daily Standup",
        LocalDateTime.of(2025, 5, 5, 9, 0),
        LocalDateTime.of(2025, 5, 5, 9, 30),
        EventFactory.parseWeekdays("MWF"),
        6
    );

    assertEquals(6, series.size());

    String seriesId = series.get(0).getSeriesId();
    for (CalendarEvent event : series) {
      assertNotNull(event.getSeriesId());
      assertEquals(seriesId, event.getSeriesId());
    }
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

  //  @Test
  //  public void testAllDayEvent() {
  //    CalendarEvent event = EventFactory.createAllDayEvent(
  //        "Conference",
  //        LocalDate.of(2025, 5, 1)
  //      );
  //
  //    assertEquals(8, event.getStartDateTime().getHour());
  //    assertEquals(17, event.getEndDateTime().getHour());
  //  }

  @Test
  public void testParseWeekdays() {
    var days = EventFactory.parseWeekdays("MTWRF");
    assertEquals(5, days.size());
    assertTrue(days.contains(java.time.DayOfWeek.MONDAY));
    assertTrue(days.contains(java.time.DayOfWeek.FRIDAY));
  }
}
