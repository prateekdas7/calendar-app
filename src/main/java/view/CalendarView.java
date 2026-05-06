package view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.CalendarEvent;

/**
 * Interface for the calendar view.
 * Defines methods for displaying calendar information to the user.
 */
public interface CalendarView {

  /**
   * Displays a message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param error the error message to display
   */
  void displayError(String error);

  /**
   * Displays events on a specific date.
   *
   * @param date   the date to display events for
   * @param events the list of events on that date
   */
  void displayEventsOnDate(LocalDate date, List<CalendarEvent> events);

  /**
   * Displays events in a date/time range.
   *
   * @param start  the start date/time
   * @param end    the end date/time
   * @param events the list of events in the range
   */
  void displayEventsInRange(LocalDateTime start, LocalDateTime end,
                            List<CalendarEvent> events);

  /**
   * Displays the busy/available status.
   *
   * @param dateTime the specific date/time
   * @param isBusy   true if busy, false if free
   */
  void displayStatus(LocalDateTime dateTime, boolean isBusy);

  /**
   * Exports events to a CSV file.
   *
   * @param filename the name of the CSV file to export to
   * @param events   the list of events to export
   */
  void exportToCsv(String filename, List<CalendarEvent> events);

  /**
   * Exports events to the distinguished type of file.
   * The file's type can be either.
   *
   * @param filename the name of the xxx file to export to
   * @param events   the list of events to export
   */
  void exportAuto(String filename, List<CalendarEvent> events);

  /**
   * Prompts the user for input (interactive mode only).
   *
   * @return the user's input string
   */
  String getUserInput();
}
