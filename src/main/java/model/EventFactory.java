package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Factory class for creating calendar events and event series.
 */
public class EventFactory {

  /**
   * Creates a single event.
   */
  public static CalendarEvent createSingleEvent(String subject,
                                                LocalDateTime start,
                                                LocalDateTime end,
                                                String location,
                                                String description) {
    return new CalendarEvent(subject, start, end, location, description, "public", null);
  }

  /**
   * Creates a single all-day event (00:00 to 23:59).
   */
  public static CalendarEvent createAllDayEvent(String subject, LocalDate date) {
    LocalDateTime start = date.atTime(0, 0);
    LocalDateTime end = date.atTime(23, 59);
    return new CalendarEvent(subject, start, end, null, null, "public", null);
  }

  /**
   * Creates an event series that repeats for a specific number of occurrences.
   */
  public static List<CalendarEvent> createEventSeries(String subject,
                                                      LocalDateTime start,
                                                      LocalDateTime end,
                                                      Set<DayOfWeek> repeatDays,
                                                      int occurrences) {
    List<CalendarEvent> events = new ArrayList<>();
    String seriesId = UUID.randomUUID().toString();

    LocalDate currentDate = start.toLocalDate();
    int count = 0;

    while (count < occurrences) {
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = currentDate.atTime(start.toLocalTime());
        LocalDateTime eventEnd = currentDate.atTime(end.toLocalTime());

        CalendarEvent event = new CalendarEvent(subject, eventStart, eventEnd,
            null, null, "public", seriesId);
        events.add(event);
        count++;
      }
      currentDate = currentDate.plusDays(1);
    }

    return events;
  }

  /**
   * Creates an event series that repeats until a specific date (inclusive).
   */
  public static List<CalendarEvent> createEventSeriesUntil(String subject,
                                                           LocalDateTime start,
                                                           LocalDateTime end,
                                                           Set<DayOfWeek> repeatDays,
                                                           LocalDate untilDate) {
    List<CalendarEvent> events = new ArrayList<>();
    String seriesId = UUID.randomUUID().toString();

    LocalDate currentDate = start.toLocalDate();

    while (!currentDate.isAfter(untilDate)) {
      if (repeatDays.contains(currentDate.getDayOfWeek())) {
        LocalDateTime eventStart = currentDate.atTime(start.toLocalTime());
        LocalDateTime eventEnd = currentDate.atTime(end.toLocalTime());

        CalendarEvent event = new CalendarEvent(subject, eventStart, eventEnd,
            null, null, "public", seriesId);
        events.add(event);
      }
      currentDate = currentDate.plusDays(1);
    }

    return events;
  }

  /**
   * Creates an all-day event series for a specific number of occurrences.
   */
  public static List<CalendarEvent> createAllDayEventSeries(String subject,
                                                            LocalDate startDate,
                                                            Set<DayOfWeek> repeatDays,
                                                            int occurrences) {
    LocalDateTime start = startDate.atTime(0, 0);
    LocalDateTime end = startDate.atTime(23, 59);
    return createEventSeries(subject, start, end, repeatDays, occurrences);
  }

  /**
   * Creates an all-day event series until a specific date.
   */
  public static List<CalendarEvent> createAllDayEventSeriesUntil(String subject,
                                                                 LocalDate startDate,
                                                                 Set<DayOfWeek> repeatDays,
                                                                 LocalDate untilDate) {
    LocalDateTime start = startDate.atTime(0, 0);
    LocalDateTime end = startDate.atTime(23, 59);
    return createEventSeriesUntil(subject, start, end, repeatDays, untilDate);
  }

  /**
   * Parses weekday string (e.g., "MWF") into a set of DayOfWeek.
   */
  public static Set<DayOfWeek> parseWeekdays(String weekdayStr) {
    Set<DayOfWeek> days = new java.util.HashSet<>();

    for (char c : weekdayStr.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday character: " + c);
      }
    }

    return days;
  }
}