package model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of MultiCalendarModel. Uses a map-based representation for storing calendars.
 * Supports each calendar be in its only timezone while the default is EST (NewYork).
 */
public class MultiCalendarModelImpl implements MultiCalendarModel {

  private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/New_York");

  /**
   * This helper holds a calendar's state: event model, timezone, and calendar name.
   */
  private static final class CalendarRecord {

    CalendarModel model;
    ZoneId zone;
    String calendarName;

    /**
     * Creates a record for a single calendar.
     *
     * @param model        event model.
     * @param zone         calendar's timezone.
     * @param calendarName calendar's name.
     */
    CalendarRecord(CalendarModel model, ZoneId zone, String calendarName) {
      this.model = model;
      this.zone = zone;
      this.calendarName = calendarName;
    }
  }

  private final Map<String, CalendarRecord> calendars = new HashMap<>();

  /**
   * Standardizes the calendar name as a non-emtpy, case-insensitive key. This means that two
   * calendars with names "CALENDAR" and "calendar" are considering two duplicate calendars.
   *
   * @param name unfurnished calendar name.
   * @return standardized calendar name.
   */
  private static String keyOf(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null");
    }

    String key = name.trim().toLowerCase(Locale.ROOT);
    if (key.isEmpty()) {
      throw new IllegalArgumentException("Name cannot be empty");
    }

    return key;
  }

  @Override
  public void createCalendar(String calendarName) {
    createCalendar(calendarName, DEFAULT_ZONE);
  }

  @Override
  public void createCalendar(String calendarName, ZoneId zone) {
    String key = keyOf(calendarName);
    if (calendars.containsKey(key)) {
      throw new IllegalArgumentException("Calendar with name " + key + " already exists.");
    }

    calendars.put(key, new CalendarRecord(new CalendarModelImpl(), zone, calendarName));
  }

  @Override
  public void deleteCalendar(String calendarName) {
    String key = keyOf(calendarName);
    if (!calendars.containsKey(key)) {
      throw new IllegalArgumentException("Calendar with name " + key + " does not exist.");
    }

    calendars.remove(key);
  }

  @Override
  public void renameCalendar(String oldCalendarName, String newCalendarName) {
    String oldKey = keyOf(oldCalendarName);
    String newKey = keyOf(newCalendarName);
    if (!calendars.containsKey(oldKey)) {
      throw new IllegalArgumentException("Calendar with name " + oldKey + " does not exist.");
    }

    CalendarRecord record = calendars.get(oldKey);
    if (oldKey.equals(newKey)) {
      record.calendarName = newCalendarName.trim();
      return;
    }

    if (calendars.containsKey(newKey)) {
      throw new IllegalArgumentException("Calendar with name " + newKey + " already exists.");
    }

    calendars.remove(oldKey);
    record.calendarName = newCalendarName.trim();
    calendars.put(newKey, record);
  }

  @Override
  public CalendarModel getCalendar(String calendarName) {
    CalendarRecord record = calendars.get(keyOf(calendarName));
    if (record == null) {
      throw new IllegalArgumentException("Calendar with name " + calendarName + " does not exist.");
    }

    return record.model;
  }

  @Override
  public boolean existCalendar(String calendarName) {
    return calendars.containsKey(keyOf(calendarName));
  }

  @Override
  public ZoneId getTimeZone(String calendarName) {
    CalendarRecord record = calendars.get(keyOf(calendarName));
    if (record == null) {
      throw new IllegalArgumentException("Calendar with name " + calendarName + " does not exist.");
    }

    return record.zone;
  }

  @Override
  public void setTimeZone(String calendarName, ZoneId newZone) {
    CalendarRecord record = calendars.get(keyOf(calendarName));
    if (record == null) {
      throw new IllegalArgumentException("Calendar with name " + calendarName + " does not exist.");
    }

    ZoneId oldZone = record.zone;
    if (Objects.equals(oldZone, newZone)) {
      return;
    }

    List<CalendarEvent> events = record.model.getAllEvents();
    if (events.isEmpty()) {
      record.zone = newZone;
      return;
    }

    List<CalendarEvent> migrated = new ArrayList<CalendarEvent>(events.size());
    for (CalendarEvent event : events) {
      ZonedDateTime oldStartTime = event.getStartDateTime().atZone(oldZone);
      ZonedDateTime oldEndTime = event.getEndDateTime().atZone(oldZone);

      ZonedDateTime newStartTime = oldStartTime.withZoneSameInstant(newZone);
      ZonedDateTime newEndTime = oldEndTime.withZoneSameInstant(newZone);

      CalendarEvent newEvent = new CalendarEvent(
          event.getSubject(),
          newStartTime.toLocalDateTime(),
          newEndTime.toLocalDateTime(),
          event.getDescription(),
          event.getLocation(),
          event.getStatus(),
          event.getSeriesId()
      );
      migrated.add(newEvent);
    }

    CalendarModel newModel = new CalendarModelImpl();
    newModel.addEvents(migrated);

    record.model = newModel;
    record.zone = newZone;
  }

  /**
   * This helper method facilitates copying the source event(s) to a target calendar by
   * shifting the source start date/time to the target start date/time.
   *
   * @param sourceEvent       the source event.
   * @param shift             duration needed to be shifted.
   * @param newSeriesIdOrNull new seriesId or null (only if not in a series)
   * @return the copied event for the target calendar.
   */
  private static CalendarEvent copyEventAndShiftTime(CalendarEvent sourceEvent, Duration shift,
                                                     String newSeriesIdOrNull) {
    LocalDateTime newStartTime = sourceEvent.getStartDateTime().plus(shift);
    Duration eventDuration = Duration.between(
        sourceEvent.getStartDateTime(), sourceEvent.getEndDateTime());
    LocalDateTime newEndTime = newStartTime.plus(eventDuration);

    return new CalendarEvent(
        sourceEvent.getSubject(),
        newStartTime,
        newEndTime,
        sourceEvent.getDescription(),
        sourceEvent.getLocation(),
        sourceEvent.getStatus(),
        newSeriesIdOrNull
    );
  }

  @Override
  public void copyEvent(String sourceCalendarName, String sourceSubject, LocalDateTime sourceStart,
                        String targetCalendarName, LocalDateTime targetStart) {
    CalendarRecord sourceCalRecord = calendars.get(keyOf(sourceCalendarName));
    if (sourceCalRecord == null) {
      throw new IllegalArgumentException(
          "Calendar with name " + sourceCalendarName + " does not exist.");
    }

    CalendarRecord targetCalRecord = calendars.get(keyOf(targetCalendarName));
    if (targetCalRecord == null) {
      throw new IllegalArgumentException(
          "Calendar with name " + targetCalendarName + " does not exist.");
    }

    List<CalendarEvent> sourceEvents = sourceCalRecord.model.findEvents(sourceSubject, sourceStart);
    if (sourceEvents.isEmpty()) {
      throw new IllegalArgumentException(
          "Source event " + sourceSubject + " @ " + sourceStart + " cannot be found.");
    }
    CalendarEvent sourceEvent = sourceEvents.get(0);

    Duration shift = Duration.between(sourceEvent.getStartDateTime(), targetStart);

    List<CalendarEvent> copiedEvents = new ArrayList<>();
    if (sourceEvent.isPartOfSeries()) {
      String newSeriesId = UUID.randomUUID().toString();
      List<CalendarEvent> series =
          sourceCalRecord.model.getEventsInSeries(sourceEvent.getSeriesId());

      for (CalendarEvent event : series) {
        copiedEvents.add(copyEventAndShiftTime(event, shift, newSeriesId));
      }

    } else {
      copiedEvents.add(copyEventAndShiftTime(sourceEvent, shift, null));
    }

    targetCalRecord.model.addEvents(copiedEvents);
  }

  private void copyEventsHelper(String sourceCalendarName,
                                LocalDateTime srcStartTime, LocalDateTime srcEndTime,
                                String targetCalendarName, LocalDate targetDate) {
    CalendarRecord sourceCalRecord = calendars.get(keyOf(sourceCalendarName));
    if (sourceCalRecord == null) {
      throw new IllegalArgumentException(
          "Calendar with name " + sourceCalendarName + " does not exist.");
    }

    CalendarRecord targetCalRecord = calendars.get(keyOf(targetCalendarName));
    if (targetCalRecord == null) {
      throw new IllegalArgumentException(
          "Calendar with name " + targetCalendarName + " does not exist.");
    }

    ZoneId srcZone = sourceCalRecord.zone;
    ZoneId trgZone = targetCalRecord.zone;

    List<CalendarEvent> events = sourceCalRecord.model.getEventsInRange(srcStartTime, srcEndTime);
    if (events.isEmpty()) {
      return;
    }

    LocalDate srcStartDate = srcStartTime.toLocalDate();
    LocalDate trgStartDate = targetDate;
    long dayShift = java.time.temporal.ChronoUnit.DAYS.between(srcStartDate, trgStartDate);

    Map<String, String> seriesMap = new HashMap<>();
    List<CalendarEvent> copiedEvents = new ArrayList<>(events.size());

    for (CalendarEvent event : events) {
      LocalDateTime srcEventStart = event.getStartDateTime();
      LocalDateTime srcEventEnd = event.getEndDateTime();

      LocalDateTime shiftedStart = srcEventStart.plusDays(dayShift);
      LocalDateTime shiftedEnd = srcEventEnd.plusDays(dayShift);

      ZonedDateTime zonedStart = shiftedStart.atZone(srcZone);
      ZonedDateTime zonedEnd = shiftedEnd.atZone(srcZone);

      ZonedDateTime trgZonedStart = zonedStart.withZoneSameInstant(trgZone);
      ZonedDateTime trgZonedEnd = zonedEnd.withZoneSameInstant(trgZone);

      String newSeriesId = null;
      if (event.isPartOfSeries()) {
        newSeriesId = seriesMap
            .computeIfAbsent(event.getSeriesId(), k -> UUID.randomUUID().toString());
      }

      copiedEvents.add(new CalendarEvent(
          event.getSubject(),
          trgZonedStart.toLocalDateTime(),
          trgZonedEnd.toLocalDateTime(),
          event.getDescription(),
          event.getLocation(),
          event.getStatus(),
          newSeriesId
      ));
    }

    targetCalRecord.model.addEvents(copiedEvents);

  }

  @Override
  public void copyEventsOn(String sourceCalendarName, LocalDate sourceDate,
                           String targetCalendarName, LocalDate targetDate) {
    LocalDateTime srcStartTime = sourceDate.atStartOfDay();
    LocalDateTime srcEndTime = sourceDate.atStartOfDay().plusDays(1);
    copyEventsHelper(sourceCalendarName, srcStartTime, srcEndTime, targetCalendarName, targetDate);
  }

  @Override
  public void copyEventsInBetween(String sourceCalendarName,
                                  LocalDate srcFromDate, LocalDate srcToDate,
                                  String targetCalendarName, LocalDate targetDate) {
    LocalDateTime srcStartTime = srcFromDate.atStartOfDay();
    LocalDateTime srcEndTime = srcToDate.atStartOfDay().plusDays(1);
    copyEventsHelper(sourceCalendarName, srcStartTime, srcEndTime, targetCalendarName, targetDate);
  }
}
