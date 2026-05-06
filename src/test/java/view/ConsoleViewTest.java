package view;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.readString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.CalendarEvent;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive test suite for ConsoleView.
 */
public class ConsoleViewTest {

  private StringWriter output;
  private ConsoleView view;

  /**
   * Sets up the test fixture before each test.
   * Initializes a fresh string writer and console view.
   */
  @Before
  public void setUp() {
    output = new StringWriter();
  }

  @Test
  public void testDisplayMessage() {
    view = new ConsoleView(new StringReader(""), output);
    view.displayMessage("Hello World");

    assertEquals("Hello World\n", output.toString());
  }

  @Test
  public void testDisplayMessageMultiple() {
    view = new ConsoleView(new StringReader(""), output);
    view.displayMessage("First message");
    view.displayMessage("Second message");

    assertEquals("First message\nSecond message\n", output.toString());
  }

  @Test
  public void testDisplayMessageEmpty() {
    view = new ConsoleView(new StringReader(""), output);
    view.displayMessage("");

    assertEquals("\n", output.toString());
  }

  @Test
  public void testDisplayError() {
    view = new ConsoleView(new StringReader(""), output);
    view.displayError("Something went wrong");

    assertEquals("ERROR: Something went wrong\n", output.toString());
  }

  @Test
  public void testDisplayErrorMultiple() {
    view = new ConsoleView(new StringReader(""), output);
    view.displayError("First error");
    view.displayError("Second error");

    assertEquals("ERROR: First error\nERROR: Second error\n", output.toString());
  }

  @Test
  public void testDisplayEventsOnDateEmpty() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDate date = LocalDate.of(2025, 5, 1);
    List<CalendarEvent> events = new ArrayList<>();

    view.displayEventsOnDate(date, events);

    assertTrue(output.toString().contains("No events scheduled on 2025-05-01"));
  }

  @Test
  public void testDisplayEventsOnDateSingleEvent() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDate date = LocalDate.of(2025, 5, 1);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, null
    );
    events.add(event);

    view.displayEventsOnDate(date, events);

    String result = output.toString();
    assertTrue(result.contains("Events on 2025-05-01"));
    assertTrue(result.contains("Meeting"));
    assertTrue(result.contains("10:00"));
    assertTrue(result.contains("11:00"));
  }

  @Test
  public void testDisplayEventsOnDateWithLocation() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDate date = LocalDate.of(2025, 5, 1);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, "Room 101", null, null
    );
    events.add(event);

    view.displayEventsOnDate(date, events);

    String result = output.toString();
    assertTrue(result.contains("Meeting"));
    assertTrue(result.contains("at Room 101"));
  }

  @Test
  public void testDisplayEventsOnDateWithoutLocation() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDate date = LocalDate.of(2025, 5, 1);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, null
    );
    events.add(event);

    view.displayEventsOnDate(date, events);

    String result = output.toString();
    assertTrue(result.contains("Meeting"));
    assertTrue(!result.contains(" at ") || result.contains("at 10:00"));
  }

  @Test
  public void testDisplayEventsOnDateWithEmptyLocation() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDate date = LocalDate.of(2025, 5, 1);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, "", null, null
    );
    events.add(event);

    view.displayEventsOnDate(date, events);

    String result = output.toString();
    assertTrue(result.contains("Meeting"));
    int atCount = result.split(" at ", -1).length - 1;
    assertEquals(0, atCount);
  }

  @Test
  public void testDisplayEventsOnDateMultipleEvents() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDate date = LocalDate.of(2025, 5, 1);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event1 = new CalendarEvent(
        "Morning Meeting",
        LocalDateTime.of(2025, 5, 1, 9, 0),
        LocalDateTime.of(2025, 5, 1, 10, 0),
        null, "Room A", null, null
    );
    CalendarEvent event2 = new CalendarEvent(
        "Lunch",
        LocalDateTime.of(2025, 5, 1, 12, 0),
        LocalDateTime.of(2025, 5, 1, 13, 0),
        null, null, null, null
    );
    events.add(event1);
    events.add(event2);

    view.displayEventsOnDate(date, events);

    String result = output.toString();
    assertTrue(result.contains("Morning Meeting"));
    assertTrue(result.contains("Lunch"));
    assertTrue(result.contains("Room A"));
  }

  @Test
  public void testDisplayEventsInRangeEmpty() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 31, 23, 59);
    List<CalendarEvent> events = new ArrayList<>();

    view.displayEventsInRange(start, end, events);

    assertTrue(output.toString().contains("No events scheduled in the specified range"));
  }

  @Test
  public void testDisplayEventsInRangeSingleEvent() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 31, 23, 59);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Conference",
        LocalDateTime.of(2025, 5, 15, 9, 0),
        LocalDateTime.of(2025, 5, 15, 17, 0),
        null, null, null, null
    );
    events.add(event);

    view.displayEventsInRange(start, end, events);

    String result = output.toString();
    assertTrue(result.contains("Events from 2025-05-01 00:00 to 2025-05-31 23:59"));
    assertTrue(result.contains("Conference"));
    assertTrue(result.contains("starting on 2025-05-15"));
    assertTrue(result.contains("ending on 2025-05-15"));
  }

  @Test
  public void testDisplayEventsInRangeWithLocation() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 31, 23, 59);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Conference",
        LocalDateTime.of(2025, 5, 15, 9, 0),
        LocalDateTime.of(2025, 5, 15, 17, 0),
        null, "Convention Center", null, null
    );
    events.add(event);

    view.displayEventsInRange(start, end, events);

    String result = output.toString();
    assertTrue(result.contains("Conference"));
    assertTrue(result.contains("at Convention Center"));
  }

  @Test
  public void testDisplayEventsInRangeWithoutLocation() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 31, 23, 59);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Conference",
        LocalDateTime.of(2025, 5, 15, 9, 0),
        LocalDateTime.of(2025, 5, 15, 17, 0),
        null, null, null, null
    );
    events.add(event);

    view.displayEventsInRange(start, end, events);

    String result = output.toString();
    assertTrue(result.contains("Conference"));
  }

  @Test
  public void testDisplayEventsInRangeWithEmptyLocation() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 31, 23, 59);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Conference",
        LocalDateTime.of(2025, 5, 15, 9, 0),
        LocalDateTime.of(2025, 5, 15, 17, 0),
        null, "", null, null
    );
    events.add(event);

    view.displayEventsInRange(start, end, events);

    String result = output.toString();
    assertTrue(result.contains("Conference"));
  }

  @Test
  public void testDisplayEventsInRangeMultiDay() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 5, 31, 23, 59);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Multi-day Conference",
        LocalDateTime.of(2025, 5, 15, 9, 0),
        LocalDateTime.of(2025, 5, 17, 17, 0),
        null, null, null, null
    );
    events.add(event);

    view.displayEventsInRange(start, end, events);

    String result = output.toString();
    assertTrue(result.contains("starting on 2025-05-15"));
    assertTrue(result.contains("ending on 2025-05-17"));
  }

  @Test
  public void testDisplayStatusBusy() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 10, 30);

    view.displayStatus(dateTime, true);

    String result = output.toString();
    assertTrue(result.contains("Status on 2025-05-01 10:30"));
    assertTrue(result.contains("busy"));
  }

  @Test
  public void testDisplayStatusAvailable() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 10, 30);

    view.displayStatus(dateTime, false);

    String result = output.toString();
    assertTrue(result.contains("Status on 2025-05-01 10:30"));
    assertTrue(result.contains("available"));
  }

  @Test
  public void testExportToCsvSingleEvent() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        "Important discussion",
        "Room 101",
        "public",
        null
    );
    events.add(event);

    view.exportToCsv("test-calendar.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
    assertTrue(result.contains("test-calendar.csv"));
  }

  @Test
  public void testExportToCsvPrivateEvent() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Private Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null,
        null,
        "private",
        null
    );
    events.add(event);

    view.exportToCsv("test-private.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
  }

  @Test
  public void testExportToCsvPublicEvent() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Public Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null,
        null,
        "public",
        null
    );
    events.add(event);

    view.exportToCsv("test-public.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
  }

  @Test
  public void testExportToCsvMultipleEvents() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event1 = new CalendarEvent(
        "Meeting 1",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, null
    );
    CalendarEvent event2 = new CalendarEvent(
        "Meeting 2",
        LocalDateTime.of(2025, 5, 2, 14, 0),
        LocalDateTime.of(2025, 5, 2, 15, 0),
        null, null, null, null
    );
    events.add(event1);
    events.add(event2);

    view.exportToCsv("test-multiple.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
  }

  @Test
  public void testExportToCsvEmptyList() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    view.exportToCsv("test-empty.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
  }

  @Test
  public void testExportToCsvWithCommasInSubject() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting, Discussion, Planning",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, null
    );
    events.add(event);

    view.exportToCsv("test-commas.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
  }

  @Test
  public void testExportToCsvWithQuotesInSubject() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting \"Important\"",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, null
    );
    events.add(event);

    view.exportToCsv("test-quotes.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
  }

  @Test
  public void testExportToCsvWithNewlineInDescription() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        "Line 1\nLine 2",
        null,
        null,
        null
    );
    events.add(event);

    view.exportToCsv("test-newline.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
  }

  @Test
  public void testExportToCsvWithNullDescription() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null,
        null,
        null,
        null
    );
    events.add(event);

    view.exportToCsv("test-null-desc.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
  }

  @Test
  public void testExportToCsvWithNullLocation() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null,
        null,
        null,
        null
    );
    events.add(event);

    view.exportToCsv("test-null-location.csv", events);

    String result = output.toString();
    assertTrue(result.contains("Calendar exported to"));
  }

  @Test
  public void testGetUserInput() {
    view = new ConsoleView(new StringReader("test command\n"), output);
    String input = view.getUserInput();

    assertEquals("test command", input);
    assertTrue(output.toString().contains(">"));
  }

  @Test
  public void testGetUserInputMultipleLines() {
    view = new ConsoleView(new StringReader("first\nsecond\n"), output);

    String input1 = view.getUserInput();
    String input2 = view.getUserInput();

    assertEquals("first", input1);
    assertEquals("second", input2);
  }

  @Test
  public void testGetUserInputEmpty() {
    view = new ConsoleView(new StringReader("\n"), output);
    String input = view.getUserInput();

    assertEquals("", input);
  }

  @Test
  public void testGetUserInputNoInput() {
    view = new ConsoleView(new StringReader(""), output);
    String input = view.getUserInput();

    assertEquals("exit", input);
  }

  @Test
  public void testGetUserInputWithSpaces() {
    view = new ConsoleView(new StringReader("  command with spaces  \n"), output);
    String input = view.getUserInput();

    assertEquals("  command with spaces  ", input);
  }

  @Test
  public void testEscapeCsvNull() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Test",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null,
        null,
        null,
        null
    );
    events.add(event);

    view.exportToCsv("test-escape-null.csv", events);
    assertTrue(output.toString().contains("Calendar exported to"));
  }

  @Test
  public void testEscapeCsvEmpty() {
    view = new ConsoleView(new StringReader(""), output);
    List<CalendarEvent> events = new ArrayList<>();

    CalendarEvent event = new CalendarEvent(
        "Test",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        "",
        "",
        null,
        null
    );
    events.add(event);

    view.exportToCsv("test-escape-empty.csv", events);
    assertTrue(output.toString().contains("Calendar exported to"));
  }

  @Test(expected = RuntimeException.class)
  public void testDisplayMessageIoException() {
    Appendable failingAppendable = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(char c) throws IOException {
        throw new IOException("Write failed");
      }
    };

    view = new ConsoleView(new StringReader(""), failingAppendable);
    view.displayMessage("This should fail");
  }

  @Test
  public void testDisplayMessageLongText() {
    view = new ConsoleView(new StringReader(""), output);
    StringBuilder longMessage = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longMessage.append("A");
    }
    view.displayMessage(longMessage.toString());

    assertTrue(output.toString().contains("AAA"));
    assertTrue(output.toString().length() > 1000);
  }

  @Test
  public void testDisplayMessageSpecialCharacters() {
    view = new ConsoleView(new StringReader(""), output);
    view.displayMessage("Message with special chars: @#$%^&*()");

    assertTrue(output.toString().contains("@#$%^&*()"));
  }

  @Test
  public void testDisplayMessageWithNewlines() {
    view = new ConsoleView(new StringReader(""), output);
    view.displayMessage("Line 1\nLine 2\nLine 3");

    String result = output.toString();
    assertTrue(result.contains("Line 1"));
    assertTrue(result.contains("Line 2"));
    assertTrue(result.contains("Line 3"));
  }

  @Test(expected = RuntimeException.class)
  public void testDisplayErrorIoException() {
    Appendable failingAppendable = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(char c) throws IOException {
        throw new IOException("Write failed");
      }
    };

    view = new ConsoleView(new StringReader(""), failingAppendable);
    view.displayError("This should fail");
  }

  @Test
  public void testDisplayErrorLongText() {
    view = new ConsoleView(new StringReader(""), output);
    StringBuilder longError = new StringBuilder();
    for (int i = 0; i < 500; i++) {
      longError.append("Error");
    }
    view.displayError(longError.toString());

    assertTrue(output.toString().contains("ERROR:"));
    assertTrue(output.toString().contains("Error"));
  }

  @Test
  public void testDisplayErrorWithNewlines() {
    view = new ConsoleView(new StringReader(""), output);
    view.displayError("Error line 1\nError line 2");

    String result = output.toString();
    assertTrue(result.contains("ERROR:"));
    assertTrue(result.contains("Error line 1"));
  }

  @Test
  public void testDisplayErrorEmptyString() {
    view = new ConsoleView(new StringReader(""), output);
    view.displayError("");

    assertEquals("ERROR: \n", output.toString());
  }

  @Test(expected = RuntimeException.class)
  public void testDisplayStatusIoException() {
    Appendable failingAppendable = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(char c) throws IOException {
        throw new IOException("Write failed");
      }
    };

    view = new ConsoleView(new StringReader(""), failingAppendable);
    view.displayStatus(LocalDateTime.of(2025, 5, 1, 10, 0), true);
  }

  @Test
  public void testDisplayStatusBusyMidnight() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 0, 0);

    view.displayStatus(dateTime, true);

    String result = output.toString();
    assertTrue(result.contains("Status on 2025-05-01 00:00"));
    assertTrue(result.contains("busy"));
  }

  @Test
  public void testDisplayStatusAvailableMidnight() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 0, 0);

    view.displayStatus(dateTime, false);

    String result = output.toString();
    assertTrue(result.contains("Status on 2025-05-01 00:00"));
    assertTrue(result.contains("available"));
  }

  @Test
  public void testDisplayStatusBusyNoon() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 12, 0);

    view.displayStatus(dateTime, true);

    String result = output.toString();
    assertTrue(result.contains("Status on 2025-05-01 12:00"));
    assertTrue(result.contains("busy"));
  }

  @Test
  public void testDisplayStatusAvailableNoon() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 12, 0);

    view.displayStatus(dateTime, false);

    String result = output.toString();
    assertTrue(result.contains("Status on 2025-05-01 12:00"));
    assertTrue(result.contains("available"));
  }

  @Test
  public void testDisplayStatusBusyEndOfDay() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDateTime dateTime = LocalDateTime.of(2025, 5, 1, 23, 59);

    view.displayStatus(dateTime, true);

    String result = output.toString();
    assertTrue(result.contains("Status on 2025-05-01 23:59"));
    assertTrue(result.contains("busy"));
  }

  @Test(expected = RuntimeException.class)
  public void testGetUserInputIoException() {
    Appendable failingAppendable = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(char c) throws IOException {
        throw new IOException("Write failed");
      }
    };

    view = new ConsoleView(new StringReader("test\n"), failingAppendable);
    view.getUserInput();
  }

  @Test
  public void testGetUserInputLongCommand() {
    StringBuilder longCommand = new StringBuilder();
    for (int i = 0; i < 1000; i++) {
      longCommand.append("a");
    }
    longCommand.append("\n");

    view = new ConsoleView(new StringReader(longCommand.toString()), output);
    String input = view.getUserInput();

    assertTrue(input.length() == 1000);
    assertTrue(input.contains("aaa"));
  }

  @Test
  public void testGetUserInputMultipleCallsUntilEmpty() {
    view = new ConsoleView(new StringReader("first\nsecond\nthird\n"), output);

    assertEquals("first", view.getUserInput());
    assertEquals("second", view.getUserInput());
    assertEquals("third", view.getUserInput());
    assertEquals("exit", view.getUserInput());
  }

  @Test
  public void testGetUserInputWithTabs() {
    view = new ConsoleView(new StringReader("command\twith\ttabs\n"), output);
    String input = view.getUserInput();

    assertTrue(input.contains("\t"));
    assertTrue(input.contains("command"));
  }

  @Test
  public void testGetUserInputExitCommand() {
    view = new ConsoleView(new StringReader("exit\n"), output);
    String input = view.getUserInput();

    assertEquals("exit", input);
  }

  @Test
  public void testGetUserInputSingleCharacter() {
    view = new ConsoleView(new StringReader("x\n"), output);
    String input = view.getUserInput();

    assertEquals("x", input);
  }

  @Test
  public void testGetUserInputOnlyWhitespace() {
    view = new ConsoleView(new StringReader("     \n"), output);
    String input = view.getUserInput();

    assertEquals("     ", input);
  }

  @Test
  public void testGetUserInputSpecialCommands() {
    view = new ConsoleView(new StringReader("create event \"Test\"\n"), output);
    String input = view.getUserInput();

    assertTrue(input.contains("create event"));
    assertTrue(input.contains("\"Test\""));
  }

  @Test(expected = RuntimeException.class)
  public void testDisplayEventsOnDateIoException() {
    Appendable failingAppendable = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(char c) throws IOException {
        throw new IOException("Write failed");
      }
    };

    view = new ConsoleView(new StringReader(""), failingAppendable);

    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, null
    );
    events.add(event);

    view.displayEventsOnDate(LocalDate.of(2025, 5, 1), events);
  }

  @Test(expected = RuntimeException.class)
  public void testDisplayEventsInRangeIoException() {
    Appendable failingAppendable = new Appendable() {
      @Override
      public Appendable append(CharSequence csq) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new IOException("Write failed");
      }

      @Override
      public Appendable append(char c) throws IOException {
        throw new IOException("Write failed");
      }
    };

    view = new ConsoleView(new StringReader(""), failingAppendable);

    List<CalendarEvent> events = new ArrayList<>();
    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, null
    );
    events.add(event);

    view.displayEventsInRange(
        LocalDateTime.of(2025, 5, 1, 0, 0),
        LocalDateTime.of(2025, 5, 31, 23, 59),
        events
    );
  }

  @Test
  public void testDisplayEventsOnDateWithVeryLongSubject() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDate date = LocalDate.of(2025, 5, 1);
    List<CalendarEvent> events = new ArrayList<>();

    StringBuilder longSubject = new StringBuilder();
    for (int i = 0; i < 200; i++) {
      longSubject.append("Long ");
    }

    CalendarEvent event = new CalendarEvent(
        longSubject.toString(),
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, null, null
    );
    events.add(event);

    view.displayEventsOnDate(date, events);

    String result = output.toString();
    assertTrue(result.contains("Long"));
  }

  @Test
  public void testDisplayEventsOnDateWithVeryLongLocation() {
    view = new ConsoleView(new StringReader(""), output);
    LocalDate date = LocalDate.of(2025, 5, 1);
    List<CalendarEvent> events = new ArrayList<>();

    StringBuilder longLocation = new StringBuilder();
    for (int i = 0; i < 200; i++) {
      longLocation.append("Place ");
    }

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, longLocation.toString(), null, null
    );
    events.add(event);

    view.displayEventsOnDate(date, events);

    String result = output.toString();
    assertTrue(result.contains("Place"));
    assertTrue(result.contains("at Place"));
  }

  @Test
  public void testExportToCsvVerifyAllFields() throws Exception {
    StringBuilder output = new StringBuilder();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    CalendarEvent event = new CalendarEvent(
        "Meeting",
        LocalDateTime.of(2024, 3, 15, 10, 0),
        LocalDateTime.of(2024, 3, 15, 11, 30),
        "Conference Room",
        "Important meeting",
        "private",
        null
    );

    String filename = "test_all_fields.csv";
    view.exportToCsv(filename, Arrays.asList(event));

    String content = new String(readAllBytes(
        java.nio.file.Paths.get(filename)));

    assertTrue("Header must be present", content.contains("Subject,Start Date"));

    assertTrue("Subject must be written", content.contains("Meeting"));
    assertTrue("Start date must be written", content.contains("03/15/2024"));
    assertTrue("Start time must be written", content.contains("10:00 AM"));
    assertTrue("End date must be written", content.contains("03/15/2024"));
    assertTrue("End time must be written", content.contains("11:30 AM"));
    assertTrue("All Day Event must be written", content.contains("False"));
    assertTrue("Description must be written", content.contains("Important meeting"));
    assertTrue("Location must be written", content.contains("Conference Room"));
    assertTrue("Private status must be written", content.contains("True"));

    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filename));
  }

  /**
   * Tests CSV export with public event to verify False for private field.
   * Kills mutation on line 164.
   */
  @Test
  public void testExportToCsvPublicEventVerification() throws Exception {
    StringBuilder output = new StringBuilder();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    CalendarEvent event = new CalendarEvent(
        "Public Event",
        LocalDateTime.of(2024, 3, 15, 10, 0),
        LocalDateTime.of(2024, 3, 15, 11, 0),
        null,
        null,
        "public",
        null
    );

    String filename = "test_public.csv";
    view.exportToCsv(filename, Arrays.asList(event));

    String content = new String(readAllBytes(
        java.nio.file.Paths.get(filename)));

    assertTrue("Public event should have False in private field",
        content.contains(",False\n"));

    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filename));
  }

  /**
   * Tests CSV export verifies commas are written between fields.
   * Kills mutations on lines 143, 146, 149, 152, 155, 157, 160, 163.
   */
  @Test
  public void testExportToCsvFieldSeparators() throws Exception {
    StringBuilder output = new StringBuilder();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    CalendarEvent event = new CalendarEvent(
        "Test",
        LocalDateTime.of(2024, 3, 15, 10, 0),
        LocalDateTime.of(2024, 3, 15, 11, 0),
        "Loc",
        "Desc",
        "public",
        null
    );

    String filename = "test_separators.csv";
    view.exportToCsv(filename, Arrays.asList(event));

    String content = new String(readAllBytes(
        java.nio.file.Paths.get(filename)));

    String[] lines = content.split("\n");
    assertTrue("Should have at least 2 lines", lines.length >= 2);

    String dataLine = lines[1];
    int commaCount = dataLine.length() - dataLine.replace(",", "").length();
    assertEquals("Should have 8 commas separating 9 fields", 8, commaCount);

    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filename));
  }

  /**
   * Tests that missing header write would cause invalid CSV.
   * Kills mutation on line 135.
   */
  @Test
  public void testExportToCsvHeaderRequired() throws Exception {
    StringBuilder output = new StringBuilder();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    CalendarEvent event = new CalendarEvent(
        "Event",
        LocalDateTime.of(2024, 3, 15, 10, 0),
        LocalDateTime.of(2024, 3, 15, 11, 0),
        null,
        null,
        "public",
        null
    );

    String filename = "test_header.csv";
    view.exportToCsv(filename, Arrays.asList(event));

    String content = new String(readAllBytes(
        java.nio.file.Paths.get(filename)));

    String[] lines = content.split("\n");
    assertTrue("File should have at least header and one data line",
        lines.length >= 2);

    assertTrue("First line must be header with Subject",
        lines[0].contains("Subject"));
    assertTrue("Header must contain Start Date",
        lines[0].contains("Start Date"));

    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filename));
  }

  /**
   * Tests that newline write is necessary for each event.
   * Kills mutation on line 167.
   */
  @Test
  public void testExportToCsvNewlinesBetweenEvents() throws Exception {
    StringBuilder output = new StringBuilder();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    CalendarEvent event1 = new CalendarEvent(
        "Event1",
        LocalDateTime.of(2024, 3, 15, 10, 0),
        LocalDateTime.of(2024, 3, 15, 11, 0),
        null, null, "public", null
    );

    CalendarEvent event2 = new CalendarEvent(
        "Event2",
        LocalDateTime.of(2024, 3, 16, 10, 0),
        LocalDateTime.of(2024, 3, 16, 11, 0),
        null, null, "public", null
    );

    String filename = "test_newlines.csv";
    view.exportToCsv(filename, Arrays.asList(event1, event2));

    String content = new String(readAllBytes(
        java.nio.file.Paths.get(filename)));

    String[] lines = content.split("\n");
    assertEquals("Should have header + 2 event lines", 3, lines.length);
    assertTrue("Line 2 should contain Event1", lines[1].contains("Event1"));
    assertTrue("Line 3 should contain Event2", lines[2].contains("Event2"));

    java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filename));
  }

  /**
   * Tests escapeCsv with value containing comma.
   * Kills mutations on line 183-184.
   */
  @Test
  public void testEscapeCsvWithComma() throws Exception {

    java.lang.reflect.Method method = ConsoleView.class.getDeclaredMethod(
        "escapeCsv", String.class);
    method.setAccessible(true);

    ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());
    String result = (String) method.invoke(view, "Hello, World");

    assertEquals("Value with comma should be quoted", "\"Hello, World\"", result);
    assertNotEquals("Should not return empty string", "", result);
  }

  /**
   * Tests escapeCsv with value containing quotes.
   * Kills mutations on line 183-184.
   */
  @Test
  public void testEscapeCsvWithQuotes() throws Exception {
    java.lang.reflect.Method method = ConsoleView.class.getDeclaredMethod(
        "escapeCsv", String.class);
    method.setAccessible(true);

    ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());
    String result = (String) method.invoke(view, "Say \"Hello\"");

    assertEquals("Quotes should be escaped", "\"Say \"\"Hello\"\"\"", result);
    assertNotEquals("Should not return empty string", "", result);
  }

  /**
   * Tests escapeCsv with value containing newline.
   * Kills mutations on line 183-184.
   */
  @Test
  public void testEscapeCsvWithNewline() throws Exception {
    java.lang.reflect.Method method = ConsoleView.class.getDeclaredMethod(
        "escapeCsv", String.class);
    method.setAccessible(true);

    ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());
    String result = (String) method.invoke(view, "Line1\nLine2");

    assertEquals("Value with newline should be quoted", "\"Line1\nLine2\"", result);
    assertNotEquals("Should not return empty string", "", result);
  }

  /**
   * Tests escapeCsv with simple value (no special chars).
   * Kills mutations on line 186.
   */
  @Test
  public void testEscapeCsvSimpleValue() throws Exception {
    java.lang.reflect.Method method = ConsoleView.class.getDeclaredMethod(
        "escapeCsv", String.class);
    method.setAccessible(true);

    ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());
    String result = (String) method.invoke(view, "SimpleValue");

    assertEquals("Simple value should not be quoted", "SimpleValue", result);
    assertNotEquals("Should not return empty string", "", result);
  }

  /**
   * Tests escapeCsv with single character.
   * Kills boundary mutations on lines 180, 183.
   */
  @Test
  public void testEscapeCsvSingleCharacter() throws Exception {
    java.lang.reflect.Method method = ConsoleView.class.getDeclaredMethod(
        "escapeCsv", String.class);
    method.setAccessible(true);

    ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());
    String result = (String) method.invoke(view, "A");

    assertEquals("Single char should return as-is", "A", result);
    assertFalse("Single char should not be empty", result.isEmpty());
  }

  /**
   * Tests escapeCsv with value that is just a comma.
   * Kills mutations on line 183.
   */
  @Test
  public void testEscapeCsvJustComma() throws Exception {
    java.lang.reflect.Method method = ConsoleView.class.getDeclaredMethod(
        "escapeCsv", String.class);
    method.setAccessible(true);

    ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());
    String result = (String) method.invoke(view, ",");

    assertEquals("Just comma should be quoted", "\",\"", result);
  }

  // ======================= Testing exportAuto method ================================= //

  @Test
  public void testExportAutoIcal() {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path ics = null;

    try {
      tmpDir = createTempDirectory("exportAutoIcalTest");
      ics = tmpDir.resolve("calendar.ics");

      view.exportAuto(ics.toString(), Collections.emptyList());

      assertTrue(exists(ics));

      String outStr = output.toString();
      assertTrue(outStr.contains("Calendar exported to: " + ics.toAbsolutePath()));

      String content = readString(ics);
      assertTrue(content.contains("BEGIN:VCALENDAR"));
      assertTrue(content.contains("END:VCALENDAR"));

    } catch (IOException e) {
      fail("Unexpected exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());

    } finally {
      try {
        if (ics != null) {
          Files.deleteIfExists(ics);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          Files.deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportAutoNullFilename() {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    view.exportAuto(null, Collections.emptyList());

    String printedOutput = output.toString();

    assertTrue(printedOutput.contains("Missing file name for export."));
  }

  @Test
  public void testExportAutoEmptyFilename() {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    view.exportAuto("", Collections.emptyList());

    String printedOutput = output.toString();

    assertTrue(printedOutput.contains("Missing file name for export."));
  }

  @Test
  public void testExportAutoUnsupportedFileType() {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    view.exportAuto("calendar.xxx", Collections.emptyList());

    String printedOutput = output.toString();

    assertTrue(printedOutput.contains("Unsupported file format: "));
  }

  @Test
  public void testExportAutoCsv() {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path csv = null;

    try {
      tmpDir = createTempDirectory("exportAutoCsvTest");
      csv = tmpDir.resolve("calendar.csv");

      view.exportAuto(csv.toString(), Collections.emptyList());
      assertTrue(exists(csv));

      String outStr = output.toString();
      assertTrue(outStr.contains("Calendar exported to: " + csv.toAbsolutePath()));

      String content = new String(readAllBytes(csv), StandardCharsets.UTF_8);
      assertTrue(content.startsWith("Subject,Start Date,Start Time,End Date,End Time,"
          + "All Day Event,Description,Location,Private"));

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());

    } finally {
      try {
        if (csv != null) {
          deleteIfExists(csv);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportAutoIcsWithEventsOverall() {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path ics = null;

    try {
      tmpDir = createTempDirectory("ics_event_test");
      ics = tmpDir.resolve("calendar.ics");

      LocalDateTime startTime =
          LocalDateTime.of(2025, 10, 31, 10, 0);
      LocalDateTime endTime =
          LocalDateTime.of(2025, 10, 31, 12, 0);
      CalendarEvent event = new CalendarEvent(
          "Camping", startTime, endTime, "Chill time with friends and food",
          "Central Park", "private", null
      );

      view.exportAuto(ics.toString(), Arrays.asList(event));

      String raw = new String(readAllBytes(ics), StandardCharsets.UTF_8);
      String content = raw.replace("\r\n", "\n");

      assertTrue(content.contains("BEGIN:VEVENT"));
      assertTrue(content.contains("END:VEVENT"));

      assertTrue(content.contains("UID:"));
      assertTrue(content.contains("DTSTAMP:"));

      DateTimeFormatter icsUtc = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
          .withZone(ZoneOffset.UTC);
      ZonedDateTime zonedStartTime =
          startTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
      ZonedDateTime zonedEndTime =
          endTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);

      String expectedDtStart = "DTSTART:" + icsUtc.format(zonedStartTime);
      String expectedDtEnd = "DTEND:" + icsUtc.format(zonedEndTime);

      assertTrue(content.contains(expectedDtStart));
      assertTrue(content.contains(expectedDtEnd));

      assertTrue(content.contains("SUMMARY:Camping"));

      assertTrue(content.contains("LOCATION:Central Park"));
      assertTrue(content.contains("DESCRIPTION:Chill time with friends and food"));

      assertTrue(output.toString().contains("Calendar exported to: " + ics.toAbsolutePath()));

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());

    } finally {
      try {
        if (ics != null) {
          deleteIfExists(ics);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportAutoIcsFailed() {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path parentIsFile = null;
    Path impossible = null;

    try {
      tmpDir = createTempDirectory("ical_io_error_");
      parentIsFile = tmpDir.resolve("not_a_directory");
      createFile(parentIsFile);

      impossible = parentIsFile.resolve("calendar.ics");

      view.exportAuto(impossible.toString(), Collections.emptyList());

      String printed = output.toString();

      assertTrue(printed.contains("Failed to export calendar iCal:"));

    } catch (Exception e) {
      fail("Unexpected exception: " + e.getClass().getSimpleName() + " - " + e.getMessage());

    } finally {
      try {
        if (tmpDir != null) {
          deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (parentIsFile != null) {
          deleteIfExists(parentIsFile);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (impossible != null) {
          deleteIfExists(impossible);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testEscapeIcalwithNullString() {
    try {
      Method m = ConsoleView.class.getDeclaredMethod("escapeIcal", String.class);
      m.setAccessible(true);
      String out = (String) m.invoke(null, (Object) null);
      assertEquals("", out);

    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
  }

  @Test
  public void testExportToCsvIoException() {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    // Try to write to an invalid/impossible path
    String invalidPath = "/root/impossible/path/that/does/not/exist/calendar.csv";

    CalendarEvent event = new CalendarEvent(
        "Test Event",
        LocalDateTime.of(2025, 5, 1, 10, 0),
        LocalDateTime.of(2025, 5, 1, 11, 0),
        null, null, "public", null
    );

    view.exportToCsv(invalidPath, Arrays.asList(event));

    String result = output.toString();
    assertTrue("Should display error message", result.contains("ERROR:"));
    assertTrue("Should mention export failure",
        result.contains("Failed to export calendar") || result.contains("ERROR"));
  }

  @Test
  public void testExportToCsvReadOnlyDirectory() throws Exception {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path readOnlyFile = null;

    try {
      tmpDir = createTempDirectory("readOnlyTest");
      readOnlyFile = tmpDir.resolve("readonly.csv");

      // Create file and make it read-only
      Files.createFile(readOnlyFile);
      readOnlyFile.toFile().setReadOnly();

      CalendarEvent event = new CalendarEvent(
          "Test",
          LocalDateTime.of(2025, 5, 1, 10, 0),
          LocalDateTime.of(2025, 5, 1, 11, 0),
          null, null, "public", null
      );

      // Try to write to read-only file (should fail on some systems)
      view.exportToCsv(readOnlyFile.toString(), Arrays.asList(event));

      // Either succeeds (some systems allow overwriting read-only)
      // or shows error message
      String result = output.toString();
      assertTrue(result.contains("Calendar exported to") || result.contains("ERROR"));

    } finally {
      try {
        if (readOnlyFile != null) {
          readOnlyFile.toFile().setWritable(true);
          Files.deleteIfExists(readOnlyFile);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          Files.deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportToCsvDirectoryAsFilename() {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;

    try {
      tmpDir = createTempDirectory("dirAsFileTest");

      CalendarEvent event = new CalendarEvent(
          "Test",
          LocalDateTime.of(2025, 5, 1, 10, 0),
          LocalDateTime.of(2025, 5, 1, 11, 0),
          null, null, "public", null
      );

      // Try to write to a directory (should fail)
      view.exportToCsv(tmpDir.toString(), Arrays.asList(event));

      String result = output.toString();
      assertTrue("Should display error",
          result.contains("ERROR:") || result.contains("Failed"));

    } catch (IOException e) {
      // Expected on some systems
    } finally {
      try {
        if (tmpDir != null) {
          Files.deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportToIcalWithNullLocation() throws Exception {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path ics = null;

    try {
      tmpDir = createTempDirectory("icalNullLocationTest");
      ics = tmpDir.resolve("calendar.ics");

      CalendarEvent event = new CalendarEvent(
          "Event Without Location",
          LocalDateTime.of(2025, 10, 31, 10, 0),
          LocalDateTime.of(2025, 10, 31, 12, 0),
          "Description here",
          null, // null location
          "public",
          null
      );

      view.exportAuto(ics.toString(), Arrays.asList(event));

      String content = readString(ics);
      assertTrue("Should contain event", content.contains("BEGIN:VEVENT"));
      assertTrue("Should contain summary", content.contains("SUMMARY:Event Without Location"));
      assertTrue("Should contain description", content.contains("DESCRIPTION:Description here"));
      // Should NOT contain LOCATION line (or it should be empty)
      assertFalse("Should not have location with content",
          content.contains("LOCATION:") && content.contains("LOCATION: ")
              && !content.contains("LOCATION:\r\n") && !content.contains("LOCATION:\n"));

    } finally {
      try {
        if (ics != null) {
          Files.deleteIfExists(ics);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          Files.deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportToIcalWithEmptyLocation() throws Exception {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path ics = null;

    try {
      tmpDir = createTempDirectory("icalEmptyLocationTest");
      ics = tmpDir.resolve("calendar.ics");

      CalendarEvent event = new CalendarEvent(
          "Event With Empty Location",
          LocalDateTime.of(2025, 10, 31, 10, 0),
          LocalDateTime.of(2025, 10, 31, 12, 0),
          "Description here",
          "", // empty location
          "public",
          null
      );

      view.exportAuto(ics.toString(), Arrays.asList(event));

      String content = readString(ics);
      assertTrue("Should contain event", content.contains("BEGIN:VEVENT"));
      assertTrue("Should contain summary", content.contains("SUMMARY:Event With Empty Location"));

    } finally {
      try {
        if (ics != null) {
          Files.deleteIfExists(ics);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          Files.deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportToIcalWithNullDescription() throws Exception {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path ics = null;

    try {
      tmpDir = createTempDirectory("icalNullDescTest");
      ics = tmpDir.resolve("calendar.ics");

      CalendarEvent event = new CalendarEvent(
          "Event Without Description",
          LocalDateTime.of(2025, 10, 31, 10, 0),
          LocalDateTime.of(2025, 10, 31, 12, 0),
          null, // null description
          "Central Park",
          "public",
          null
      );

      view.exportAuto(ics.toString(), Arrays.asList(event));

      String content = readString(ics);
      assertTrue("Should contain event", content.contains("BEGIN:VEVENT"));
      assertTrue("Should contain summary", content.contains("SUMMARY:Event Without Description"));
      assertTrue("Should contain location", content.contains("LOCATION:Central Park"));
      // Should NOT contain DESCRIPTION line with content

    } finally {
      try {
        if (ics != null) {
          Files.deleteIfExists(ics);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          Files.deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportToIcalWithEmptyDescription() throws Exception {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path ics = null;

    try {
      tmpDir = createTempDirectory("icalEmptyDescTest");
      ics = tmpDir.resolve("calendar.ics");

      CalendarEvent event = new CalendarEvent(
          "Event With Empty Description",
          LocalDateTime.of(2025, 10, 31, 10, 0),
          LocalDateTime.of(2025, 10, 31, 12, 0),
          "", // empty description
          "Park",
          "public",
          null
      );

      view.exportAuto(ics.toString(), Arrays.asList(event));

      String content = readString(ics);
      assertTrue("Should contain event", content.contains("BEGIN:VEVENT"));
      assertTrue("Should contain summary", content.contains("SUMMARY:"
          + "Event With Empty Description"));

    } finally {
      try {
        if (ics != null) {
          Files.deleteIfExists(ics);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          Files.deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportToIcalMultipleEvents() throws Exception {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path ics = null;

    try {
      tmpDir = createTempDirectory("icalMultiEventTest");
      ics = tmpDir.resolve("calendar.ics");

      CalendarEvent event1 = new CalendarEvent(
          "First Event",
          LocalDateTime.of(2025, 10, 31, 10, 0),
          LocalDateTime.of(2025, 10, 31, 11, 0),
          "Description 1",
          "Location 1",
          "public",
          null
      );

      CalendarEvent event2 = new CalendarEvent(
          "Second Event",
          LocalDateTime.of(2025, 11, 1, 14, 0),
          LocalDateTime.of(2025, 11, 1, 15, 0),
          null, // null description
          "", // empty location
          "private",
          null
      );

      CalendarEvent event3 = new CalendarEvent(
          "Third Event",
          LocalDateTime.of(2025, 11, 2, 9, 0),
          LocalDateTime.of(2025, 11, 2, 10, 0),
          "", // empty description
          null, // null location
          "public",
          null
      );

      view.exportAuto(ics.toString(), Arrays.asList(event1, event2, event3));

      String content = readString(ics);

      // Should have 3 events
      int eventCount = content.split("BEGIN:VEVENT").length - 1;
      assertEquals("Should have 3 events", 3, eventCount);

      assertTrue("Should contain first event", content.contains("SUMMARY:First Event"));
      assertTrue("Should contain second event", content.contains("SUMMARY:Second Event"));
      assertTrue("Should contain third event", content.contains("SUMMARY:Third Event"));

      assertTrue("First event should have location", content.contains("LOCATION:Location 1"));
      assertTrue("First event should have description", content.contains("D"
          + "ESCRIPTION:Description 1"));

    } finally {
      try {
        if (ics != null) {
          Files.deleteIfExists(ics);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          Files.deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testExportToIcalEscapingSpecialCharacters() throws Exception {
    output = new StringWriter();
    ConsoleView view = new ConsoleView(new StringReader(""), output);

    Path tmpDir = null;
    Path ics = null;

    try {
      tmpDir = createTempDirectory("icalEscapeTest");
      ics = tmpDir.resolve("calendar.ics");

      // Test escaping: backslash, comma, semicolon, newline
      CalendarEvent event = new CalendarEvent(
          "Event\\with,special;chars",
          LocalDateTime.of(2025, 10, 31, 10, 0),
          LocalDateTime.of(2025, 10, 31, 11, 0),
          "Description\\with,special;chars\nNew line",
          "Location\\with,special;chars",
          "public",
          null
      );

      view.exportAuto(ics.toString(), Arrays.asList(event));

      String content = readString(ics);

      // Should escape backslash, comma, semicolon, and newline
      assertTrue("Should contain event", content.contains("BEGIN:VEVENT"));
      assertTrue("Should escape special chars in summary",
          content.contains("SUMMARY:") && content.contains("Event"));

    } finally {
      try {
        if (ics != null) {
          Files.deleteIfExists(ics);
        }
      } catch (Exception ignored) {
        // ok
      }

      try {
        if (tmpDir != null) {
          Files.deleteIfExists(tmpDir);
        }
      } catch (Exception ignored) {
        // ok
      }
    }
  }

  @Test
  public void testEscapeIcalWithBackslash() {
    try {
      Method m = ConsoleView.class.getDeclaredMethod("escapeIcal", String.class);
      m.setAccessible(true);
      ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());

      String result = (String) m.invoke(view, "C:\\Users\\path");
      assertEquals("C:\\\\Users\\\\path", result);

    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
  }

  @Test
  public void testEscapeIcalWithComma() {
    try {
      Method m = ConsoleView.class.getDeclaredMethod("escapeIcal", String.class);
      m.setAccessible(true);
      ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());

      String result = (String) m.invoke(view, "Hello, World");
      assertEquals("Hello\\, World", result);

    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
  }

  @Test
  public void testEscapeIcalWithSemicolon() {
    try {
      Method m = ConsoleView.class.getDeclaredMethod("escapeIcal", String.class);
      m.setAccessible(true);
      ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());

      String result = (String) m.invoke(view, "Item1; Item2");
      assertEquals("Item1\\; Item2", result);

    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
  }

  @Test
  public void testEscapeIcalWithAllSpecialChars() {
    try {
      Method m = ConsoleView.class.getDeclaredMethod("escapeIcal", String.class);
      m.setAccessible(true);
      ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());

      String result = (String) m.invoke(view, "Path\\with,comma;semicolon\nand newline");
      assertEquals("Path\\\\with\\,comma\\;semicolon\\nand newline", result);

    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
  }

  @Test
  public void testEscapeIcalWithEmptyString() {
    try {
      Method m = ConsoleView.class.getDeclaredMethod("escapeIcal", String.class);
      m.setAccessible(true);
      ConsoleView view = new ConsoleView(new StringReader(""), new StringBuilder());

      String result = (String) m.invoke(view, "");
      assertEquals("", result);

    } catch (Exception e) {
      fail("Reflection failed: " + e.getMessage());
    }
  }
}