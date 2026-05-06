package controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertFalse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import model.CalendarEvent;
import model.CalendarModel;
import model.CalendarModelImpl;
import model.MultiCalendarModel;
import model.MultiCalendarModelImpl;
import org.junit.Before;
import org.junit.Test;
import view.CalendarView;

/**
 * Comprehensive test suite for CalendarController.
 */
public class CalendarControllerTest {

  private CalendarModel model;
  private MultiCalendarModel multiModel;
  private MockCalendarView view;
  private CalendarController controller;
  private CalendarController multiCtrl;

  /**
   * Mock implementation of CalendarView for testing.
   */
  private static class MockCalendarView implements CalendarView {
    private final List<String> messages = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private String lastExportFilename;
    private List<CalendarEvent> lastExportedEvents;

    @Override
    public void displayMessage(String message) {
      messages.add(message);
    }

    @Override
    public void displayError(String error) {
      errors.add(error);
    }

    @Override
    public void displayEventsOnDate(LocalDate date, List<CalendarEvent> events) {
      messages.add("Events on " + date + ": " + events.size());
    }

    @Override
    public void displayEventsInRange(LocalDateTime start, LocalDateTime end,
                                     List<CalendarEvent> events) {
      messages.add("Events from " + start + " to " + end + ": " + events.size());
    }

    @Override
    public void displayStatus(LocalDateTime dateTime, boolean busy) {
      messages.add("Status at " + dateTime + ": " + (busy ? "Busy" : "Available"));
    }

    @Override
    public void exportToCsv(String filename, List<CalendarEvent> events) {
      lastExportFilename = filename;
      lastExportedEvents = events;
      messages.add("Exported " + events.size() + " events to " + filename);
    }

    @Override
    public void exportAuto(String filename, List<CalendarEvent> events) {
      lastExportFilename = filename;
      lastExportedEvents = events;
      messages.add("Exported " + events.size() + " events to " + filename);
    }

    @Override
    public String getUserInput() {
      return null;
    }

    public void reset() {
      messages.clear();
      errors.clear();
      lastExportFilename = null;
      lastExportedEvents = null;
    }

    public List<String> getMessages() {
      return messages;
    }

    public List<String> getErrors() {
      return errors;
    }

    public String getLastExportFilename() {
      return lastExportFilename;
    }

    public List<CalendarEvent> getLastExportedEvents() {
      return lastExportedEvents;
    }
  }

  /**
   * Sets up the test fixture before each test.
   * Initializes a fresh calendar model, mock view, and controller.
   */
  @Before
  public void setUp() {
    model = new CalendarModelImpl();
    multiModel = new MultiCalendarModelImpl();
    view = new MockCalendarView();
    controller = new CalendarController(model, view);
    multiCtrl = new CalendarController(multiModel, view);
  }

  @Test
  public void testCreateSingleEventWithQuotes() {
    controller.processCommand(
        "create event \"Team Meeting\" from 2025-05-01T10:00 to 2025-05-01T11:00"
    );

    assertEquals(1, model.getAllEvents().size());
    assertEquals("Team Meeting", model.getAllEvents().get(0).getSubject());
    assertTrue(view.getMessages().get(0).contains("successfully"));
  }

  @Test
  public void testCreateSingleEventWithoutQuotes() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );

    assertEquals(1, model.getAllEvents().size());
    assertEquals("Meeting", model.getAllEvents().get(0).getSubject());
  }


  //  @Test
  //  public void testCreateAllDayEventWithQuotes() {
  //    controller.processCommand("create event \"Holiday\" on 2025-05-01");
  //
  //    assertEquals(1, model.getAllEvents().size());
  //    CalendarEvent event = model.getAllEvents().get(0);
  //    assertEquals("Holiday", event.getSubject());
  //    assertEquals(8, event.getStartDateTime().getHour());
  //    assertEquals(17, event.getEndDateTime().getHour());
  //  }

  @Test
  public void testCreateAllDayEventWithoutQuotes() {
    controller.processCommand("create event Holiday on 2025-05-01");

    assertEquals(1, model.getAllEvents().size());
    assertEquals("Holiday", model.getAllEvents().get(0).getSubject());
  }

  @Test
  public void testCreateRepeatingEventWithQuotesForNtimes() {
    controller.processCommand(
        "create event \"Standup\" from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MWF for 6 times"
    );

    assertEquals(6, model.getAllEvents().size());
    assertTrue(view.getMessages().get(0).contains("6 occurrences"));
  }

  @Test
  public void testCreateRepeatingEventWithoutQuotesForNtimes() {
    controller.processCommand(
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MWF for 6 times"
    );

    assertEquals(6, model.getAllEvents().size());
  }

  @Test
  public void testCreateRepeatingEventWithQuotesUntilDate() {
    controller.processCommand(
        "create event \"Standup\" from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MW until 2025-05-14"
    );

    assertEquals(4, model.getAllEvents().size());
  }

  @Test
  public void testCreateRepeatingEventWithoutQuotesUntilDate() {
    controller.processCommand(
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MW until 2025-05-14"
    );

    assertEquals(4, model.getAllEvents().size());
  }

  @Test
  public void testCreateAllDayRepeatingEventWithQuotesForNtimes() {
    controller.processCommand(
        "create event \"Weekend Hike\" on 2025-05-10 repeats S for 4 times"
    );

    assertEquals(4, model.getAllEvents().size());
  }

  @Test
  public void testCreateAllDayRepeatingEventWithoutQuotesForNtimes() {
    controller.processCommand(
        "create event Hike on 2025-05-10 repeats S for 4 times"
    );

    assertEquals(4, model.getAllEvents().size());
  }

  @Test
  public void testCreateAllDayRepeatingEventWithQuotesUntilDate() {
    controller.processCommand(
        "create event \"Training\" on 2025-05-05 repeats MWF until 2025-05-16"
    );

    assertEquals(6, model.getAllEvents().size());
  }

  @Test
  public void testCreateAllDayRepeatingEventWithoutQuotesUntilDate() {
    controller.processCommand(
        "create event Training on 2025-05-05 repeats MWF until 2025-05-16"
    );

    assertEquals(6, model.getAllEvents().size());
  }

  @Test
  public void testEditEventSubjectWithoutQuotes() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "edit event subject Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with ImportantMeeting"
    );

    assertEquals("ImportantMeeting", model.getAllEvents().get(0).getSubject());
  }

  @Test
  public void testEditEventLocation() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "edit event location Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with Room101"
    );

    assertEquals("Room101", model.getAllEvents().get(0).getLocation());
  }

  @Test
  public void testEditEventDescription() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "edit event description Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with TeamSync"
    );

    assertEquals("TeamSync", model.getAllEvents().get(0).getDescription());
  }

  @Test
  public void testEditEventStatus() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "edit event status Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with private"
    );

    assertEquals("private", model.getAllEvents().get(0).getStatus());
  }

  @Test
  public void testEditEventStartTime() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "edit event start Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with 2025-05-01T09:00"
    );

    assertEquals(LocalDateTime.of(2025, 5, 1, 9, 0),
        model.getAllEvents().get(0).getStartDateTime());
  }

  @Test
  public void testEditEventEndTime() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "edit event end Meeting from 2025-05-01T10:00 to 2025-05-01T11:00 "
            + "with 2025-05-01T12:00"
    );

    assertEquals(LocalDateTime.of(2025, 5, 1, 12, 0),
        model.getAllEvents().get(0).getEndDateTime());
  }

  @Test
  public void testEditEventWithQuotesWithoutEndTime() {
    controller.processCommand(
        "create event \"Meeting\" from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "edit event location \"Meeting\" from 2025-05-01T10:00 with Room202"
    );

    assertEquals("Room202", model.getAllEvents().get(0).getLocation());
  }

  @Test
  public void testEditEventWithoutQuotesWithoutEndTime() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "edit event location Meeting from 2025-05-01T10:00 with Room202"
    );

    assertEquals("Room202", model.getAllEvents().get(0).getLocation());
  }

  @Test
  public void testEditEventsFromThisWithQuotes() {
    controller.processCommand(
        "create event \"Standup\" from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MW for 4 times"
    );
    controller.processCommand(
        "edit events subject \"Standup\" from 2025-05-07T09:00 with Modified"
    );

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals("Standup", events.get(0).getSubject());
    assertEquals("Modified", events.get(1).getSubject());
    assertEquals("Modified", events.get(2).getSubject());
    assertEquals("Modified", events.get(3).getSubject());
  }

  @Test
  public void testEditEventsFromThisWithoutQuotes() {
    controller.processCommand(
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MW for 4 times"
    );
    controller.processCommand(
        "edit events subject Standup from 2025-05-07T09:00 with Modified"
    );

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals("Standup", events.get(0).getSubject());
    assertEquals("Modified", events.get(1).getSubject());
  }

  @Test
  public void testEditSeriesWithQuotes() {
    controller.processCommand(
        "create event \"Standup\" from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MW for 4 times"
    );
    controller.processCommand(
        "edit series subject \"Standup\" from 2025-05-05T09:00 with NewStandup"
    );

    for (CalendarEvent event : model.getAllEvents()) {
      assertEquals("NewStandup", event.getSubject());
    }
  }

  @Test
  public void testEditSeriesWithoutQuotes() {
    controller.processCommand(
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MW for 4 times"
    );
    controller.processCommand(
        "edit series subject Standup from 2025-05-05T09:00 with NewStandup"
    );

    for (CalendarEvent event : model.getAllEvents()) {
      assertEquals("NewStandup", event.getSubject());
    }
  }

  @Test
  public void testPrintEventsOnDate() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand("print events on 2025-05-01");

    assertTrue(view.getMessages().get(1).contains("Events on 2025-05-01"));
  }

  @Test
  public void testPrintEventsInRange() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "print events from 2025-05-01T00:00 to 2025-05-31T23:59"
    );

    assertTrue(view.getMessages().get(1).contains("Events from"));
  }

  @Test
  public void testExportCalendar() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand("export cal calendar.csv");

    assertEquals("calendar.csv", view.getLastExportFilename());
    assertEquals(1, view.getLastExportedEvents().size());
  }

  @Test
  public void testShowStatusBusy() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand("show status on 2025-05-01T10:30");

    assertTrue(view.getMessages().get(1).contains("Busy"));
  }

  @Test
  public void testShowStatusAvailable() {
    controller.processCommand("show status on 2025-05-01T10:30");

    assertTrue(view.getMessages().get(0).contains("Available"));
  }

  @Test
  public void testUnknownCommand() {
    controller.processCommand("unknown command");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Unknown command"));
  }

  @Test
  public void testInvalidCreateEventFormat() {
    controller.processCommand("create event invalid format");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Invalid"));
  }

  @Test
  public void testInvalidDateTimeFormat() {
    controller.processCommand(
        "create event Meeting from invalid-date to 2025-05-01T11:00"
    );

    assertEquals(1, view.getErrors().size());
  }

  @Test
  public void testEditNonExistentEvent() {
    controller.processCommand(
        "edit event subject Meeting from 2025-05-01T10:00 with NewSubject"
    );

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("No event found"));
  }

  @Test
  public void testEditInvalidProperty() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );
    controller.processCommand(
        "edit event invalid Meeting from 2025-05-01T10:00 with value"
    );

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Unknown property"));
  }

  @Test
  public void testInvalidEditEventFormat() {
    controller.processCommand("edit event invalid format");

    assertEquals(1, view.getErrors().size());
  }

  @Test
  public void testInvalidEditEventsFormat() {
    controller.processCommand("edit events invalid format");

    assertEquals(1, view.getErrors().size());
  }

  @Test
  public void testInvalidEditSeriesFormat() {
    controller.processCommand("edit series invalid format");

    assertEquals(1, view.getErrors().size());
  }

  @Test
  public void testInvalidPrintEventsInRangeFormat() {
    controller.processCommand("print events from 2025-05-01T10:00");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Invalid date range format"));
  }

  @Test
  public void testEmptyCommand() {
    controller.processCommand("");
    assertEquals(0, view.getErrors().size());
    assertEquals(0, view.getMessages().size());
  }

  @Test
  public void testExitCommand() {
    controller.processCommand("exit");
    assertEquals(0, view.getErrors().size());
    assertEquals(0, view.getMessages().size());
  }

  @Test
  public void testCaseInsensitiveExit() {
    controller.processCommand("EXIT");
    assertEquals(0, view.getErrors().size());
    assertEquals(0, view.getMessages().size());
  }

  //  @Test
  //  public void testCompleteWorkflow() {
  //    controller.processCommand(
  //        "create event \"Team Meeting\" from 2025-05-01T10:00 to 2025-05-01T11:00"
  //    );
  //    controller.processCommand(
  //        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
  //        + "repeats MWF for 3 times"
  //    );
  //
  //    assertEquals(4, model.getAllEvents().size());
  //
  //    controller.processCommand(
  //        "edit event location \"Team Meeting\" from 2025-05-01T10:00 with RoomA"
  //    );
  //
  //    assertEquals("RoomA", model.getAllEvents().get(0).getLocation());
  //
  //    controller.processCommand("print events on 2025-05-01");
  //    assertTrue(view.getMessages().size() > 0);
  //
  //    controller.processCommand("show status on 2025-05-01T10:30");
  //    assertTrue(view.getMessages().get(view.getMessages().size() - 1).contains("Busy"));
  //  }

  @Test
  public void testEditEventsFromThisMultipleEventsFound() {
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

    model.addEvent(event1);

    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );

    controller.processCommand(
        "edit events subject Meeting from 2025-05-01T10:00 with NewMeeting"
    );

    assertTrue(view.getMessages().size() > 0);
  }

  @Test
  public void testEditEventsFromThisNoEventFound() {
    controller.processCommand(
        "edit events subject Meeting from 2025-05-01T10:00 with NewMeeting"
    );

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("No event found"));
  }

  @Test
  public void testEditEventsFromThisLocationProperty() {
    controller.processCommand(
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MW for 4 times"
    );
    controller.processCommand(
        "edit events location Standup from 2025-05-07T09:00 with RoomB"
    );

    List<CalendarEvent> events = model.getAllEvents();
    assertNull(events.get(0).getLocation());
    assertEquals("RoomB", events.get(1).getLocation());
    assertEquals("RoomB", events.get(2).getLocation());
  }

  @Test
  public void testEditEventsFromThisDescriptionProperty() {
    controller.processCommand(
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MW for 4 times"
    );
    controller.processCommand(
        "edit events description Standup from 2025-05-07T09:00 with DailySync"
    );

    List<CalendarEvent> events = model.getAllEvents();
    assertNull(events.get(0).getDescription());
    assertEquals("DailySync", events.get(1).getDescription());
  }

  @Test
  public void testEditEventsFromThisStatusProperty() {
    controller.processCommand(
        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
            + "repeats MW for 4 times"
    );
    controller.processCommand(
        "edit events status Standup from 2025-05-07T09:00 with private"
    );

    List<CalendarEvent> events = model.getAllEvents();
    assertEquals("public", events.get(0).getStatus());
    assertEquals("private", events.get(1).getStatus());
  }

  //  @Test
  //  public void testEditEventsFromThisEndProperty() {
  //    controller.processCommand(
  //        "create event Standup from 2025-05-05T09:00 to 2025-05-05T09:30 "
  //        + "repeats M for 3 times"
  //    );
  //    controller.processCommand(
  //        "edit events end Standup from 2025-05-12T09:00 with 2025-05-12T10:30"
  //    );
  //
  //    List<CalendarEvent> events = model.getAllEvents();
  //    assertEquals(LocalDateTime.of(2025, 5, 5, 9, 30),
  //        events.get(0).getEndDateTime());
  //    assertEquals(LocalDateTime.of(2025, 5, 12, 10, 30),
  //        events.get(1).getEndDateTime());
  //  }

  // ========================= Testing Create Calendar Method ================================= //

  @Test
  public void testCreateCalendarM1() {
    multiCtrl.processCommand("create calendar --name \"*boston_trip 1**\" "
        + "--timezone America/New_York");

    assertTrue(multiModel.existCalendar("*boston_trip 1**"));
    assertEquals(ZoneId.of("America/New_York"),
        multiModel.getTimeZone("*boston_trip 1**"));
    assertTrue(view.getMessages().get(0).contains("created in timezone America/New_York"));
  }

  @Test
  public void testCreateCalendarM2() {
    multiCtrl.processCommand("create calendar --name BostonTrip --timezone America/New_York");

    assertTrue(multiModel.existCalendar("BostonTrip"));
    assertEquals(ZoneId.of("America/New_York"),
        multiModel.getTimeZone("BostonTrip"));
    assertTrue(view.getMessages().get(0).contains("created in timezone America/New_York"));
  }

  @Test
  public void testCreateCalendarInvalidCommand() {
    multiCtrl.processCommand("create calendar --name MissingTimezone America/New_York");

    assertFalse(multiModel.existCalendar("MissingTimezone"));
    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Invalid command"));
  }

  @Test
  public void testCreateCalendarInvalidTimezone() {
    multiCtrl.processCommand("create calendar --name TokyoTrip --timezone Sakura/Tokyo");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Unsupported timezone"));
  }

  @Test
  public void testCreateCalendarDuplicateName() {
    multiCtrl.processCommand("create calendar --name Friday --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Friday --timezone Europe/Paris");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("already exists"));
  }

  // ========================= Testing Edit Calendar Method ================================= //

  @Test
  public void testEditCalendarRenameBothWithQuotes() {
    multiCtrl.processCommand("create calendar --name \"CS10 00\" --timezone America/New_York");
    assertTrue(multiModel.existCalendar("CS10 00"));
    assertFalse(multiModel.existCalendar("CS1000"));

    multiCtrl.processCommand("edit calendar --name \"CS10 00\" --property name \"CS1000\"");

    assertTrue(multiModel.existCalendar("CS1000"));
    assertFalse(multiModel.existCalendar("CS10 00"));
  }

  @Test
  public void testEditCalendarRenameOneWithQuotes1() {
    multiCtrl.processCommand("create calendar --name CS1010 --timezone America/New_York");
    assertTrue(multiModel.existCalendar("CS1010"));
    assertFalse(multiModel.existCalendar("CS1_0_1_0"));

    multiCtrl.processCommand("edit calendar --name CS1010 --property name \"CS1_0_1_0\"");

    assertTrue(multiModel.existCalendar("CS1_0_1_0"));
    assertFalse(multiModel.existCalendar("CS1010"));
  }

  @Test
  public void testEditCalendarRenameOneWithQuotes2() {
    multiCtrl.processCommand("create calendar --name \"CS great!\" --timezone America/New_York");
    assertTrue(multiModel.existCalendar("CS great!"));
    assertFalse(multiModel.existCalendar("GREAT!"));

    multiCtrl.processCommand("edit calendar --name \"CS great!\" --property name GREAT!");

    assertTrue(multiModel.existCalendar("GREAT!"));
    assertFalse(multiModel.existCalendar("CS great!"));
  }

  @Test
  public void testEditCalendarRenameNoQuotes() {
    multiCtrl.processCommand("create calendar --name calendar0 --timezone America/New_York");
    assertTrue(multiModel.existCalendar("calendar0"));
    assertFalse(multiModel.existCalendar("calendar1"));

    multiCtrl.processCommand("edit calendar --name calendar0 --property name calendar1");

    assertTrue(multiModel.existCalendar("calendar1"));
    assertFalse(multiModel.existCalendar("calendar0"));
  }

  @Test
  public void testEditCalendarRenameDuplicate() {
    multiCtrl.processCommand("create calendar --name calendar00 --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name calendar000 --timezone America/New_York");
    assertTrue(multiModel.existCalendar("calendar00"));
    assertTrue(multiModel.existCalendar("calendar000"));

    multiCtrl.processCommand("edit calendar --name calendar00 --property name calendar000");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("already exists"));
  }

  @Test
  public void testEditCalendarChangeTimezone() {
    multiCtrl.processCommand("create calendar --name globalTrip --timezone America/New_York");
    multiCtrl.processCommand("edit calendar --name globalTrip --property timezone America/Chicago");

    assertEquals(ZoneId.of("America/Chicago"),
        multiModel.getTimeZone("globalTrip"));
    String msg = view.messages.get(view.messages.size() - 1).toLowerCase();
    assertTrue(msg.contains("successfully modified timezone"));
  }

  @Test
  public void testEditCalendarChangeTimezoneInvalid() {
    multiCtrl.processCommand("create calendar --name globalTrip2 --timezone America/New_York");
    multiCtrl.processCommand("edit calendar --name globalTrip2 --property timezone Null/Null");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Unsupported timezone"));
    assertEquals(ZoneId.of("America/New_York"),
        multiModel.getTimeZone("globalTrip2"));
  }

  @Test
  public void testEditCalendarUnknownProperty() {
    multiCtrl.processCommand("create calendar --name calendar99 --timezone America/New_York");
    multiCtrl.processCommand("edit calendar --name calendar99 --property unknownProperty hello");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Unknown property"));
  }

  @Test
  public void testEditCalendarInvalidCommand() {
    multiCtrl.processCommand("create calendar --name calendar9 --timezone America/New_York");
    multiCtrl.processCommand("edit calendar hello world!");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Invalid command"));
  }

  // ========================= Testing Use Calendar Method ================================= //

  @Test
  public void testUseCalendar() {
    multiCtrl.processCommand("create calendar --name skyView --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name oceanView --timezone Europe/Paris");

    assertTrue(multiModel.existCalendar("skyView"));
    assertTrue(multiModel.existCalendar("oceanView"));


    multiCtrl.processCommand("use calendar --name skyView");
    assertTrue(view.messages.get(view.messages.size() - 1).contains("Using calendar \"skyView\""));

    multiCtrl.processCommand("create event Meeting from 2025-05-29T10:00 to 2025-05-29T11:00");

    assertEquals(1, multiModel.getCalendar("skyView").getAllEvents().size());
    assertEquals(0, multiModel.getCalendar("oceanView")
        .getAllEvents().size());
  }

  // ========================= Testing Copy Event Method ================================= //

  @Test
  public void testCopyEventSingleEvent() {
    multiCtrl.processCommand("create calendar --name sourceCal --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name targetCal --timezone America/Los_Angeles");
    multiCtrl.processCommand("use calendar --name sourceCal");

    assertTrue(view.getErrors().isEmpty());

    multiCtrl.processCommand("create event Meeting from 2025-12-01T10:00 to 2025-12-01T11:00");
    assertEquals(1,
        multiModel.getCalendar("sourceCal").getAllEvents().size());

    multiCtrl.processCommand("copy event Meeting on 2025-12-01T10:00 --target targetCal "
        + "to 2025-12-05T09:00");

    assertTrue(view.getErrors().isEmpty());

    assertEquals(1,
        multiModel.getCalendar("targetCal").getAllEvents().size());
    assertEquals(LocalDateTime.of(2025, 12, 5, 9, 0),
        multiModel.getCalendar("targetCal").getAllEvents().get(0).getStartDateTime());

    assertEquals(1,
        multiModel.getCalendar("sourceCal").getAllEvents().size());
  }

  @Test
  public void testCopyEventWholeSeries() {
    multiCtrl.processCommand("create calendar --name watchMermaid --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name watchH2O --timezone America/Los_Angeles");
    multiCtrl.processCommand("use calendar --name watchMermaid");

    assertTrue(view.getErrors().isEmpty());

    multiCtrl.processCommand("create event watchInBed from 2025-11-01T20:00 to 2025-11-01T23:00 "
        + "repeats MWS for 4 times");
    assertEquals(4,
        multiModel.getCalendar("watchMermaid").getAllEvents().size());

    multiCtrl.processCommand("copy event watchInBed on 2025-11-01T20:00 --target watchH2O "
        + "to 2025-12-05T21:30");

    assertTrue(view.getErrors().isEmpty());

    assertEquals(4,
        multiModel.getCalendar("watchH2O").getAllEvents().size());
    assertEquals(LocalDateTime.of(2025, 12, 5, 21, 30),
        multiModel.getCalendar("watchH2O").getAllEvents().get(0).getStartDateTime());

    assertEquals(4,
        multiModel.getCalendar("watchMermaid").getAllEvents().size());
  }

  @Test
  public void testCopyEventsOnSingleEvent() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    multiCtrl.processCommand("create event Meeting from 2025-01-15T10:00 to 2025-01-15T11:00");
    assertEquals(1, multiModel.getCalendar("Source").getAllEvents().size());

    multiCtrl.processCommand("copy events on 2025-01-15 --target Target to 2025-02-20");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(1, multiModel.getCalendar("Target").getAllEvents().size());
    assertEquals("Meeting", multiModel.getCalendar("Target").getAllEvents().get(0).getSubject());
    assertEquals(LocalDateTime.of(2025, 2, 20, 10, 0),
        multiModel.getCalendar("Target").getAllEvents().get(0).getStartDateTime());
  }

  @Test
  public void testCopyEventsOnMultipleEvents() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    multiCtrl.processCommand("create event Morning from 2025-01-15T09:00 to 2025-01-15T10:00");
    multiCtrl.processCommand("create event Lunch from 2025-01-15T12:00 to 2025-01-15T13:00");
    multiCtrl.processCommand("create event Evening from 2025-01-15T17:00 to 2025-01-15T18:00");

    multiCtrl.processCommand("copy events on 2025-01-15 --target Target to 2025-03-10");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(3, multiModel.getCalendar("Target").getAllEvents().size());
  }

  @Test
  public void testCopyEventsOnWithQuotedCalendarName() {
    multiCtrl.processCommand("create calendar --name \"My Source\" --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name \"My Target\" --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name \"My Source\"");

    multiCtrl.processCommand("create event Test from 2025-01-15T10:00 to 2025-01-15T11:00");

    multiCtrl.processCommand("copy events on 2025-01-15 --target \"My Target\" to 2025-02-15");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(1, multiModel.getCalendar("My Target").getAllEvents().size());
  }

  @Test
  public void testCopyEventsOnNoCalendarInUse() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");

    // Don't use any calendar
    multiCtrl.processCommand("copy events on 2025-01-15 --target Target to 2025-02-15");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("No calendar in use"));
  }

  @Test
  public void testCopyEventsOnNonExistentTargetCalendar() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");
    multiCtrl.processCommand("create event Test from 2025-01-15T10:00 to 2025-01-15T11:00");

    multiCtrl.processCommand("copy events on 2025-01-15 --target NonExistent to 2025-02-15");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("does not exist"));
  }

  @Test
  public void testCopyEventsOnEmptyDate() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    // No events on this date
    multiCtrl.processCommand("copy events on 2025-01-15 --target Target to 2025-02-15");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(0, multiModel.getCalendar("Target").getAllEvents().size());
  }

  @Test
  public void testCopyEventsOnWithTimezoneConversion() {
    multiCtrl.processCommand("create calendar --name NYC --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name LA --timezone America/Los_Angeles");
    multiCtrl.processCommand("use calendar --name NYC");

    multiCtrl.processCommand("create event Call from 2025-01-15T14:00 to 2025-01-15T15:00");

    multiCtrl.processCommand("copy events on 2025-01-15 --target LA to 2025-01-15");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(1, multiModel.getCalendar("LA").getAllEvents().size());
    // 2pm EST = 11am PST
    assertEquals(LocalDateTime.of(2025, 1, 15, 11, 0),
        multiModel.getCalendar("LA").getAllEvents().get(0).getStartDateTime());
  }

  @Test
  public void testCopyEventsOnInvalidDateFormat() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    multiCtrl.processCommand("copy events on 2025-99-99 --target Target to 2025-02-15");

    assertEquals(1, view.getErrors().size());
  }

  @Test
  public void testCopyEventsOnInvalidCommandFormat() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    // Missing date
    multiCtrl.processCommand("copy events on --target Target to 2025-02-15");

    assertEquals(1, view.getErrors().size());
  }

  @Test
  public void testCopyEventsBetweenSingleDay() {
    multiCtrl.processCommand("create calendar --name Fall2024 --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Spring2025 --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Fall2024");

    multiCtrl.processCommand("create event Lecture from 2024-09-05T10:00 to 2024-09-05T11:30");

    multiCtrl.processCommand("copy events between 2024-09-05 and 2024-09-05"
        + " --target Spring2025 to 2025-01-08");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(1, multiModel.getCalendar("Spring2025").getAllEvents().size());
    assertEquals("Lecture", multiModel.getCalendar("Spri"
        + "ng2025").getAllEvents().get(0).getSubject());
  }

  @Test
  public void testCopyEventsBetweenMultipleDays() {
    multiCtrl.processCommand("create calendar --name Fall2024 --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Spring2025 --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Fall2024");

    multiCtrl.processCommand("create event Day1 from 2024-09-05T10:00 to 2024-09-05T11:00");
    multiCtrl.processCommand("create event Day2 from 2024-09-09T10:00 to 2024-09-09T11:00");
    multiCtrl.processCommand("create event Day3 from 2024-09-11T10:00 to 2024-09-11T11:00");

    multiCtrl.processCommand("copy events between 2024-09-05 "
        + "and 2024-09-11 --target Spring2025 to 2025-01-08");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(3, multiModel.getCalendar("Spring2025").getAllEvents().size());
  }

  @Test
  public void testCopyEventsBetweenWithQuotedNames() {
    multiCtrl.processCommand("create calendar --name \"Fall "
        + "Semester\" --timezone America/New_York");
    multiCtrl.processCommand("create calendar --"
        + "name \"Spring Semester\" --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name \"Fall Semester\"");

    multiCtrl.processCommand("create event Class from 2024-09-05T10:00 to 2024-09-05T11:00");

    multiCtrl.processCommand("copy events "
        + "between 2024-09-05 and 2024-09-10 --target \"Spring Semester\" to 2025-01-08");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(1, multiModel.getCalendar("Spring Semester").getAllEvents().size());
  }

  @Test
  public void testCopyEventsBetweenNoCalendarInUse() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");

    multiCtrl.processCommand("copy events between "
        + "2024-09-05 and 2024-09-10 --target Target to 2025-01-08");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("No calendar in use"));
  }

  @Test
  public void testCopyEventsBetweenNonExistentTargetCalendar() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");
    multiCtrl.processCommand("create event Test from 2024-09-05T10:00 to 2024-09-05T11:00");

    multiCtrl.processCommand("copy events between 2024-09-05 "
        + "and 2024-09-10 --target NonExistent to 2025-01-08");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("does not exist"));
  }

  @Test
  public void testCopyEventsBetweenEmptyRange() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    // No events in this range
    multiCtrl.processCommand("copy events between 2024-09-05 and "
        + "2024-09-10 --target Target to 2025-01-08");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(0, multiModel.getCalendar("Target").getAllEvents().size());
  }

  @Test
  public void testCopyEventsBetweenWithTimezones() {
    multiCtrl.processCommand("create calendar --name Tokyo --timezone Asia/Tokyo");
    multiCtrl.processCommand("create calendar --name London --timezone Europe/London");
    multiCtrl.processCommand("use calendar --name Tokyo");

    multiCtrl.processCommand("create event Meeting from 2024-09-05T15:00 to 2024-09-05T16:00");
    multiCtrl.processCommand("create event Lunch from 2024-09-06T12:00 to 2024-09-06T13:00");

    multiCtrl.processCommand("copy events between 2024-09-05 "
        + "and 2024-09-06 --target London to 2025-01-08");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(2, multiModel.getCalendar("London").getAllEvents().size());
  }

  @Test
  public void testCopyEventsBetweenInvalidDateFormat() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    multiCtrl.processCommand("copy events between 2024-99-99 "
        + "and 2024-09-10 --target Target to 2025-01-08");

    assertEquals(1, view.getErrors().size());
  }

  @Test
  public void testCopyEventsBetweenInvalidCommandFormatMissingAnd() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    multiCtrl.processCommand("copy events between 2024-09-05 2024-09-10 "
        + "--target Target to 2025-01-08");

    assertEquals(1, view.getErrors().size());
  }

  @Test
  public void testCopyEventsBetweenMissingTarget() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    multiCtrl.processCommand("copy events between 2024-09-05 and 2024-09-10 to 2025-01-08");

    assertEquals(1, view.getErrors().size());
  }

  @Test
  public void testCopyEventsBetweenRecurringSeries() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    // Create recurring series
    multiCtrl.processCommand("create event Standup from 2024-09-02T09:00 "
        + "to 2024-09-02T09:30 repeats MTWRF for 20 times");

    // Copy partial series (Sept 5-12)
    multiCtrl.processCommand("copy events between 2024-09-05 and 2"
        + "024-09-12 --target Target to 2025-01-08");

    assertTrue(view.getErrors().isEmpty());
    assertTrue(multiModel.getCalendar("Target").getAllEvents().size() > 0);
    assertTrue(multiModel.getCalendar("Target").getAllEvents().size() < 20); // Should be partial
  }

  @Test
  public void testCopyEventsBetweenSameCalendar() {
    multiCtrl.processCommand("create calendar --name MyCalendar --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name MyCalendar");

    multiCtrl.processCommand("create event Original from 2024-09-05T10:00 to 2024-09-05T11:00");

    // Copy within same calendar
    multiCtrl.processCommand("copy events between 2024-09-05 "
        + "and 2024-09-05 --target MyCalendar to 2025-01-08");

    assertTrue(view.getErrors().isEmpty());
    // Should have both original and copy
    assertEquals(2, multiModel.getCalendar("MyCalendar").getAllEvents().size());
  }

  @Test
  public void testCopyEventsBetweenLargeRange() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    multiCtrl.processCommand("create event Week1 from 2024-09-05T10:00 to 2024-09-05T11:00");
    multiCtrl.processCommand("create event Week2 from 2024-09-12T10:00 to 2024-09-12T11:00");
    multiCtrl.processCommand("create event Week3 from 2024-09-19T10:00 to 2024-09-19T11:00");
    multiCtrl.processCommand("create event Week4 from 2024-09-26T10:00 to 2024-09-26T11:00");

    multiCtrl.processCommand("copy events between 2024-09-01 and "
        + "2024-09-30 --target Target to 2025-01-01");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(4, multiModel.getCalendar("Target").getAllEvents().size());
  }

  @Test
  public void testCopyEventsBetweenEventsOutsideRange() {
    multiCtrl.processCommand("create calendar --name Source --timezone America/New_York");
    multiCtrl.processCommand("create calendar --name Target --timezone America/New_York");
    multiCtrl.processCommand("use calendar --name Source");

    multiCtrl.processCommand("create event Before from 2024-09-04T10:00 to 2024-09-04T11:00");
    multiCtrl.processCommand("create event Inside from 2024-09-05T10:00 to 2024-09-05T11:00");
    multiCtrl.processCommand("create event After from 2024-09-11T10:00 to 2024-09-11T11:00");

    multiCtrl.processCommand("copy events between 2024-09-05 and 2024-09-10 "
        + "--target Target to 2025-01-08");

    assertTrue(view.getErrors().isEmpty());
    assertEquals(1, multiModel.getCalendar("Target").getAllEvents().size());
    assertEquals("Inside", multiModel.getCalendar("Target").getAllEvents().get(0).getSubject());
  }

  @Test
  public void testCreateCalendarFailsInSingleCalendarMode() {
    controller.processCommand("create calendar --name testCal --timezone UTC");
    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0)
        .contains("requires a multi calendar controller"));
  }

  @Test
  public void testEditCalendarFailsInSingleCalendarMode() {
    controller.processCommand("edit calendar --name test --property name newName");
    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0)
        .contains("requires a multi calendar controller"));
  }

  @Test
  public void testUseCalendarFailsInSingleCalendarMode() {
    controller.processCommand("use calendar --name test");
    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0)
        .contains("requires a multi calendar controller"));
  }

  @Test
  public void testCopyEventFailsInSingleCalendarMode() {
    controller.processCommand("copy event Meeting on 2025-05-01T10:00"
        + " --target Target to 2025-05-02T10:00");
    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0)
        .contains("requires a multi calendar controller"));
  }

  @Test
  public void testMultiCtrlThrowsWhenNoCalendarInUse() {
    multiCtrl.processCommand("create calendar --name work --timezone UTC");
    // Forget to use the calendar before adding event
    multiCtrl.processCommand("create event Meeting from 2025-01-01T10:00 to 2025-01-01T11:00");
    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("No calendar in use"));
  }

  @Test
  public void testCreateAllDayEventShowsSuccessMessage() {
    view.reset();
    controller.processCommand("create event Holiday on 2025-05-01");
    assertTrue(view.getMessages().get(0).toLowerCase().contains("successfully"));
  }

  @Test
  public void testEditEventShowsSuccessMessage() {
    controller.processCommand("create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00");
    view.reset();
    controller.processCommand("edit event location Meeting from 2025-05-01T10:00 with RoomX");
    assertTrue(view.getMessages().get(0).toLowerCase().contains("updated"));
  }

  @Test
  public void testEditSeriesShowsSuccessMessage() {
    controller.processCommand("create event Standup from 2025-05-05T09:00 "
        + "to 2025-05-05T09:30 repeats MW for 4 times");
    view.reset();
    controller.processCommand("edit series subject Standup from 2025-05-05T09:00 with Renamed");
    assertTrue(view.getMessages().get(0).toLowerCase().contains("series updated successfully"));
  }

  @Test
  public void testCopyEventShowsSuccessMessage() {
    multiCtrl.processCommand("create calendar --name src --timezone UTC");
    multiCtrl.processCommand("create calendar --name trg --timezone UTC");
    multiCtrl.processCommand("use calendar --name src");
    multiCtrl.processCommand("create event Meeting from 2025-01-01T10:00 to 2025-01-01T11:00");
    view.reset();
    multiCtrl.processCommand("copy event Meeting on 2025-01-01T10:00 --target trg to"
        + " 2025-01-02T10:00");
    assertTrue(view.getMessages().get(0).toLowerCase().contains("successfully"));
  }

  @Test
  public void testCopyEventsOnShowsSuccessMessage() {
    multiCtrl.processCommand("create calendar --name src --timezone UTC");
    multiCtrl.processCommand("create calendar --name trg --timezone UTC");
    multiCtrl.processCommand("use calendar --name src");
    multiCtrl.processCommand("create event Meeting from 2025-01-01T10:00 to 2025-01-01T11:00");
    view.reset();
    multiCtrl.processCommand("copy events on 2025-01-01 --target trg to 2025-01-03");
    assertTrue(view.getMessages().get(0).toLowerCase().contains("successfully"));
  }

  @Test
  public void testCopyEventsBetweenShowsSuccessMessage() {
    multiCtrl.processCommand("create calendar --name src --timezone UTC");
    multiCtrl.processCommand("create calendar --name trg --timezone UTC");
    multiCtrl.processCommand("use calendar --name src");
    multiCtrl.processCommand("create event Meeting from 2025-01-01T10:00 to 2025-01-01T11:00");
    view.reset();
    multiCtrl.processCommand("copy events between 2025-01-01 "
        + "and 2025-01-02 --target trg to 2025-02-01");
    assertTrue(view.getMessages().get(0).toLowerCase().contains("successfully"));
  }

  @Test
  public void testRenamingActiveCalendarKeepsItActive() {
    multiCtrl.processCommand("create calendar --name oldCal --timezone UTC");
    multiCtrl.processCommand("use calendar --name oldCal");
    multiCtrl.processCommand("edit calendar --name oldCal --property name newCal");
    multiCtrl.processCommand("create event Meeting from 2025-01-01T10:00 to 2025-01-01T11:00");
    assertEquals(1, multiModel.getCalendar("newCal").getAllEvents().size());
  }

  @Test
  public void testRepeatingEventWithZeroOccurrences() {
    controller.processCommand("create event Meeting from 2025-05-01T10:00 "
        + "to 2025-05-01T11:00 repeats M for 0 times");
    // Should produce an error or no events
    assertTrue(view.getErrors().size() > 0 || model.getAllEvents().isEmpty());
  }

  @Test
  public void testAllDayRepeatingEventWithZeroOccurrences() {
    controller.processCommand("create event Holiday on 2025-05-01 repeats F for 0 times");
    assertTrue(view.getErrors().size() > 0 || model.getAllEvents().isEmpty());
  }

  @Test
  public void testUseCalendarMissingNameFlag() {
    multiCtrl.processCommand("use calendar Work");

    assertEquals(1, view.getErrors().size());
    assertTrue(view.getErrors().get(0).contains("Invalid"));
  }

  @Test
  public void testEditEventSubjectWithQuotes() {
    controller.processCommand(
        "create event \"Stand Up\" from 2025-05-01T10:00 to 2025-05-01T11:00"
    );

    controller.processCommand(
        "edit event subject \"Stand Up\" from 2025-05-01T10:00 to 2025-05-01T11:00 with NewName"
    );

    assertEquals("NewName", model.getAllEvents().get(0).getSubject());
  }

  @Test
  public void testEditEventLocationQuotedSubject() {
    controller.processCommand(
        "create event \"Lunch Break\" from 2025-05-01T12:00 to 2025-05-01T13:00"
    );

    controller.processCommand(
        "edit event location \"Lunch Break\" from 2025-05-01T12:00 to 2025-05-01T13:00 with Cafe"
    );

    assertEquals("Cafe", model.getAllEvents().get(0).getLocation());
  }

  @Test
  public void testEditEventInvalidFormat() {
    controller.processCommand(
        "create event Meeting from 2025-05-01T10:00 to 2025-05-01T11:00"
    );

    controller.processCommand("edit event Meeting wrongcommand");

    assertEquals(1, view.getErrors().size());
  }

}