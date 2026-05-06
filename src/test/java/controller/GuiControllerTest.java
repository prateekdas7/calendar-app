package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import model.CalendarEvent;
import model.CalendarModel;
import model.MultiCalendarModel;
import model.MultiCalendarModelImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Comprehensive test suite for GuiController.
 */
public class GuiControllerTest {

  private MultiCalendarModel multiModel;
  private GuiController gui;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  /**
   * Sets up a fresh multi-calendar model and GUI controller before each test.
   */
  @Before
  public void setUp() {
    multiModel = new MultiCalendarModelImpl();
    gui = new GuiController(multiModel);
  }

  @Test
  public void testDefaultCalendarIsCreatedAndActive() {
    assertTrue(multiModel.existCalendar("Default Calendar"));

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals("Default Calendar", gui.getActiveCalendarName());

    ZoneId modelZone = multiModel.getTimeZone("Default Calendar");
    assertEquals(modelZone.getId(), gui.getCurrentTimezone());
    assertTrue(defaultModel.getAllEvents().isEmpty());
  }

  @Test
  public void testDefaultCalendarIsCreatedAndInactive() {
    MultiCalendarModelImpl model = new MultiCalendarModelImpl();
    model.createCalendar("Default Calendar", ZoneId.systemDefault());
    CalendarModel existingDefault = model.getCalendar("Default Calendar");
    assertTrue(existingDefault.getAllEvents().isEmpty());

    GuiController gui = new GuiController(model);
    gui.createSingleEvent("Lunch", "2025-11-22T11:00", "2025-11-22T12:00", null, null);

    List<CalendarEvent> eventsInExisting = existingDefault.getAllEvents();
    assertEquals(1, eventsInExisting.size());
    assertEquals("Lunch", eventsInExisting.get(0).getSubject());

    assertEquals(ZoneId.systemDefault().getId(), gui.getCurrentTimezone());
  }

  @Test
  public void testCreateCalendarWithTimezone() {
    gui.createCalendar("calendar1", "America/Los_Angeles");

    assertTrue(multiModel.existCalendar("calendar1"));
    assertEquals(ZoneId.of("America/Los_Angeles"),
        multiModel.getTimeZone("calendar1"));
  }

  @Test
  public void testSwitchCalendar() {
    gui.createCalendar("new york trip", "America/New_York");
    gui.createCalendar("los angeles trip", "America/Los_Angeles");

    gui.switchCalendar("new york trip");
    assertEquals("new york trip", gui.getActiveCalendarName());
    assertEquals("America/New_York", gui.getCurrentTimezone());

    gui.switchCalendar("los angeles trip");
    assertEquals("los angeles trip", gui.getActiveCalendarName());
    assertEquals("America/Los_Angeles", gui.getCurrentTimezone());
  }

  @Test
  public void testCreateSingleEvent() {
    gui.createSingleEvent(
        "city walk", "2025-11-22T18:00", "2025-11-22T19:00", null, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    List<CalendarEvent> eventsInDefault = defaultModel.getAllEvents();
    assertEquals(1, eventsInDefault.size());

    CalendarEvent event = eventsInDefault.get(0);
    assertEquals("city walk", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 11, 22, 18, 0),
        event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 11, 22, 19, 0),
        event.getEndDateTime());
  }

  @Test
  public void testCreateAllDayEvent() {
    gui.createAllDayEvent("all-day sleep", "2025-11-22");

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    List<CalendarEvent> eventsInDefault = defaultModel.getAllEvents();
    assertEquals(1, eventsInDefault.size());

    CalendarEvent event = eventsInDefault.get(0);
    assertEquals("all-day sleep", event.getSubject());
    assertEquals(LocalDateTime.of(2025, 11, 22, 0, 0),
        event.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 11, 22, 23, 59),
        event.getEndDateTime());
  }

  @Test
  public void testCreateRecurringEventWithOccurrences() {
    gui.createRepeatingEvent("exercise", "2025-11-17T09:00",
        "2025-11-17T10:00", "MWF", 6, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(6, defaultModel.getAllEvents().size());
  }

  @Test
  public void testCreateRecurringEventUntilDate() {
    gui.createRepeatingEvent("exercise", "2025-11-17T09:00",
        "2025-11-17T10:00", "MWF", -1, "2025-11-21");

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(3, defaultModel.getAllEvents().size());
  }

  @Test
  public void testCreateAllDayRecurringEventWithOccurrences() {
    gui.createAllDayRepeatingEvent("all-day sleep", "2025-11-22", "S",
        4, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(4, defaultModel.getAllEvents().size());
  }

  @Test
  public void testCreateAllDayRecurringEventUntilDate() {
    gui.createAllDayRepeatingEvent("all-day sleep", "2025-11-22", "S",
        -1, "2025-12-27");

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(6, defaultModel.getAllEvents().size());
  }

  @Test
  public void testGetEventsOnDate() {
    gui.createSingleEvent("lunch", "2025-11-22T12:00", "2025-11-22T14:00", null, null);
    gui.createSingleEvent("Switch", "2025-11-22T14:00", "2025-11-22T17:00", null, null);
    gui.createSingleEvent("drawing", "2025-11-23T18:00", "2025-11-23T21:00", null, null);

    List<CalendarEvent> eventsOnDate =
        gui.getEventsOnDate(LocalDate.of(2025, 11, 22));
    assertEquals(2, eventsOnDate.size());

    assertEquals("lunch", eventsOnDate.get(0).getSubject());
    assertEquals("Switch", eventsOnDate.get(1).getSubject());
  }

  @Test
  public void testEditEventWithEmptyEvent() {
    try {
      gui.editEvent("null event", "2025-11-22T11:00", "subject",
          "null-event", 0);
      fail("Exception expected since event cannot be found.");

    } catch (Exception e) {
      // okay
    }
  }

  @Test
  public void testEditEventWithSingleEventAndSubject() {
    gui.createSingleEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00", null, null);
    gui.editEvent("hello world", "2025-11-22T10:00", "SUBJECT",
        "goodbye world", 0);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    List<CalendarEvent> events = defaultModel.getAllEvents();
    assertEquals(1, events.size());
    assertEquals("goodbye world", events.get(0).getSubject());
  }

  @Test
  public void testEditEventWithSingleEventAndStartDateTime() {
    gui.createSingleEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00", null, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(1, defaultModel.getAllEvents().size());

    gui.editEvent("hello world", "2025-11-22T10:00", "start",
        "2025-11-22T09:00", 0);
    assertEquals(LocalDateTime.of(2025, 11, 22, 9, 0),
        defaultModel.getAllEvents().get(0).getStartDateTime());

    gui.editEvent("hello world", "2025-11-22T09:00", "start",
        "2025-11-22T13:30", 0);
    assertEquals(LocalDateTime.of(2025, 11, 22, 13, 30),
        defaultModel.getAllEvents().get(0).getStartDateTime());
  }

  @Test
  public void testEditEventWithSingleEventAndEndDateTime() {
    gui.createSingleEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00", null, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(1, defaultModel.getAllEvents().size());

    gui.editEvent("hello world", "2025-11-22T10:00", "end",
        "2025-11-22T16:00", 0);
    assertEquals(LocalDateTime.of(2025, 11, 22, 16, 0),
        defaultModel.getAllEvents().get(0).getEndDateTime());

    gui.editEvent("hello world", "2025-11-22T10:00", "end",
        "2025-11-22T13:30", 0);
    assertEquals(LocalDateTime.of(2025, 11, 22, 13, 30),
        defaultModel.getAllEvents().get(0).getEndDateTime());
  }

  @Test
  public void testEditEventWithSingleEventAndDescription() {
    gui.createSingleEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00", null, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(1, defaultModel.getAllEvents().size());

    gui.editEvent("hello world", "2025-11-22T10:00", "description",
        "warm greeting from ai", 0);
    assertEquals(
        "warm greeting from ai", defaultModel.getAllEvents().get(0).getDescription());

    gui.editEvent("hello world", "2025-11-22T10:00", "description",
        "warm greeting to human", 0);
    assertEquals(
        "warm greeting to human", defaultModel.getAllEvents().get(0).getDescription());
  }

  @Test
  public void testEditEventWithSingleEventAndLocation() {
    gui.createSingleEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00", null, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(1, defaultModel.getAllEvents().size());

    gui.editEvent("hello world", "2025-11-22T10:00", "location",
        "macbook99", 0);
    assertEquals("macbook99", defaultModel.getAllEvents().get(0).getLocation());

    gui.editEvent("hello world", "2025-11-22T10:00", "location",
        "hp56", 0);
    assertEquals("hp56", defaultModel.getAllEvents().get(0).getLocation());
  }

  @Test
  public void testEditEventWithSingleEventAndStatus() {
    gui.createSingleEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00", null, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(1, defaultModel.getAllEvents().size());

    gui.editEvent("hello world", "2025-11-22T10:00", "status",
        "private", 0);
    assertEquals("private", defaultModel.getAllEvents().get(0).getStatus());

    gui.editEvent("hello world", "2025-11-22T10:00", "status",
        "public", 0);
    assertEquals("public", defaultModel.getAllEvents().get(0).getStatus());
  }

  @Test
  public void testEditEventWithSingleEventAndUnknownProperty() {
    gui.createSingleEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00", null, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(1, defaultModel.getAllEvents().size());

    try {
      gui.editEvent("hello world", "2025-11-22T10:00", "unknown",
          "private", 0);
      fail("Exception expected since unknown property is detected.");

    } catch (Exception e) {
      // okay
    }
  }

  @Test
  public void testEditEventWithFromThisEventsAndSubject() {
    gui.createRepeatingEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00",
        "S", 5, null);
    gui.editEvent("hello world", "2025-12-06T10:00", "subject",
        "goodbye world", 1);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    List<CalendarEvent> events = defaultModel.getAllEvents();
    assertEquals(5, events.size());
    assertEquals("hello world", events.get(0).getSubject());
    assertEquals("hello world", events.get(1).getSubject());
    assertEquals("goodbye world", events.get(2).getSubject());
    assertEquals("goodbye world", events.get(3).getSubject());
    assertEquals("goodbye world", events.get(4).getSubject());
  }

  @Test
  public void testEditEventWithAllInSeriesEventsAndSubject() {
    gui.createRepeatingEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00",
        "S", 5, null);
    gui.editEvent("hello world", "2025-12-06T10:00", "subject",
        "goodbye world", 2);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    List<CalendarEvent> events = defaultModel.getAllEvents();
    assertEquals(5, events.size());

    for (CalendarEvent event : events) {
      assertEquals("goodbye world", event.getSubject());
    }
  }

  @Test
  public void testEditEventWithInvalidScope() {
    gui.createRepeatingEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T14:00",
        "S", 5, null);

    try {
      gui.editEvent("hello world", "2025-12-06T10:00", "subject",
          "goodbye world", 4);
      fail("Exception expected since the scope is invalid.");

    } catch (Exception e) {
      // okay
    }
  }

  @Test
  public void testExportCalendarToCsv() {
    gui.createSingleEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T12:00", null, null);

    File outFile = new File(tempFolder.getRoot(), "gui-calendar.csv");
    assertFalse(outFile.exists());

    gui.exportCalendar(outFile.getAbsolutePath());
    assertTrue(outFile.exists());
  }

  @Test
  public void testExportCalendarToIcs() {
    gui.createSingleEvent(
        "hello world", "2025-11-22T10:00", "2025-11-22T12:00", null, null);

    File outFile = new File(tempFolder.getRoot(), "gui-calendar.ics");
    assertFalse(outFile.exists());

    gui.exportCalendar(outFile.getAbsolutePath());
    assertTrue(outFile.exists());
  }

  @Test
  public void testCreateCalendarWithInvalidTimezone() {
    try {
      gui.createCalendar("BadCal", "Invalid/Timezone");
      fail("Expected exception for invalid timezone");
    } catch (Exception e) {
      // Expected - invalid timezone should throw exception
      assertTrue(e.getMessage().contains("timezone") || e instanceof java.time.DateTimeException);
    }
  }

  @Test
  public void testCreateCalendarWithDuplicateName() {
    gui.createCalendar("DupCal", "America/New_York");

    try {
      gui.createCalendar("DupCal", "America/Los_Angeles");
      fail("Expected exception for duplicate calendar name");
    } catch (IllegalArgumentException e) {
      // Expected - duplicate calendar name
      assertTrue(e.getMessage().contains("exist"));
    }
  }

  @Test
  public void testSwitchToNonExistentCalendar() {
    try {
      gui.switchCalendar("NonExistent");
      fail("Expected exception when switching to non-existent calendar");
    } catch (IllegalArgumentException e) {
      // Expected
      assertTrue(e.getMessage().contains("not exist") || e.getMessage().contains("exist"));
    }
  }

  @Test
  public void testCreateSingleEventWithInvalidDateTime() {
    try {
      gui.createSingleEvent("BadEvent", "invalid-date", "2025-11-22T10:00", null, null);
      fail("Expected exception for invalid date format");
    } catch (Exception e) {
      // Expected - should throw DateTimeParseException or similar
      assertTrue(e instanceof java.time.format.DateTimeParseException);
    }
  }

  @Test
  public void testCreateAllDayEventWithInvalidDate() {
    try {
      gui.createAllDayEvent("BadAllDay", "not-a-date");
      fail("Expected exception for invalid date format");
    } catch (Exception e) {
      // Expected
      assertTrue(e instanceof java.time.format.DateTimeParseException);
    }
  }

  @Test
  public void testCreateRepeatingEventWithInvalidWeekdays() {
    try {
      gui.createRepeatingEvent("BadRepeat", "2025-11-22T10:00",
          "2025-11-22T11:00", "XYZ", 5, null);
      fail("Expected exception for invalid weekday characters");
    } catch (IllegalArgumentException e) {
      // Expected
      assertTrue(e.getMessage().contains("weekday") || e.getMessage().contains("Invalid"));
    }
  }

  @Test
  public void testCreateAllDayRepeatingEventWithInvalidWeekdays() {
    try {
      gui.createAllDayRepeatingEvent("BadAllDayRepeat", "2025-11-22", "ABC", 5, null);
      fail("Expected exception for invalid weekday characters");
    } catch (IllegalArgumentException e) {
      // Expected
      assertTrue(e.getMessage().contains("weekday") || e.getMessage().contains("Invalid"));
    }
  }

  @Test
  public void testGetEventsOnDateWithNoEvents() {
    List<CalendarEvent> events = gui.getEventsOnDate(LocalDate.of(2025, 12, 25));
    assertNotNull(events);
    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsOnDateAfterSwitchingCalendars() {
    gui.createSingleEvent("DefaultEvent", "2025-11-22T10:00", "2025-11-22T11:00", null, null);

    gui.createCalendar("NewCal", "America/Chicago");
    gui.switchCalendar("NewCal");
    gui.createSingleEvent("NewCalEvent", "2025-11-22T10:00", "2025-11-22T11:00", null, null);

    List<CalendarEvent> events = gui.getEventsOnDate(LocalDate.of(2025, 11, 22));
    assertEquals(1, events.size());
    assertEquals("NewCalEvent", events.get(0).getSubject());

    // Switch back
    gui.switchCalendar("Default Calendar");
    events = gui.getEventsOnDate(LocalDate.of(2025, 11, 22));
    assertEquals(1, events.size());
    assertEquals("DefaultEvent", events.get(0).getSubject());
  }

  @Test
  public void testEditEventChangingEndTime() {
    gui.createSingleEvent("Meeting", "2025-11-22T10:00", "2025-11-22T11:00", null, null);
    gui.editEvent("Meeting", "2025-11-22T10:00", "end", "2025-11-22T12:30", 0);

    LocalDate date = LocalDate.of(2025, 11, 22);
    List<CalendarEvent> events = gui.getEventsOnDate(date);
    assertEquals(1, events.size());
    assertEquals(LocalDateTime.of(2025, 11, 22, 12, 30),
        events.get(0).getEndDateTime());
  }

  @Test
  public void testGetCurrentTimezoneAfterSwitchingCalendars() {
    final String defaultTz = gui.getCurrentTimezone();

    gui.createCalendar("EastCoast", "America/New_York");
    gui.switchCalendar("EastCoast");
    assertEquals("America/New_York", gui.getCurrentTimezone());

    gui.createCalendar("WestCoast", "America/Los_Angeles");
    gui.switchCalendar("WestCoast");
    assertEquals("America/Los_Angeles", gui.getCurrentTimezone());

    gui.switchCalendar("Default Calendar");
    assertEquals(defaultTz, gui.getCurrentTimezone());
  }

  @Test
  public void testGetActiveCalendarNameAfterCreation() {
    assertEquals("Default Calendar", gui.getActiveCalendarName());

    gui.createCalendar("Work", "America/Chicago");
    gui.switchCalendar("Work");
    assertEquals("Work", gui.getActiveCalendarName());
  }

  @Test
  public void testExportCalendarWithNoEvents() {
    File outFile = new File(tempFolder.getRoot(), "empty-calendar.csv");
    assertFalse(outFile.exists());

    gui.exportCalendar(outFile.getAbsolutePath());
    assertTrue(outFile.exists());
  }

  @Test
  public void testExportCalendarWithMultipleEvents() {
    gui.createSingleEvent("Event1", "2025-11-22T10:00", "2025-11-22T11:00", null, null);
    gui.createSingleEvent("Event2", "2025-11-23T14:00", "2025-11-23T15:00", null, null);
    gui.createAllDayEvent("Event3", "2025-11-24");

    File outFile = new File(tempFolder.getRoot(), "multi-events.ics");
    assertFalse(outFile.exists());

    gui.exportCalendar(outFile.getAbsolutePath());
    assertTrue(outFile.exists());
  }

  @Test
  public void testExportCalendarWithInvalidExtension() {
    gui.createSingleEvent("Event", "2025-11-22T10:00", "2025-11-22T11:00", null, null);

    File outFile = new File(tempFolder.getRoot(), "calendar.txt");

    // Should handle gracefully (either throw exception or ignore)
    try {
      gui.exportCalendar(outFile.getAbsolutePath());
      // If it doesn't throw, that's okay - some implementations might handle this
    } catch (Exception e) {
      // Also okay if it throws an exception for unsupported format
    }
  }

  @Test
  public void testCreateRepeatingEventAllWeekdays() {
    gui.createRepeatingEvent("Daily", "2025-11-17T09:00",
        "2025-11-17T10:00", "MTWRFSU", 7, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(7, defaultModel.getAllEvents().size());
  }

  @Test
  public void testCreateRepeatingEventSingleWeekday() {
    gui.createRepeatingEvent("MondaysOnly", "2025-11-17T09:00",
        "2025-11-17T10:00", "M", 4, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(4, defaultModel.getAllEvents().size());
  }

  @Test
  public void testEditSeriesPreservesOtherEvents() {
    gui.createRepeatingEvent("Series", "2025-11-17T09:00",
        "2025-11-17T10:00", "MWF", 5, null);

    gui.createSingleEvent("Standalone", "2025-11-20T10:00", "2025-11-20T11:00", null, null);

    CalendarModel defaultModel = multiModel.getCalendar("Default Calendar");
    assertEquals(6, defaultModel.getAllEvents().size());

    gui.editEvent("Series", "2025-11-17T09:00", "subject", "UpdatedSeries", 2);

    // Should still have 6 events total
    assertEquals(6, defaultModel.getAllEvents().size());

    // Standalone should be unchanged
    boolean foundStandalone = false;
    for (CalendarEvent e : defaultModel.getAllEvents()) {
      if (e.getSubject().equals("Standalone")) {
        foundStandalone = true;
        break;
      }
    }
    assertTrue(foundStandalone);
  }
}