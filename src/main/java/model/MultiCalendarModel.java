package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Interface for the Multiple Calendars model.
 * Defines operations for managing calendars.
 */
public interface MultiCalendarModel {

  /**
   * Creates a single calendar with a specific name and default timezone (EST).
   *
   * @param calendarName the calendar name.
   */
  void createCalendar(String calendarName);

  /**
   * Creates a single calendar with a specific name and timezone.
   *
   * @param calendarName the calendar name.
   * @param zone         the calendar's timezone
   */
  void createCalendar(String calendarName, ZoneId zone);

  /**
   * Deletes a calendar by name.
   *
   * @param calendarName the name of calendar that needs to be deleted.
   */
  void deleteCalendar(String calendarName);

  /**
   * Edits the name of a calendar which must be unique.
   *
   * @param oldCalendarName calendar's old name.
   * @param newCalendarName calendar's new name.
   */
  void renameCalendar(String oldCalendarName, String newCalendarName);

  /**
   * Retrieves the calendar model by name.
   *
   * @param calendarName calendar name.
   * @return the calendar model with the same name.
   */
  CalendarModel getCalendar(String calendarName);

  /**
   * Validates whether the calendar with a specific name exists.
   *
   * @param calendarName calendar name.
   * @return true if the calendar exists, false otherwise.
   */
  boolean existCalendar(String calendarName);

  /**
   * Retrieves the calendar's timezone.
   *
   * @param calendarName the calendar that we want to know about its timezone.
   * @return the calendar's timezone.
   */
  ZoneId getTimeZone(String calendarName);

  /**
   * Changes the timezone of a calendar.
   *
   * @param calendarName the calendar's name.
   * @param newZone      the calendar's new timezone.
   */
  void setTimeZone(String calendarName, ZoneId newZone);

  /**
   * Copies a single event from a source calendar to a specified starting date/time
   * in a target calendar.
   *
   * @param sourceCalendarName the name of source calendar
   * @param sourceSubject      the name of source event in the source calendar
   * @param sourceStartTime    the start date/time of the source event
   * @param targetCalendarName the name of target calendar
   * @param targetStartTime    the start date/time of the target event.
   */
  void copyEvent(String sourceCalendarName, String sourceSubject, LocalDateTime sourceStartTime,
                 String targetCalendarName, LocalDateTime targetStartTime);


  /**
   * Copies all events on a specific date from a source calendar to a specific date in a target
   * calendar. Time changes based on timezone only.
   *
   * @param sourceCalendarName the name of source calendar
   * @param sourceDate         the date of the source events
   * @param targetCalendarName the name of target calendar
   * @param targetDate         the date of the target events
   */
  void copyEventsOn(String sourceCalendarName, LocalDate sourceDate,
                    String targetCalendarName, LocalDate targetDate);

  /**
   * Copies all events in a specific range [srcFromDate, srcToDate] from a source calendar to a
   * same interval time starting from a specific date in the target calendar.
   *
   * @param sourceCalendarName the name of source calendar
   * @param srcFromDate        the start date of the range
   * @param srcToDate          the end date of the range
   * @param targetCalendarName the name of target calendar
   * @param targetDate         the start date of the target events
   */
  void copyEventsInBetween(String sourceCalendarName,
                           LocalDate srcFromDate, LocalDate srcToDate,
                           String targetCalendarName, LocalDate targetDate);
}
