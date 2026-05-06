package view;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newBufferedWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import model.CalendarEvent;

/**
 * Console-based implementation of CalendarView.
 */
public class ConsoleView implements CalendarView {
  private final Scanner scanner;
  private final Appendable out;
  private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
  private final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  /**
   * Constructs a ConsoleView using the given input and output streams.
   *
   * @param input  the input source (e.g., System.in)
   * @param output the output destination (e.g., System.out)
   */
  public ConsoleView(Readable input, Appendable output) {
    this.scanner = new Scanner(input);
    this.out = output;
  }

  @Override
  public void displayMessage(String message) {
    try {
      out.append(message).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Error writing to output", e);
    }
  }

  @Override
  public void displayError(String error) {
    try {
      out.append("ERROR: ").append(error).append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Error writing to output", e);
    }
  }

  @Override
  public void displayEventsOnDate(LocalDate date, List<CalendarEvent> events) {
    try {
      if (events.isEmpty()) {
        out.append("No events scheduled on ")
            .append(date.format(dateFormatter))
            .append("\n");
        return;
      }

      out.append("Events on ").append(date.format(dateFormatter)).append(":\n");
      for (CalendarEvent event : events) {
        out.append("- ").append(event.getSubject())
            .append(" from ").append(event.getStartDateTime().format(timeFormatter))
            .append(" to ").append(event.getEndDateTime().format(timeFormatter));

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          out.append(" at ").append(event.getLocation());
        }
        out.append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing to output", e);
    }
  }

  @Override
  public void displayEventsInRange(LocalDateTime start, LocalDateTime end,
                                   List<CalendarEvent> events) {
    try {
      if (events.isEmpty()) {
        out.append("No events scheduled in the specified range\n");
        return;
      }

      out.append("Events from ").append(start.format(dateTimeFormatter))
          .append(" to ").append(end.format(dateTimeFormatter))
          .append(":\n");

      for (CalendarEvent event : events) {
        out.append("- ").append(event.getSubject())
            .append(" starting on ")
            .append(event.getStartDateTime().toLocalDate().format(dateFormatter))
            .append(" at ")
            .append(event.getStartDateTime().format(timeFormatter))
            .append(", ending on ")
            .append(event.getEndDateTime().toLocalDate().format(dateFormatter))
            .append(" at ")
            .append(event.getEndDateTime().format(timeFormatter));

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          out.append(" at ").append(event.getLocation());
        }
        out.append("\n");
      }
    } catch (IOException e) {
      throw new RuntimeException("Error writing to output", e);
    }
  }

  @Override
  public void displayStatus(LocalDateTime dateTime, boolean isBusy) {
    try {
      out.append("Status on ")
          .append(dateTime.format(dateTimeFormatter))
          .append(": ")
          .append(isBusy ? "busy" : "available")
          .append("\n");
    } catch (IOException e) {
      throw new RuntimeException("Error writing to output", e);
    }
  }

  @Override
  public void exportToCsv(String filename, List<CalendarEvent> events) {
    try {
      Path filePath = Paths.get(filename);

      try (BufferedWriter writer = newBufferedWriter(filePath)) {
        // Write CSV header (Google Calendar format)
        writer.write(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
                + "Description,Location,Private\n"
        );

        // Write events
        for (CalendarEvent event : events) {
          writer.write(escapeCsv(event.getSubject()));
          writer.write(",");
          writer.write(event.getStartDateTime().toLocalDate()
              .format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
          writer.write(",");
          writer.write(event.getStartDateTime()
              .format(DateTimeFormatter.ofPattern("hh:mm a")));
          writer.write(",");
          writer.write(event.getEndDateTime().toLocalDate()
              .format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
          writer.write(",");
          writer.write(event.getEndDateTime()
              .format(DateTimeFormatter.ofPattern("hh:mm a")));
          writer.write(",");
          writer.write("False"); // All Day Event
          writer.write(",");
          writer.write(escapeCsv(
              event.getDescription() != null ? event.getDescription() : ""));
          writer.write(",");
          writer.write(escapeCsv(
              event.getLocation() != null ? event.getLocation() : ""));
          writer.write(",");
          writer.write("private".equalsIgnoreCase(event.getStatus())
              ? "True"
              : "False");
          writer.write("\n");
        }
      }

      Path absolutePath = filePath.toAbsolutePath();
      displayMessage("Calendar exported to: " + absolutePath);

    } catch (IOException e) {
      displayError("Failed to export calendar: " + e.getMessage());
    }
  }

  private String escapeCsv(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  @Override
  public void exportAuto(String filename, List<CalendarEvent> events) {
    if (filename == null || filename.isEmpty()) {
      displayError("Missing file name for export.");
      return;
    }

    String filenameLowercase = filename.toLowerCase();
    if (filenameLowercase.endsWith(".csv")) {
      exportToCsv(filename, events);
      return;
    }

    if (filenameLowercase.endsWith(".ics") || filenameLowercase.endsWith(".ical")) {
      exportToIcal(filename, events);
      return;
    }

    displayError("Unsupported file format: " + filenameLowercase);
  }

  private void exportToIcal(String filename, List<CalendarEvent> events) {
    try {
      String crlf = "\r\n";
      StringBuilder sb = new StringBuilder(4096);

      sb.append("BEGIN:VCALENDAR").append(crlf);
      sb.append("VERSION:2.0").append(crlf);
      sb.append("PRODID:-//CalendarApp//ConsoleView//EN").append(crlf);

      for (CalendarEvent event : events) {
        sb.append("BEGIN:VEVENT").append(crlf);

        sb.append("UID:").append(UUID.randomUUID()).append(crlf);
        sb.append("DTSTAMP:").append(formatUtc(Instant.now())).append(crlf);

        ZonedDateTime zonedStartTime = event.getStartDateTime()
            .atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime zonedEndTime = event.getEndDateTime()
            .atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);

        sb.append("DTSTART:").append(formatUtc(zonedStartTime.toInstant())).append(crlf);
        sb.append("DTEND:").append(formatUtc(zonedEndTime.toInstant())).append(crlf);

        sb.append("SUMMARY:").append(escapeIcal(event.getSubject())).append(crlf);

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          sb.append("LOCATION:").append(escapeIcal(event.getLocation())).append(crlf);
        }

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
          sb.append("DESCRIPTION:").append(escapeIcal(event.getDescription())).append(crlf);
        }

        sb.append("END:VEVENT").append(crlf);
      }

      sb.append("END:VCALENDAR").append(crlf);

      Path filePath = Paths.get(filename);
      createDirectories(filePath.toAbsolutePath().getParent());

      try (BufferedWriter writer = newBufferedWriter(filePath)) {
        writer.write(sb.toString());
      }

      Path absolutePath = filePath.toAbsolutePath();
      displayMessage("Calendar exported to: " + absolutePath);

    } catch (IOException e) {
      displayError("Failed to export calendar iCal: " + e.getMessage());
    }
  }

  private static final DateTimeFormatter ICS_UTC =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

  private static String formatUtc(Instant instant) {
    return ICS_UTC.format(instant);
  }

  private static String escapeIcal(String string) {
    if (string == null) {
      return "";
    }

    return string.replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("\r\n", "\\n")
        .replace("\n", "\\n")
        .replace("\r", "");
  }

  @Override
  public String getUserInput() {
    try {
      out.append("> ");
    } catch (IOException e) {
      throw new RuntimeException("Error writing to output", e);
    }

    if (scanner.hasNextLine()) {
      return scanner.nextLine();
    }
    return "exit";
  }
}
