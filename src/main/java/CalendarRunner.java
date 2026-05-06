import controller.CalendarController;
import controller.GuiController;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import model.MultiCalendarModel;
import model.MultiCalendarModelImpl;
import view.CalendarGui;
import view.CalendarView;
import view.ConsoleView;


/**
 * Main entry point for the Calendar application.
 * Supports interactive, headless, and GUI modes.
 * Usage:
 * java -jar calendar.jar                          (GUI mode)
 * java -jar calendar.jar --mode interactive       (Interactive mode)
 * java -jar calendar.jar --mode headless commands.txt (Headless mode)
 */
public class CalendarRunner {

  /**
   * Entry point of the Calendar application.
   * Parses command-line arguments and launches appropriate mode.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      runGuiMode();
    } else {
      int exitCode = run(args);
      System.exit(exitCode);
    }
  }

  /**
   * Runs the calendar application and returns an exit code.
   * This method is package-private to allow testing without System.exit().
   *
   * @param args command-line arguments
   * @return exit code (0 for success, 1 for error)
   */
  static int run(String[] args) {
    if (args.length == 0) {
      return runGuiMode();
    }

    if (args.length < 2) {
      System.err.println("Usage: java CalendarRunner [--mode <interactive|headless> [filename]]");
      System.err.println("  GUI mode (default): java CalendarRunner");
      System.err.println("  Interactive mode: java CalendarRunner --mode interactive");
      System.err.println("  Headless mode: java CalendarRunner --mode headless <filename>");
      return 1;
    }

    String modeFlag = args[0];
    String mode = args[1];

    if (!modeFlag.equalsIgnoreCase("--mode")) {
      System.err.println("Error: First argument must be --mode");
      System.err.println("For GUI mode, run without arguments: java CalendarRunner");
      return 1;
    }

    MultiCalendarModel multiModel = new MultiCalendarModelImpl();
    CalendarView view = new ConsoleView(new InputStreamReader(System.in), System.out);
    CalendarController controller = new CalendarController(multiModel, view);

    if (mode.equalsIgnoreCase("interactive")) {
      runInteractiveMode(controller, view);
      return 0;
    } else if (mode.equalsIgnoreCase("headless")) {
      if (args.length < 3) {
        System.err.println("Error: Headless mode requires a filename");
        return 1;
      }
      String filename = args[2];
      return runHeadlessMode(controller, view, filename);
    } else {
      System.err.println("Error: Mode must be 'interactive' or 'headless'");
      System.err.println("For GUI mode, run without arguments: java CalendarRunner");
      return 1;
    }
  }

  /**
   * Runs the application in GUI mode.
   * Opens a graphical window for interactive calendar management.
   *
   * @return exit code (0 for success)
   */
  private static int runGuiMode() {
    try {
      javax.swing.UIManager.setLookAndFeel(
          javax.swing.UIManager.getSystemLookAndFeelClassName()
      );
    } catch (Exception e) {
      System.err.println("Warning: Could not set system look and feel");
    }

    javax.swing.SwingUtilities.invokeLater(() -> {
      MultiCalendarModel multiModel = new MultiCalendarModelImpl();
      GuiController controller = new GuiController(multiModel);
      CalendarGui gui = new CalendarGui(controller);
      gui.display();
    });

    return 0;
  }

  /**
   * Runs the application in interactive mode.
   * User enters commands one at a time until 'exit' is typed.
   */
  private static void runInteractiveMode(CalendarController controller, CalendarView view) {
    view.displayMessage("Calendar Application - Interactive Mode");
    view.displayMessage("Type 'exit' to quit");
    view.displayMessage("");

    while (true) {
      String command = view.getUserInput();

      if (command.trim().equalsIgnoreCase("exit")) {
        view.displayMessage("Goodbye!");
        break;
      }

      controller.processCommand(command);
    }
  }

  /**
   * Runs the application in headless mode.
   * Reads commands from a file and executes them sequentially.
   *
   * @param controller the calendar controller
   * @param view       the calendar view
   * @param filename   the command file path
   * @return exit code (0 for success, 1 for error)
   */
  private static int runHeadlessMode(CalendarController controller, CalendarView view,
                                     String filename) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      String line;
      boolean foundExit = false;

      while ((line = reader.readLine()) != null) {
        String command = line.trim();

        if (command.isEmpty() || command.startsWith("#")) {
          continue;
        }

        if (command.equalsIgnoreCase("exit")) {
          foundExit = true;
          view.displayMessage("Exiting...");
          break;
        }

        controller.processCommand(command);
      }

      if (!foundExit) {
        view.displayError("File ended without 'exit' command");
        return 1;
      }

      return 0;
    } catch (IOException e) {
      view.displayError("Failed to read command file: " + e.getMessage());
      return 1;
    }
  }
}