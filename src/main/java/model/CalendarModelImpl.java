package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Implementation of CalendarModel.
 * Uses a list-based representation for storing events.
 * This representation was chosen for simplicity and flexibility in querying.
 */
public class CalendarModelImpl implements CalendarModel {
  private final List<CalendarEvent> events;

  /**
   * Constructs an empty CalendarModelImpl.
   * Initializes an empty list of events.
   */
  public CalendarModelImpl() {
    this.events = new ArrayList<>();
  }

  @Override
  public void addEvent(CalendarEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (events.contains(event)) {
      throw new IllegalArgumentException(
          "An event with the same subject, start time, and end time already exists");
    }

    for (CalendarEvent existing : events) {

      if (event == existing) {
        continue;
      }

      if (isAllDayEvent(event) && isAllDayEvent(existing)) {
        continue;
      }

      if (isAllDayEvent(event) || isAllDayEvent(existing)) {
        continue;
      }

      if (eventsOverlap(event, existing)) {
        throw new IllegalArgumentException(
            "Event conflicts with existing event: " + existing.getSubject()
                + " at " + existing.getStartDateTime());
      }
    }

    events.add(event);
  }

  /**
   * Checks if two events overlap in time.
   *
   * @param e1 first event
   * @param e2 second event
   * @return true if events overlap, false otherwise
   */
  private boolean eventsOverlap(CalendarEvent e1, CalendarEvent e2) {
    return e1.getStartDateTime().isBefore(e2.getEndDateTime())
        && e1.getEndDateTime().isAfter(e2.getStartDateTime());
  }

  @Override
  public void addEvents(List<CalendarEvent> eventsToAdd) {
    for (CalendarEvent event : eventsToAdd) {
      addEvent(event);
    }
  }

  private boolean isAllDayEvent(CalendarEvent event) {
    long minutes = java.time.Duration.between(
        event.getStartDateTime(), event.getEndDateTime()).toMinutes();
    return minutes >= 1435;
  }

  @Override
  public List<CalendarEvent> findEvents(String subject, LocalDateTime startDateTime) {
    return events.stream()
        .filter(e -> e.getSubject().equals(subject)
            && e.getStartDateTime().equals(startDateTime))
        .collect(Collectors.toList());
  }

  @Override
  public void updateEvent(CalendarEvent oldEvent, CalendarEvent newEvent) {
    if (!events.contains(oldEvent)) {
      throw new IllegalArgumentException("Event not found in calendar");
    }

    events.remove(oldEvent);

    try {
      for (CalendarEvent existing : events) {
        if (isAllDayEvent(newEvent) && isAllDayEvent(existing)) {
          continue;
        }

        if (isAllDayEvent(newEvent) || isAllDayEvent(existing)) {
          continue;
        }

        if (eventsOverlap(newEvent, existing)) {
          throw new IllegalArgumentException(
              "Event conflicts with existing event: " + existing.getSubject()
                  + " at " + existing.getStartDateTime());
        }
      }

      events.add(newEvent);

    } catch (Exception e) {
      events.add(oldEvent);
      throw e;
    }
  }

  @Override
  public void updateEventsFromThis(CalendarEvent startEvent, String property, String newValue) {
    if (!startEvent.isPartOfSeries()) {
      updateSingleEventProperty(startEvent, property, newValue);
      return;
    }

    String seriesId = startEvent.getSeriesId();
    List<CalendarEvent> seriesEvents = getEventsInSeries(seriesId);

    List<CalendarEvent> toUpdate = seriesEvents.stream()
        .filter(e -> !e.getStartDateTime().isBefore(startEvent.getStartDateTime()))
        .collect(Collectors.toList());

    for (CalendarEvent event : toUpdate) {
      updateSingleEventProperty(event, property, newValue);
    }
  }

  @Override
  public void updateAllInSeries(CalendarEvent event, String property, String newValue) {
    if (!event.isPartOfSeries()) {
      updateSingleEventProperty(event, property, newValue);
      return;
    }

    String seriesId = event.getSeriesId();
    List<CalendarEvent> seriesEvents = getEventsInSeries(seriesId);

    for (CalendarEvent e : seriesEvents) {
      updateSingleEventProperty(e, property, newValue);
    }
  }

  private void updateSingleEventProperty(CalendarEvent event, String property, String newValue) {
    CalendarEvent updatedEvent;

    switch (property.toLowerCase()) {
      case "subject":
        updatedEvent = event.withSubject(newValue);
        break;
      case "start":
        updatedEvent = event.withStartDateTime(LocalDateTime.parse(newValue));
        break;
      case "end":
        updatedEvent = event.withEndDateTime(LocalDateTime.parse(newValue));
        break;
      case "description":
        updatedEvent = event.withDescription(newValue);
        break;
      case "location":
        updatedEvent = event.withLocation(newValue);
        break;
      case "status":
        updatedEvent = event.withStatus(newValue);
        break;
      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }

    updateEvent(event, updatedEvent);
  }

  @Override
  public List<CalendarEvent> getEventsOnDate(LocalDate date) {
    return events.stream()
        .filter(e -> {
          LocalDate eventStart = e.getStartDateTime().toLocalDate();
          LocalDate eventEnd = e.getEndDateTime().toLocalDate();
          return !date.isBefore(eventStart)
              && !date.isAfter(eventEnd);
        })
        .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
        .collect(Collectors.toList());
  }

  @Override
  public List<CalendarEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    return events.stream()
        .filter(e -> {
          return e.getStartDateTime().isBefore(end)
              && e.getEndDateTime().isAfter(start);
        })
        .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
        .collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return events.stream()
        .anyMatch(e -> !dateTime.isBefore(e.getStartDateTime())
            && !dateTime.isAfter(e.getEndDateTime()));
  }

  @Override
  public List<CalendarEvent> getAllEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public List<CalendarEvent> getEventsInSeries(String seriesId) {
    if (seriesId == null) {
      return new ArrayList<>();
    }

    return events.stream()
        .filter(e -> seriesId.equals(e.getSeriesId()))
        .sorted((e1, e2) -> e1.getStartDateTime().compareTo(e2.getStartDateTime()))
        .collect(Collectors.toList());
  }

  @Override
  public CalendarAnalytics getAnalytics(LocalDate from, LocalDate to) {
    if (from == null || to == null) {
      throw new IllegalArgumentException("Dates cannot be null");
    }
    if (to.isBefore(from)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }

    long daysInRange = ChronoUnit.DAYS.between(from, to) + 1;

    Map<String, Integer> bySubject = new HashMap<>();
    Map<DayOfWeek, Integer> byWeekday = new HashMap<>();
    Map<Integer, Integer> byWeekIndex = new HashMap<>();
    Map<YearMonth, Integer> byMonth = new HashMap<>();
    Map<LocalDate, Integer> byDay = new HashMap<>();

    LocalDate d = from;
    int dayIndex = 0;
    while (!d.isAfter(to)) {
      byDay.put(d, 0);
      d = d.plusDays(1);
      dayIndex++;
    }

    int total = 0;
    int online = 0;
    int offline = 0;

    for (CalendarEvent event : events) {
      LocalDate startDate = event.getStartDateTime().toLocalDate();
      if (startDate.isBefore(from) || startDate.isAfter(to)) {
        continue;
      }

      total++;

      bySubject.merge(event.getSubject(), 1, Integer::sum);

      DayOfWeek dow = startDate.getDayOfWeek();
      byWeekday.merge(dow, 1, Integer::sum);

      long daysFromStart = ChronoUnit.DAYS.between(from, startDate);
      int weekIndex = (int) (daysFromStart / 7) + 1;
      byWeekIndex.merge(weekIndex, 1, Integer::sum);

      YearMonth ym = YearMonth.from(startDate);
      byMonth.merge(ym, 1, Integer::sum);

      byDay.merge(startDate, 1, Integer::sum);

      String loc = event.getLocation();
      if (loc != null && loc.equalsIgnoreCase("online")) {
        online++;
      } else {
        offline++;
      }
    }

    double avgPerDay = daysInRange > 0 ? (double) total / daysInRange : 0.0;

    LocalDate busiestDay = null;
    int busiestCount = -1;
    LocalDate leastBusyDay = null;
    int leastCount = Integer.MAX_VALUE;

    for (Map.Entry<LocalDate, Integer> entry : byDay.entrySet()) {
      LocalDate date = entry.getKey();
      int count = entry.getValue();
      if (count > busiestCount) {
        busiestCount = count;
        busiestDay = date;
      }
      if (count < leastCount) {
        leastCount = count;
        leastBusyDay = date;
      }
    }

    int totalWithLocation = online + offline;
    double onlinePct = totalWithLocation == 0
        ? 0.0
        : (100.0 * online) / totalWithLocation;

    return new CalendarAnalytics(
        from,
        to,
        total,
        bySubject,
        byWeekday,
        byWeekIndex,
        byMonth,
        avgPerDay,
        busiestDay,
        busiestCount,
        leastBusyDay,
        leastCount,
        online,
        offline,
        onlinePct
    );
  }
}