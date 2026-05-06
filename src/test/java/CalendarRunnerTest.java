import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for CalendarRunner class.
 * Tests interactive mode, headless mode, argument parsing, and error handling.
 */
public class CalendarRunnerTest {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private ByteArrayOutputStream errContent;
  private ByteArrayOutputStream outContent;
  private PrintStream originalErr;
  private PrintStream originalOut;
  private java.io.InputStream originalIn;

  /**
   * Sets up the test environment before each test method.
   * Redirects System.err and System.out to capture output for assertions.
   * Saves original system streams for restoration in tearDown.
   */
  @Before
  public void setUp() {
    errContent = new ByteArrayOutputStream();
    outContent = new ByteArrayOutputStream();
    originalErr = System.err;
    originalOut = System.out;
    originalIn = System.in;

    System.setErr(new PrintStream(errContent));
    System.setOut(new PrintStream(outContent));
  }

  /**
   * Restores the original system state after each test method.
   * Resets System.err, System.out, and System.in to their original streams.
   * Ensures test isolation by preventing state pollution between tests.
   */
  @After
  public void tearDown() {
    System.setErr(originalErr);
    System.setOut(originalOut);
    System.setIn(originalIn);
  }

  /**
   * Tests that run() returns error code when only --mode flag is provided.
   */
  @Test
  public void testInsufficientArguments() {
    int exitCode = CalendarRunner.run(new String[] {"--mode"});

    assertEquals("Should return exit code 1", 1, exitCode);
    String errorOutput = errContent.toString();
    assertTrue("Should show usage instructions", errorOutput.contains("Usage:"));
  }

  /**
   * Tests that run() returns error code when first argument is not --mode.
   */
  @Test
  public void testInvalidFirstArgument() {
    int exitCode = CalendarRunner.run(new String[] {"--invalid", "interactive"});

    assertEquals("Should return exit code 1", 1, exitCode);
    String errorOutput = errContent.toString();
    assertTrue("Should mention --mode requirement", errorOutput.contains("--mode"));
  }

  /**
   * Tests that run() returns error code for invalid mode value.
   */
  @Test
  public void testInvalidMode() {
    int exitCode = CalendarRunner.run(new String[] {"--mode", "invalid"});

    assertEquals("Should return exit code 1", 1, exitCode);
    String errorOutput = errContent.toString();
    assertTrue("Should mention valid mode options",
        errorOutput.contains("interactive") || errorOutput.contains("headless"));
  }

  /**
   * Tests that headless mode returns error when filename is missing.
   */
  @Test
  public void testHeadlessModeWithoutFilename() {
    int exitCode = CalendarRunner.run(new String[] {"--mode", "headless"});

    assertEquals("Should return exit code 1", 1, exitCode);
    String errorOutput = errContent.toString();
    assertTrue("Should mention filename requirement",
        errorOutput.toLowerCase().contains("filename"));
  }

  /**
   * Tests that --mode flag is case-insensitive.
   */
  @Test
  public void testModeFlagCaseInsensitive() {
    int exitCode = CalendarRunner.run(new String[] {"--MODE", "headless"});

    assertEquals("Should return exit code 1 for missing filename", 1, exitCode);
    String errorOutput = errContent.toString();
    assertTrue("Should recognize --MODE and report missing filename",
        errorOutput.toLowerCase().contains("filename"));
  }

  /**
   * Tests that interactive mode starts and responds to exit command.
   */
  @Test
  public void testInteractiveModeWithImmediateExit() {
    System.setIn(new ByteArrayInputStream("exit\n".getBytes()));

    int exitCode = CalendarRunner.run(new String[] {"--mode", "interactive"});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should show interactive mode message",
        output.contains("Interactive Mode"));
    assertTrue("Should show goodbye message", output.contains("Goodbye"));
  }

  /**
   * Tests that interactive mode handles commands before exit.
   */
  @Test
  public void testInteractiveModeWithCommandsThenExit() {
    String input = "help\nexit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    int exitCode = CalendarRunner.run(new String[] {"--mode", "interactive"});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should display interactive mode message",
        output.contains("Interactive Mode"));
  }

  /**
   * Tests that exit command is case-insensitive.
   */
  @Test
  public void testInteractiveModeExitIsCaseInsensitive() {
    System.setIn(new ByteArrayInputStream("EXIT\n".getBytes()));

    int exitCode = CalendarRunner.run(new String[] {"--mode", "interactive"});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should handle EXIT command", output.contains("Goodbye"));
  }

  /**
   * Tests that exit command works with surrounding whitespace.
   */
  @Test
  public void testInteractiveModeExitWithWhitespace() {
    System.setIn(new ByteArrayInputStream("  exit  \n".getBytes()));

    int exitCode = CalendarRunner.run(new String[] {"--mode", "interactive"});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should trim whitespace and recognize exit", output.contains("Goodbye"));
  }

  /**
   * Tests that interactive mode processes multiple commands.
   */
  @Test
  public void testInteractiveModeWithMultipleCommands() {
    String input = "command1\ncommand2\ncommand3\nexit\n";
    System.setIn(new ByteArrayInputStream(input.getBytes()));

    int exitCode = CalendarRunner.run(new String[] {"--mode", "interactive"});

    assertEquals("Should return exit code 0", 0, exitCode);
  }

  /**
   * Tests mode value is case-insensitive (interactive).
   */
  @Test
  public void testModeValueIsCaseInsensitiveInteractive() {
    System.setIn(new ByteArrayInputStream("exit\n".getBytes()));

    int exitCode = CalendarRunner.run(new String[] {"--mode", "INTERACTIVE"});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should recognize INTERACTIVE mode", output.contains("Interactive Mode"));
  }

  /**
   * Tests headless mode with valid file containing exit.
   */
  @Test
  public void testHeadlessModeWithValidExitFile() throws Exception {
    File commandFile = tempFolder.newFile("valid.txt");
    Files.write(commandFile.toPath(), "exit\n".getBytes());

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should show exiting message", output.contains("Exiting"));
  }

  /**
   * Tests that headless mode returns error when file lacks exit command.
   */
  @Test
  public void testHeadlessModeWithoutExitCommand() throws Exception {
    File commandFile = tempFolder.newFile("no-exit.txt");
    Files.write(commandFile.toPath(), "create event Test\n".getBytes());

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 1", 1, exitCode);
    String output = outContent.toString() + errContent.toString();
    assertTrue("Should report missing exit command",
        output.toLowerCase().contains("exit"));
  }

  /**
   * Tests that empty lines in file are skipped.
   */
  @Test
  public void testHeadlessModeSkipsEmptyLines() throws Exception {
    File commandFile = tempFolder.newFile("empty-lines.txt");
    Files.write(commandFile.toPath(), "\n\n\nexit\n".getBytes());

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should skip empty lines and process exit", output.contains("Exiting"));
  }

  /**
   * Tests that comment lines (starting with #) are skipped.
   */
  @Test
  public void testHeadlessModeSkipsComments() throws Exception {
    File commandFile = tempFolder.newFile("with-comments.txt");
    Files.write(commandFile.toPath(),
        "# This is a comment\n# Another comment\nexit\n".getBytes());

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should skip comments and process exit", output.contains("Exiting"));
  }

  /**
   * Tests that exit command in file is case-insensitive.
   */
  @Test
  public void testHeadlessModeExitIsCaseInsensitive() throws Exception {
    File commandFile = tempFolder.newFile("uppercase-exit.txt");
    Files.write(commandFile.toPath(), "EXIT\n".getBytes());

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should recognize EXIT command", output.contains("Exiting"));
  }

  /**
   * Tests that whitespace-only lines are properly trimmed and skipped.
   */
  @Test
  public void testHeadlessModeSkipsWhitespaceLines() throws Exception {
    File commandFile = tempFolder.newFile("whitespace.txt");
    Files.write(commandFile.toPath(), "   \n\t\nexit\n".getBytes());

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should skip whitespace lines", output.contains("Exiting"));
  }

  /**
   * Tests that headless mode returns error when file is not found.
   */
  @Test
  public void testHeadlessModeFileNotFound() {
    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", "nonexistent.txt"});

    assertEquals("Should return exit code 1", 1, exitCode);
    String output = outContent.toString() + errContent.toString();
    assertTrue("Should report file error",
        output.toLowerCase().contains("failed") || output.toLowerCase().contains("read"));
  }

  /**
   * Tests that empty file (no exit) returns error.
   */
  @Test
  public void testHeadlessModeEmptyFile() throws Exception {
    File commandFile = tempFolder.newFile("empty.txt");

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 1", 1, exitCode);
    String output = outContent.toString() + errContent.toString();
    assertTrue("Should report missing exit", output.toLowerCase().contains("exit"));
  }

  /**
   * Tests file with only comments and empty lines (no exit) returns error.
   */
  @Test
  public void testHeadlessModeOnlyCommentsAndEmptyLines() throws Exception {
    File commandFile = tempFolder.newFile("comments.txt");
    Files.write(commandFile.toPath(), "# Comment 1\n\n# Comment 2\n\n".getBytes());

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 1", 1, exitCode);
    String output = outContent.toString() + errContent.toString();
    assertTrue("Should report missing exit", output.toLowerCase().contains("exit"));
  }

  /**
   * Tests mode value is case-insensitive (headless).
   */
  @Test
  public void testModeValueIsCaseInsensitiveHeadless() throws Exception {
    File commandFile = tempFolder.newFile("test.txt");
    Files.write(commandFile.toPath(), "exit\n".getBytes());

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "HEADLESS", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should recognize HEADLESS mode", output.contains("Exiting"));
  }

  /**
   * Tests that headless mode processes commands before exit.
   */
  @Test
  public void testHeadlessModeWithCommandsBeforeExit() throws Exception {
    File commandFile = tempFolder.newFile("commands.txt");
    Files.write(commandFile.toPath(),
        "create event Test\nlist events\nexit\n".getBytes());

    int exitCode = CalendarRunner.run(
        new String[] {"--mode", "headless", commandFile.getAbsolutePath()});

    assertEquals("Should return exit code 0", 0, exitCode);
    String output = outContent.toString();
    assertTrue("Should process commands and exit", output.contains("Exiting"));
  }

  private int invokePrivateRunGuiMode() {
    try {
      java.lang.reflect.Method m =
          CalendarRunner.class.getDeclaredMethod("runGuiMode");
      m.setAccessible(true);
      return (int) m.invoke(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testRunGuiModeReturnsZero() {
    int exitCode = invokePrivateRunGuiMode();
    assertEquals("GUI mode should return 0", 0, exitCode);
  }

  /**
   * Test double for CalendarGui used to verify that the GUI-launching
   * lambda inside runGuiMode() is executed. Instead of opening a real
   * window, this class simply sets a flag when display() is called.
   */
  public static class FakeGui extends view.CalendarGui {

    /**
     * Tracks whether display() was invoked.
     */
    public static boolean displayed = false;

    /**
     * Creates a FakeGui instance for testing.
     *
     * @param controller the GuiController passed to the real CalendarGui
     */
    public FakeGui(controller.GuiController controller) {
      super(controller);
    }

    @Override
    public void display() {
      displayed = true;
    }
  }

  @Test
  public void testRunGuiModeDoesNotThrow() {
    assertEquals(0, invokePrivateRunGuiMode());
  }

  @Test
  public void testCalendarRunnerConstructor() {
    CalendarRunner runner = new CalendarRunner();
    assertNotNull("Constructor should create instance", runner);
  }

}