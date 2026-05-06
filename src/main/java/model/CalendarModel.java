package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for the Calendar model.
 * Defines operations for managing calendar events.
 */
public interface CalendarModel {

  /**
   * Adds a single event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event conflicts with existing event
   */
  void addEvent(CalendarEvent event);

  /**
   * Adds multiple events to the calendar (for event series).
   *
   * @param events list of events to add
   * @throws IllegalArgumentException if any event conflicts
   */
  void addEvents(List<CalendarEvent> events);

  /**
   * Finds events matching the given criteria.
   *
   * @param subject       event subject
   * @param startDateTime event start time
   * @return list of matching events
   */
  List<CalendarEvent> findEvents(String subject, LocalDateTime startDateTime);

  /**
   * Updates a single event instance.
   *
   * @param oldEvent the event to update
   * @param newEvent the updated event
   * @throws IllegalArgumentException if update would create conflict
   */
  void updateEvent(CalendarEvent oldEvent, CalendarEvent newEvent);

  /**
   * Updates all events in a series starting from the given event.
   *
   * @param startEvent the event to start from
   * @param property   the property to update
   * @param newValue   the new value
   * @throws IllegalArgumentException if update would create conflict
   */
  void updateEventsFromThis(CalendarEvent startEvent, String property, String newValue);

  /**
   * Updates all events in a series.
   *
   * @param event    any event in the series
   * @param property the property to update
   * @param newValue the new value
   * @throws IllegalArgumentException if update would create conflict
   */
  void updateAllInSeries(CalendarEvent event, String property, String newValue);

  /**
   * Gets all events on a specific date.
   *
   * @param date the date to query
   * @return list of events on that date
   */
  List<CalendarEvent> getEventsOnDate(LocalDate date);

  /**
   * Gets all events in a date/time range.
   *
   * @param start start of range (inclusive)
   * @param end   end of range (inclusive)
   * @return list of events in range
   */
  List<CalendarEvent> getEventsInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if the user is busy at a specific time.
   *
   * @param dateTime the time to check
   * @return true if busy, false if available
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events
   */
  List<CalendarEvent> getAllEvents();

  /**
   * Gets all events in a series.
   *
   * @param seriesId the series identifier
   * @return list of events in the series
   */
  List<CalendarEvent> getEventsInSeries(String seriesId);

  /**
   * Computes analytics for events whose start date lies between from and to (inclusive).
   *
   * @param from start date (inclusive)
   * @param to   end date (inclusive)
   * @return analytics summary for that interval
   */
  CalendarAnalytics getAnalytics(LocalDate from, LocalDate to);
}