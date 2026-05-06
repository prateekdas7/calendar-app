package model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single calendar event with subject, start/end times, and optional properties.
 * Immutable class to ensure thread safety and prevent unintended modifications.
 */
public class CalendarEvent {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final String location;
  private final String status;
  private final String seriesId;

  /**
   * Constructor for CalendarEvent.
   *
   * @param subject       the event subject (required)
   * @param startDateTime the start date and time (required)
   * @param endDateTime   the end date and time (can be null for all-day events)
   * @param description   optional description
   * @param location      optional location
   * @param status        optional status (public/private)
   * @param seriesId      optional series identifier
   */
  public CalendarEvent(String subject, LocalDateTime startDateTime,
                       LocalDateTime endDateTime, String description,
                       String location, String status, String seriesId) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start date/time cannot be null");
    }

    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime != null ? endDateTime :
        startDateTime.withHour(17).withMinute(0);
    this.description = description;
    this.location = location;
    this.status = status != null ? status : "public";
    this.seriesId = seriesId;

    if (this.endDateTime.isBefore(this.startDateTime)) {
      throw new IllegalArgumentException("End date/time cannot be before start date/time");
    }
  }


  public String getSubject() {
    return subject;
  }

  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  public LocalDateTime getEndDateTime() {
    return endDateTime;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public String getStatus() {
    return status;
  }

  public String getSeriesId() {
    return seriesId;
  }

  public boolean isPartOfSeries() {
    return seriesId != null;
  }

  /**
   * Creates a copy of this event with a new subject.
   */
  public CalendarEvent withSubject(String newSubject) {
    return new CalendarEvent(newSubject, startDateTime, endDateTime,
        description, location, status, seriesId);
  }

  /**
   * Creates a copy of this event with a new start time.
   */
  public CalendarEvent withStartDateTime(LocalDateTime newStart) {
    return new CalendarEvent(subject, newStart, endDateTime,
        description, location, status, null);
  }

  /**
   * Creates a copy of this event with a new end time.
   */
  public CalendarEvent withEndDateTime(LocalDateTime newEnd) {
    return new CalendarEvent(subject, startDateTime, newEnd,
        description, location, status, seriesId);
  }

  /**
   * Creates a copy of this event with a new description.
   */
  public CalendarEvent withDescription(String newDescription) {
    return new CalendarEvent(subject, startDateTime, endDateTime,
        newDescription, location, status, seriesId);
  }

  /**
   * Creates a copy of this event with a new location.
   */
  public CalendarEvent withLocation(String newLocation) {
    return new CalendarEvent(subject, startDateTime, endDateTime,
        description, newLocation, status, seriesId);
  }

  /**
   * Creates a copy of this event with a new status.
   */
  public CalendarEvent withStatus(String newStatus) {
    return new CalendarEvent(subject, startDateTime, endDateTime,
        description, location, newStatus, seriesId);
  }

  /**
   * Creates a copy of this event with a new series ID.
   */
  public CalendarEvent withSeriesId(String newSeriesId) {
    return new CalendarEvent(subject, startDateTime, endDateTime,
        description, location, status, newSeriesId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CalendarEvent that = (CalendarEvent) o;
    return Objects.equals(subject, that.subject)
        && Objects.equals(startDateTime, that.startDateTime)
        && Objects.equals(endDateTime, that.endDateTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  @Override
  public String toString() {
    return subject
        + " starting on " + startDateTime
        + ", ending on " + endDateTime
        + (location != null && !location.isEmpty()
        ? " at " + location
        : "");
  }
}