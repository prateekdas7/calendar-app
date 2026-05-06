package controller;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.CalendarAnalytics;
import model.CalendarEvent;
import model.CalendarModel;
import model.EventFactory;
import model.MultiCalendarModel;
import view.CalendarView;


/**
 * Controller for the calendar application.
 * Handles command parsing and coordinates between model and view.
 */
public class CalendarController {
  private final CalendarModel model;
  private final MultiCalendarModel multi;
  private CalendarModel activeModel;
  private String activeCalendarName;
  private final CalendarView view;

  /**
   * Constructs a CalendarController with the given model and view.
   *
   * @param model the calendar model
   * @param view  the calendar view
   */
  public CalendarController(CalendarModel model, CalendarView view) {
    this.model = model;
    this.multi = null;
    this.view = view;
    this.activeModel = null;
    this.activeCalendarName = null;
  }

  /**
   * Constructs a CalendarController with the given multiCalendarModel and view.
   *
   * @param multi the multi calendars model
   * @param view  the calendar view
   */
  public CalendarController(MultiCalendarModel multi, CalendarView view) {
    this.model = null;
    this.multi = multi;
    this.view = view;
    this.activeModel = null;
    this.activeCalendarName = null;
  }

  /**
   * This helper returns the currently avtive CalendarModel.
   *
   * @return the currently avtive CalendarModel.
   */
  private CalendarModel currentModel() {
    if (activeModel != null) {
      return activeModel;
    }

    if (model != null) {
      return model;
    }

    throw new IllegalStateException("No calendar in use. Use: use calendar --name <calName>");
  }

  /**
   * Processes a single command input by the user.
   *
   * @param command the input command string
   */
  public void processCommand(String command) {
    try {
      command = command.trim();
      if (command.isEmpty() || command.equalsIgnoreCase("exit")) {
        return;
      }

      if (command.startsWith("create event ")) {
        handleCreateEvent(command);
      } else if (command.startsWith("edit event ")) {
        handleEditEvent(command);
      } else if (command.startsWith("edit events ")) {
        handleEditEvents(command);
      } else if (command.startsWith("edit series ")) {
        handleEditSeries(command);
      } else if (command.startsWith("print events on ")) {
        handlePrintEventsOnDate(command);
      } else if (command.startsWith("print events from ")) {
        handlePrintEventsInRange(command);
      } else if (command.startsWith("export cal ")) {
        handleExportCalendar(command);
      } else if (command.startsWith("show status on ")) {
        handleShowStatus(command);
      } else if (command.startsWith("create calendar ")) {
        ensureMultiMode();
        handleCreateCalendar(command);
      } else if (command.startsWith("edit calendar ")) {
        ensureMultiMode();
        handleEditCalendar(command);
      } else if (command.startsWith("use calendar ")) {
        ensureMultiMode();
        handleUseCalendar(command);
      } else if (command.startsWith("copy event ")) {
        ensureMultiMode();
        currentModel();
        handleCopyEvent(command);
      } else if (command.startsWith("copy events on ")) {
        ensureMultiMode();
        currentModel();
        handleCopyEventsOn(command);
      } else if (command.startsWith("copy events between ")) {
        ensureMultiMode();
        currentModel();
        handleCopyEventsBetween(command);
      } else if (command.startsWith("show calendar dashboard from ")) {
        ensureMultiMode();
        currentModel();
        handleShowDashboard(command);
      } else {
        throw new IllegalArgumentException("Unknown command: " + command);
      }
    } catch (Exception e) {
      view.displayError("Error: " + e.getMessage());
    }
  }

  private void ensureMultiMode() {
    if (multi == null) {
      throw new IllegalArgumentException("This command requires a multi calendar controller.");
    }
  }

  private void handleCreateEvent(String command) {
    currentModel();

    String cmdBody = command.substring("create event ".length());

    Pattern p1 = Pattern.compile(
        "\"([^\"]+)\" from (\\S+) to (\\S+) repeats (\\w+) for (\\d+) times"
    );
    Matcher m1 = p1.matcher(cmdBody);
    if (m1.matches()) {
      createRepeatingEvent(m1.group(1), m1.group(2), m1.group(3), m1.group(4),
          Integer.parseInt(m1.group(5)), null);
      return;
    }

    Pattern p2 = Pattern.compile(
        "(\\S+) from (\\S+) to (\\S+) repeats (\\w+) for (\\d+) times"
    );
    Matcher m2 = p2.matcher(cmdBody);
    if (m2.matches()) {
      createRepeatingEvent(m2.group(1), m2.group(2), m2.group(3), m2.group(4),
          Integer.parseInt(m2.group(5)), null);
      return;
    }

    Pattern p3 = Pattern.compile(
        "\"([^\"]+)\" from (\\S+) to (\\S+) repeats (\\w+) until (\\S+)"
    );
    Matcher m3 = p3.matcher(cmdBody);
    if (m3.matches()) {
      createRepeatingEvent(m3.group(1), m3.group(2), m3.group(3), m3.group(4),
          -1, m3.group(5));
      return;
    }

    Pattern p4 = Pattern.compile(
        "(\\S+) from (\\S+) to (\\S+) repeats (\\w+) until (\\S+)"
    );
    Matcher m4 = p4.matcher(cmdBody);
    if (m4.matches()) {
      createRepeatingEvent(m4.group(1), m4.group(2), m4.group(3), m4.group(4),
          -1, m4.group(5));
      return;
    }

    Pattern p5 = Pattern.compile("\"([^\"]+)\" from (\\S+) to (\\S+)");
    Matcher m5 = p5.matcher(cmdBody);
    if (m5.matches()) {
      createSingleEvent(m5.group(1), m5.group(2), m5.group(3));
      return;
    }

    Pattern p6 = Pattern.compile("(\\S+) from (\\S+) to (\\S+)");
    Matcher m6 = p6.matcher(cmdBody);
    if (m6.matches()) {
      createSingleEvent(m6.group(1), m6.group(2), m6.group(3));
      return;
    }

    Pattern p7 = Pattern.compile(
        "\"([^\"]+)\" on (\\S+) repeats (\\w+) for (\\d+) times"
    );
    Matcher m7 = p7.matcher(cmdBody);
    if (m7.matches()) {
      createAllDayRepeatingEvent(m7.group(1), m7.group(2), m7.group(3),
          Integer.parseInt(m7.group(4)), null);
      return;
    }

    Pattern p8 = Pattern.compile("(\\S+) on (\\S+) repeats (\\w+) for (\\d+) times");
    Matcher m8 = p8.matcher(cmdBody);
    if (m8.matches()) {
      createAllDayRepeatingEvent(m8.group(1), m8.group(2), m8.group(3),
          Integer.parseInt(m8.group(4)), null);
      return;
    }

    Pattern p9 = Pattern.compile(
        "\"([^\"]+)\" on (\\S+) repeats (\\w+) until (\\S+)"
    );
    Matcher m9 = p9.matcher(cmdBody);
    if (m9.matches()) {
      createAllDayRepeatingEvent(m9.group(1), m9.group(2), m9.group(3),
          -1, m9.group(4));
      return;
    }

    Pattern p10 = Pattern.compile("(\\S+) on (\\S+) repeats (\\w+) until (\\S+)");
    Matcher m10 = p10.matcher(cmdBody);
    if (m10.matches()) {
      createAllDayRepeatingEvent(m10.group(1), m10.group(2), m10.group(3),
          -1, m10.group(4));
      return;
    }

    Pattern p11 = Pattern.compile("\"([^\"]+)\" on (\\S+)");
    Matcher m11 = p11.matcher(cmdBody);
    if (m11.matches()) {
      createAllDayEvent(m11.group(1), m11.group(2));
      return;
    }

    Pattern p12 = Pattern.compile("(\\S+) on (\\S+)");
    Matcher m12 = p12.matcher(cmdBody);
    if (m12.matches()) {
      createAllDayEvent(m12.group(1), m12.group(2));
      return;
    }

    throw new IllegalArgumentException("Invalid create event command format");
  }

  private void createSingleEvent(String subject, String startStr, String endStr) {
    LocalDateTime start = LocalDateTime.parse(startStr);
    LocalDateTime end = LocalDateTime.parse(endStr);
    CalendarEvent event = EventFactory.createSingleEvent(subject, start, end, null, null);
    currentModel().addEvent(event);
    view.displayMessage("Event created successfully");
  }

  private void createAllDayEvent(String subject, String dateStr) {
    LocalDate date = LocalDate.parse(dateStr);
    CalendarEvent event = EventFactory.createAllDayEvent(subject, date);
    currentModel().addEvent(event);
    view.displayMessage("All-day event created successfully");
  }

  private void createRepeatingEvent(String subject, String startStr, String endStr,
                                    String weekdaysStr, int occurrences, String untilStr) {
    LocalDateTime start = LocalDateTime.parse(startStr);
    LocalDateTime end = LocalDateTime.parse(endStr);
    Set<DayOfWeek> weekdays = EventFactory.parseWeekdays(weekdaysStr);

    List<CalendarEvent> events;
    if (occurrences > 0) {
      events = EventFactory.createEventSeries(subject, start, end, weekdays, occurrences);
    } else {
      LocalDate until = LocalDate.parse(untilStr);
      events = EventFactory.createEventSeriesUntil(subject, start, end, weekdays, until);
    }

    currentModel().addEvents(events);
    view.displayMessage("Event series created with " + events.size() + " occurrences");
  }

  private void createAllDayRepeatingEvent(String subject, String dateStr, String weekdaysStr,
                                          int occurrences, String untilStr) {
    LocalDate date = LocalDate.parse(dateStr);
    Set<DayOfWeek> weekdays = EventFactory.parseWeekdays(weekdaysStr);

    List<CalendarEvent> events;
    if (occurrences > 0) {
      events = EventFactory.createAllDayEventSeries(subject, date, weekdays, occurrences);
    } else {
      LocalDate until = LocalDate.parse(untilStr);
      events = EventFactory.createAllDayEventSeriesUntil(subject, date, weekdays, until);
    }

    currentModel().addEvents(events);
    view.displayMessage("All-day event series created with " + events.size() + " occurrences");
  }

  private void handleEditEvent(String command) {
    String cmdBody = command.substring("edit event ".length());

    Pattern p1 = Pattern.compile("(\\w+) \"([^\"]+)\" from (\\S+) to (\\S+) with (.+)");
    Matcher m1 = p1.matcher(cmdBody);
    if (m1.matches()) {
      editSingleEvent(m1.group(1), m1.group(2), m1.group(3), m1.group(5));
      return;
    }

    Pattern p2 = Pattern.compile("(\\w+) (\\S+) from (\\S+) to (\\S+) with (.+)");
    Matcher m2 = p2.matcher(cmdBody);
    if (m2.matches()) {
      editSingleEvent(m2.group(1), m2.group(2), m2.group(3), m2.group(5));
      return;
    }

    Pattern p3 = Pattern.compile("(\\w+) \"([^\"]+)\" from (\\S+) with (.+)");
    Matcher m3 = p3.matcher(cmdBody);
    if (m3.matches()) {
      editSingleEvent(m3.group(1), m3.group(2), m3.group(3), m3.group(4));
      return;
    }

    Pattern p4 = Pattern.compile("(\\w+) (\\S+) from (\\S+) with (.+)");
    Matcher m4 = p4.matcher(cmdBody);
    if (m4.matches()) {
      editSingleEvent(m4.group(1), m4.group(2), m4.group(3), m4.group(4));
      return;
    }

    throw new IllegalArgumentException("Invalid edit event command format");
  }

  private void editSingleEvent(String property, String subject, String startStr, String newValue) {
    LocalDateTime start = LocalDateTime.parse(startStr);
    List<CalendarEvent> events = currentModel().findEvents(subject, start);

    if (events.isEmpty()) {
      throw new IllegalArgumentException("No event found with specified criteria");
    }
    if (events.size() > 1) {
      throw new IllegalArgumentException("Multiple events found. Cannot edit.");
    }

    CalendarEvent event = events.get(0);
    CalendarEvent updated = applyPropertyChange(event, property, newValue);
    currentModel().updateEvent(event, updated);
    view.displayMessage("Event updated successfully");
  }

  private void handleEditEvents(String command) {
    String cmdBody = command.substring("edit events ".length());

    Pattern p1 = Pattern.compile("(\\w+) \"([^\"]+)\" from (\\S+) with (.+)");
    Matcher m1 = p1.matcher(cmdBody);
    if (m1.matches()) {
      editEventsFromThis(m1.group(1), m1.group(2), m1.group(3), m1.group(4));
      return;
    }

    Pattern p2 = Pattern.compile("(\\w+) (\\S+) from (\\S+) with (.+)");
    Matcher m2 = p2.matcher(cmdBody);
    if (m2.matches()) {
      editEventsFromThis(m2.group(1), m2.group(2), m2.group(3), m2.group(4));
      return;
    }

    throw new IllegalArgumentException("Invalid edit events command format");
  }

  private void editEventsFromThis(
      String property,
      String subject,
      String startStr,
      String newValue
  ) {
    LocalDateTime start = LocalDateTime.parse(startStr);
    List<CalendarEvent> events = currentModel().findEvents(subject, start);

    if (events.isEmpty()) {
      throw new IllegalArgumentException("No event found with specified criteria");
    }
    if (events.size() > 1) {
      throw new IllegalArgumentException("Multiple events found. Cannot edit.");
    }

    CalendarEvent event = events.get(0);
    currentModel().updateEventsFromThis(event, property, newValue);
    view.displayMessage("Events updated successfully");
  }

  private void handleEditSeries(String command) {
    String cmdBody = command.substring("edit series ".length());

    Pattern p1 = Pattern.compile("(\\w+) \"([^\"]+)\" from (\\S+) with (.+)");
    Matcher m1 = p1.matcher(cmdBody);
    if (m1.matches()) {
      editAllInSeries(m1.group(1), m1.group(2), m1.group(3), m1.group(4));
      return;
    }

    Pattern p2 = Pattern.compile("(\\w+) (\\S+) from (\\S+) with (.+)");
    Matcher m2 = p2.matcher(cmdBody);
    if (m2.matches()) {
      editAllInSeries(m2.group(1), m2.group(2), m2.group(3), m2.group(4));
      return;
    }

    throw new IllegalArgumentException("Invalid edit series command format");
  }

  private void editAllInSeries(String property, String subject, String startStr, String newValue) {
    LocalDateTime start = LocalDateTime.parse(startStr);
    List<CalendarEvent> events = currentModel().findEvents(subject, start);

    if (events.isEmpty()) {
      throw new IllegalArgumentException("No event found with specified criteria");
    }
    if (events.size() > 1) {
      throw new IllegalArgumentException("Multiple events found. Cannot edit.");
    }

    CalendarEvent event = events.get(0);
    currentModel().updateAllInSeries(event, property, newValue);
    view.displayMessage("Series updated successfully");
  }

  private CalendarEvent applyPropertyChange(CalendarEvent event, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
        return event.withSubject(newValue);
      case "start":
        return event.withStartDateTime(LocalDateTime.parse(newValue));
      case "end":
        return event.withEndDateTime(LocalDateTime.parse(newValue));
      case "description":
        return event.withDescription(newValue);
      case "location":
        return event.withLocation(newValue);
      case "status":
        return event.withStatus(newValue);
      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  private void handlePrintEventsOnDate(String command) {
    String dateStr = command.substring("print events on ".length()).trim();
    LocalDate date = LocalDate.parse(dateStr);
    List<CalendarEvent> events = currentModel().getEventsOnDate(date);
    view.displayEventsOnDate(date, events);
  }

  private void handlePrintEventsInRange(String command) {
    String rangeStr = command.substring("print events from ".length()).trim();
    String[] parts = rangeStr.split(" to ");

    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid date range format");
    }

    LocalDateTime start = LocalDateTime.parse(parts[0].trim());
    LocalDateTime end = LocalDateTime.parse(parts[1].trim());
    List<CalendarEvent> events = currentModel().getEventsInRange(start, end);
    view.displayEventsInRange(start, end, events);
  }

  private void handleExportCalendar(String command) {
    String filename = command.substring("export cal ".length()).trim();
    List<CalendarEvent> events = currentModel().getAllEvents();
    view.exportAuto(filename, events);
  }

  private void handleShowStatus(String command) {
    String dateTimeStr = command.substring("show status on ".length()).trim();
    LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr);
    boolean busy = currentModel().isBusy(dateTime);
    view.displayStatus(dateTime, busy);
  }

  private void handleCreateCalendar(String command) {
    String cmdBody = command.substring("create calendar ".length()).trim();

    Pattern p1 = Pattern.compile("--name\\s+\"([^\"]+)\"\\s+--timezone\\s+(\\S+)");
    Matcher m1 = p1.matcher(cmdBody);

    Pattern p2 = Pattern.compile("--name\\s+(\\S+)\\s+--timezone\\s+(\\S+)");
    Matcher m2 = p2.matcher(cmdBody);

    String calendarName;
    String timeZoneString;

    if (m1.matches()) {
      calendarName = m1.group(1).trim();
      timeZoneString = m1.group(2).trim();

    } else if (m2.matches()) {
      calendarName = m2.group(1).trim();
      timeZoneString = m2.group(2).trim();

    } else {
      throw new IllegalArgumentException("Invalid command. "
          + "Use: create calendar --name <calName> --timezone area/location");
    }

    final ZoneId zone;
    try {
      zone = ZoneId.of(timeZoneString);

    } catch (DateTimeException dte) {
      throw new IllegalArgumentException("Unsupported timezone: " + timeZoneString);
    }

    try {
      multi.createCalendar(calendarName, zone);
      view.displayMessage("Calendar \"" + calendarName + "\" successfully created in timezone "
          + zone.getId());

    } catch (IllegalArgumentException iae) {
      throw iae;
    }
  }

  /**
   * edit calendar --name {name-of-calendar} --property {property-name} {new-property-value}.
   * existing property is either name or timezone of the calendar，for now.
   */
  private void handleEditCalendar(String command) {
    String cmdBody = command.substring("edit calendar ".length()).trim();

    Pattern p = Pattern.compile(
        "--name\\s+(?:\"([^\"]+)\"|(\\S+))\\s+"
            + "--property\\s+(\\w+)\\s+"
            + "(?:\"([^\"]+)\"|(\\S+))\\s*$"
    );
    Matcher m = p.matcher(cmdBody);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid command. "
          + "Use: edit calendar --name <name-of-calendar> "
          + "--property <property-name> <new-property-value>.");
    }

    String calendarName = m.group(1) != null ? m.group(1) : m.group(2);
    String property = m.group(3).toLowerCase();
    String newValue = m.group(4) != null ? m.group(4) : m.group(5);

    if (!multi.existCalendar(calendarName)) {
      throw new IllegalArgumentException("Calendar \"" + calendarName + "\" does not exist.");
    }

    switch (property) {
      case "name":
        multi.renameCalendar(calendarName, newValue);
        view.displayMessage("Calendar \"" + calendarName + "\" successfully renamed to \""
            + newValue + "\".");

        if (activeCalendarName != null && activeCalendarName.equalsIgnoreCase(calendarName)) {
          activeCalendarName = newValue;
          activeModel = multi.getCalendar(newValue);
        }
        break;

      case "timezone":
        try {
          ZoneId zone = ZoneId.of(newValue);
          multi.setTimeZone(calendarName, zone);
          view.displayMessage("Calendar \"" + calendarName + "\" successfully modified timezone to "
              + zone.getId());

        } catch (DateTimeException dte) {
          throw new IllegalArgumentException("Unsupported timezone: " + newValue);
        }
        break;

      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * use calendar --name {name-of-calendar}.
   */
  private void handleUseCalendar(String command) {
    String cmdBody = command.substring("use calendar ".length()).trim();

    Pattern p = Pattern.compile("--name\\s+(?:\"([^\"]+)\"|(\\S+))\\s*$");
    Matcher m = p.matcher(cmdBody);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid command. "
          + "Use: use calendar --name <name-of-calendar>");
    }

    String calendarName = (m.group(1) != null ? m.group(1) : m.group(2)).trim();

    if (!multi.existCalendar(calendarName)) {
      throw new IllegalArgumentException("Calendar \"" + calendarName + "\" does not exist.");
    }

    this.activeModel = multi.getCalendar(calendarName);
    this.activeCalendarName = calendarName;
    view.displayMessage("Using calendar \"" + calendarName + "\"");
  }

  /**
   * copy event {eventName} on {dateStringTtimeString} --target {calendarName} to
   * {dateStringTtimeString}.
   */
  private void handleCopyEvent(String command) {
    currentModel();

    String cmdBody = command.substring("copy event ".length()).trim();

    Pattern p = Pattern.compile(
        "(?:\"([^\"]+)\"|(\\S+))\\s+"
            + "on\\s+(\\S+)\\s+"
            + "--target\\s+(?:\"([^\"]+)\"|(\\S+))\\s+"
            + "to\\s+(\\S+)\\s*$"
    );
    Matcher m = p.matcher(cmdBody);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid command. "
          + "Use: copy event --name <nameName> on <dateStringTtimeString> "
          + "--target <calendarName> to <dateStringTtimeString>");
    }

    String subject = (m.group(1) != null ? m.group(1) : m.group(2)).trim();
    String sourceStartTimeString = m.group(3);
    String targetCalendar = m.group(4) != null ? m.group(4) : m.group(5);
    String targetStartTimeString = m.group(6);

    LocalDateTime sourceStartTime = LocalDateTime.parse(sourceStartTimeString);
    LocalDateTime targetStartTime = LocalDateTime.parse(targetStartTimeString);

    multi.copyEvent(activeCalendarName, subject, sourceStartTime, targetCalendar, targetStartTime);
    view.displayMessage("Successfully copied \"" + subject + "\" from \"" + activeCalendarName
        + "\" to \"" + targetCalendar + "\" starting " + targetStartTimeString);
  }

  /**
   * copy events on {dateString} --target {calendarName} to {dateString}.
   */
  private void handleCopyEventsOn(String command) {

    String cmdBody = command.substring("copy events on ".length()).trim();

    Pattern p = Pattern.compile(
        "(\\d{4}-\\d{2}-\\d{2})\\s+"
            + "--target\\s+(?:\"([^\"]+)\"|(\\S+))\\s+"
            + "to\\s+(\\d{4}-\\d{2}-\\d{2})\\s*$"
    );
    Matcher m = p.matcher(cmdBody);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid command. "
          + "Use: copy events on <dateString> --target <calendarName> to <dateString>");
    }

    LocalDate srcDate = LocalDate.parse(m.group(1));
    String targetCalendar = m.group(2) != null ? m.group(2) : m.group(3);
    LocalDate trgDate = LocalDate.parse(m.group(4));

    multi.copyEventsOn(activeCalendarName, srcDate, targetCalendar, trgDate);
    view.displayMessage("Successfully copied events from \"" + activeCalendarName
        + "\" (on \"" + srcDate + ") to \"" + targetCalendar + "\" (on \"" + trgDate + "\").");
  }

  /**
   * copy events between {dateString} and {dateString} --target {calendarName} to {dateString}.
   */
  private void handleCopyEventsBetween(String command) {

    String cmdBody = command.substring("copy events between ".length()).trim();

    Pattern p = Pattern.compile(
        "(\\d{4}-\\d{2}-\\d{2})\\s+and\\s+(\\d{4}-\\d{2}-\\d{2})\\s+"
            + "--target\\s+(?:\"([^\"]+)\"|(\\S+))\\s+"
            + "to\\s+(\\d{4}-\\d{2}-\\d{2})\\s*$"
    );
    Matcher m = p.matcher(cmdBody);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid command.  Use: copy events between <dateString> "
          + "and <dateString> --target <calendarName> to <dateString>");
    }

    LocalDate srcFrom = LocalDate.parse(m.group(1));
    LocalDate srcTo = LocalDate.parse(m.group(2));
    String trgCalendar = m.group(3) != null ? m.group(3) : m.group(4);
    LocalDate trgDate = LocalDate.parse(m.group(5));

    multi.copyEventsInBetween(activeCalendarName, srcFrom, srcTo, trgCalendar, trgDate);
    view.displayMessage("Successfully copied events between \"" + srcFrom + "\" to \"" + srcTo
        + "\" from" + activeCalendarName + " to \"" + trgCalendar + "\" starting " + trgDate);
  }

  private void handleShowDashboard(String command) {
    CalendarModel cm = currentModel();

    String body = command.substring("show calendar dashboard from ".length()).trim();
    String[] parts = body.split("\\s+to\\s+");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid date range. "
          + "Use: show calendar dashboard from YYYY-MM-DD to YYYY-MM-DD");
    }

    LocalDate from = LocalDate.parse(parts[0].trim());
    LocalDate to = LocalDate.parse(parts[1].trim());

    CalendarAnalytics analytics = cm.getAnalytics(from, to);
    displayAnalytics(analytics);
  }

  private void displayAnalytics(CalendarAnalytics a) {
    StringBuilder sb = new StringBuilder();

    sb.append("Calendar dashboard from ")
        .append(a.getFromDate())
        .append(" to ")
        .append(a.getToDate())
        .append("\n\n");

    sb.append("Total events: ").append(a.getTotalEvents()).append("\n\n");

    sb.append("Total events by subject:\n");
    if (a.getEventsBySubject().isEmpty()) {
      sb.append("  (none)\n");
    } else {
      a.getEventsBySubject().forEach((subj, count) ->
          sb.append("  ").append(subj).append(": ").append(count).append("\n"));
    }
    sb.append("\n");

    sb.append("Total events by weekday:\n");
    if (a.getEventsByWeekday().isEmpty()) {
      sb.append("  (none)\n");
    } else {
      a.getEventsByWeekday().forEach((dow, count) ->
          sb.append("  ").append(dow).append(": ").append(count).append("\n"));
    }
    sb.append("\n");

    sb.append("Total events by week (relative to start date):\n");
    if (a.getEventsByWeekIndex().isEmpty()) {
      sb.append("  (none)\n");
    } else {
      a.getEventsByWeekIndex().forEach((weekIndex, count) ->
          sb.append("  Week ").append(weekIndex).append(": ")
              .append(count).append("\n"));
    }
    sb.append("\n");

    sb.append("Total events by month:\n");
    if (a.getEventsByMonth().isEmpty()) {
      sb.append("  (none)\n");
    } else {
      a.getEventsByMonth().forEach((ym, count) ->
          sb.append("  ").append(ym).append(": ").append(count).append("\n"));
    }
    sb.append("\n");

    sb.append("Average events per day: ")
        .append(String.format("%.2f", a.getAverageEventsPerDay()))
        .append("\n");

    sb.append("Busiest day: ")
        .append(a.getBusiestDay())
        .append(" (")
        .append(a.getBusiestDayCount())
        .append(" events)\n");

    sb.append("Least busy day: ")
        .append(a.getLeastBusyDay())
        .append(" (")
        .append(a.getLeastBusyDayCount())
        .append(" events)\n\n");

    sb.append("Online events: ").append(a.getOnlineEvents()).append("\n");
    sb.append("Offline / other events: ").append(a.getOfflineEvents()).append("\n");
    sb.append("Percentage online: ")
        .append(String.format("%.1f", a.getOnlinePercentage()))
        .append("%\n");

    view.displayMessage(sb.toString());
  }
}
