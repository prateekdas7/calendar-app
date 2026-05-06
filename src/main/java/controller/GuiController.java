package controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import model.CalendarAnalytics;
import model.CalendarEvent;
import model.CalendarModel;
import model.EventFactory;
import model.MultiCalendarModel;

/**
 * Controller for the GUI view of the calendar application.
 * Manages interactions between the GUI and the model.
 */
public class GuiController {
  private final MultiCalendarModel multiModel;
  private CalendarModel activeModel;
  private String activeCalendarName;
  private ZoneId activeTimezone;

  /**
   * Constructs a GUIController with a multi-calendar model.
   *
   * @param multiModel the multi-calendar model
   */
  public GuiController(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;

    ZoneId systemZone = ZoneId.systemDefault();
    this.activeCalendarName = "Default Calendar";
    this.activeTimezone = systemZone;

    try {
      multiModel.createCalendar(activeCalendarName, systemZone);
      this.activeModel = multiModel.getCalendar(activeCalendarName);
    } catch (IllegalArgumentException e) {
      this.activeModel = multiModel.getCalendar(activeCalendarName);
    }
  }

  /**
   * Creates a new calendar with the specified name and timezone.
   *
   * @param name     the calendar name
   * @param timezone the timezone string (e.g., "America/New_York")
   * @throws IllegalArgumentException if timezone is invalid or calendar already exists
   */
  public void createCalendar(String name, String timezone) {
    ZoneId zone = ZoneId.of(timezone);
    multiModel.createCalendar(name, zone);
  }

  /**
   * Switches to a different calendar.
   *
   * @param name the name of the calendar to switch to
   */
  public void switchCalendar(String name) {
    this.activeModel = multiModel.getCalendar(name);
    this.activeCalendarName = name;
    this.activeTimezone = multiModel.getTimeZone(name);
  }

  /**
   * Creates a single event.
   *
   * @param subject     the event subject
   * @param startStr    start date/time string (ISO format)
   * @param endStr      end date/time string (ISO format)
   * @param location    optional location
   * @param description optional description
   */
  public void createSingleEvent(String subject, String startStr, String endStr,
                                String location, String description) {
    LocalDateTime start = LocalDateTime.parse(startStr);
    LocalDateTime end = LocalDateTime.parse(endStr);
    CalendarEvent event = EventFactory.createSingleEvent(subject, start, end,
        location, description);
    activeModel.addEvent(event);
  }

  /**
   * Creates an all-day event.
   *
   * @param subject the event subject
   * @param dateStr the date string (ISO format)
   */
  public void createAllDayEvent(String subject, String dateStr) {
    LocalDate date = LocalDate.parse(dateStr);
    CalendarEvent event = EventFactory.createAllDayEvent(subject, date);
    activeModel.addEvent(event);
  }

  /**
   * Creates a repeating event.
   *
   * @param subject     the event subject
   * @param startStr    start date/time string
   * @param endStr      end date/time string
   * @param weekdaysStr weekdays string (e.g., "MWF")
   * @param occurrences number of occurrences (-1 if using until date)
   * @param untilStr    until date string (null if using occurrences)
   */
  public void createRepeatingEvent(String subject, String startStr, String endStr,
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

    activeModel.addEvents(events);
  }

  /**
   * Creates a repeating all-day event.
   *
   * @param subject     the event subject
   * @param dateStr     the start date string
   * @param weekdaysStr weekdays string (e.g., "MWF")
   * @param occurrences number of occurrences (-1 if using until date)
   * @param untilStr    until date string (null if using occurrences)
   */
  public void createAllDayRepeatingEvent(String subject, String dateStr, String weekdaysStr,
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

    activeModel.addEvents(events);
  }

  /**
   * Gets all events on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date
   */
  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    return activeModel.getEventsOnDate(date);
  }

  /**
   * Edits an event or series of events.
   *
   * @param subject     the event subject
   * @param dateTimeStr the event start date/time string
   * @param property    the property to edit
   * @param newValue    the new value
   * @param scope       0=single, 1=from this, 2=all in series
   */
  public void editEvent(String subject, String dateTimeStr, String property,
                        String newValue, int scope) {
    LocalDateTime startTime = LocalDateTime.parse(dateTimeStr);
    List<CalendarEvent> events = activeModel.findEvents(subject, startTime);

    if (events.isEmpty()) {
      throw new IllegalArgumentException("Event not found");
    }
    if (events.size() > 1) {
      throw new IllegalArgumentException("Multiple events found with same criteria");
    }

    CalendarEvent event = events.get(0);

    switch (scope) {
      case 0:
        CalendarEvent updated = applyPropertyChange(event, property, newValue);
        activeModel.updateEvent(event, updated);
        break;
      case 1:
        activeModel.updateEventsFromThis(event, property, newValue);
        break;
      case 2:
        activeModel.updateAllInSeries(event, property, newValue);
        break;
      default:
        throw new IllegalArgumentException("Invalid scope");
    }
  }

  /**
   * Applies a property change to an event.
   */
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

  /**
   * Exports the current calendar to a file.
   *
   * @param filename the filename (with .csv or .ics extension)
   */
  public void exportCalendar(String filename) {
    List<CalendarEvent> events = activeModel.getAllEvents();

    java.io.StringWriter sw = new java.io.StringWriter();
    view.ConsoleView tempView = new view.ConsoleView(
        new java.io.StringReader(""), sw);

    tempView.exportAuto(filename, events);
  }

  /**
   * Gets the current timezone of the active calendar.
   *
   * @return the timezone ID string
   */
  public String getCurrentTimezone() {
    return activeTimezone.getId();
  }

  /**
   * Gets the name of the currently active calendar.
   *
   * @return the calendar name
   */
  public String getActiveCalendarName() {
    return activeCalendarName;
  }

  /**
   * Returns analytics for the active calendar in the given date range (inclusive).
   *
   * @param from start date (inclusive)
   * @param to   end date (inclusive)
   * @return analytics summary for that interval
   */
  public CalendarAnalytics getCalendarAnalytics(LocalDate from, LocalDate to) {
    return activeModel.getAnalytics(from, to);
  }
}