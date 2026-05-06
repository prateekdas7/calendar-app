package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import controller.CalendarController;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import view.CalendarView;

/**
 * Unit tests for CalendarAnalytics.
 * and controller dashboard behavior.
 */
public class CalendarAnalyticsTest {

  private CalendarModelImpl model;

  /**
   * Sets up a CalendarModelImpl object for testing.
   */
  @Before
  public void setUp() {
    model = new CalendarModelImpl();
  }

  /**
   * Helper to create a simple non-recurring, non–all-day event.
   */
  private CalendarEvent createEvent(String subject,
                                    String date,
                                    String startTime,
                                    String endTime,
                                    String location) {
    LocalDateTime start = LocalDateTime.parse(date + "T" + startTime);
    LocalDateTime end = LocalDateTime.parse(date + "T" + endTime);

    return new CalendarEvent(
        subject,
        start,
        end,
        "",
        location,
        "BUSY",
        null
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetAnalyticsNullFromDate() {
    LocalDate to = LocalDate.of(2025, 1, 10);
    model.getAnalytics(null, to);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetAnalyticsNullToDate() {
    LocalDate from = LocalDate.of(2025, 1, 1);
    model.getAnalytics(from, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetAnalyticsEndBeforeStart() {
    LocalDate from = LocalDate.of(2025, 1, 10);
    LocalDate to = LocalDate.of(2025, 1, 1);
    model.getAnalytics(from, to);
  }

  @Test
  public void testNoEventsInRange() {
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 7);

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(0, a.getTotalEvents());
    assertEquals(0.0, a.getAverageEventsPerDay(), 1e-6);
    assertTrue(a.getEventsBySubject().isEmpty());
    assertTrue(a.getEventsByWeekday().isEmpty());
    assertTrue(a.getEventsByWeekIndex().isEmpty());
    assertTrue(a.getEventsByMonth().isEmpty());
    assertEquals(0, a.getOnlineEvents());
    assertEquals(0, a.getOfflineEvents());
    assertEquals(0.0, a.getOnlinePercentage(), 1e-6);
  }

  @Test
  public void testSingleOnlineEventOneDayRange() {
    LocalDate from = LocalDate.of(2025, 1, 10);
    LocalDate to = LocalDate.of(2025, 1, 10);

    CalendarEvent e = createEvent(
        "Meeting",
        "2025-01-10",
        "10:00",
        "11:00",
        "online"
    );

    model.addEvent(e);

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(1, a.getTotalEvents());
    assertEquals(1.0, a.getAverageEventsPerDay(), 1e-6);

    assertEquals(Integer.valueOf(1), a.getEventsBySubject().get("Meeting"));

    DayOfWeek dow = LocalDate.of(2025, 1, 10).getDayOfWeek();
    assertEquals(Integer.valueOf(1), a.getEventsByWeekday().get(dow));

    YearMonth ym = YearMonth.of(2025, 1);
    assertEquals(Integer.valueOf(1), a.getEventsByMonth().get(ym));

    assertEquals(1, a.getOnlineEvents());
    assertEquals(0, a.getOfflineEvents());
    assertEquals(100.0, a.getOnlinePercentage(), 1e-6);

    assertEquals(from, a.getBusiestDay());
    assertEquals(1, a.getBusiestDayCount());
    assertEquals(from, a.getLeastBusyDay());
    assertEquals(1, a.getLeastBusyDayCount());
  }

  @Test
  public void testEventsOutsideRangeAreIgnored() {
    LocalDate from = LocalDate.of(2025, 1, 10);
    LocalDate to = LocalDate.of(2025, 1, 12);

    model.addEvent(createEvent("Before", "2025-01-09", "09:00", "10:00", "Office"));
    model.addEvent(createEvent("Inside1", "2025-01-10", "10:00", "11:00", "Office"));
    model.addEvent(createEvent("Inside2", "2025-01-12", "15:00", "16:00", "online"));
    model.addEvent(createEvent("After", "2025-01-13", "09:00", "10:00", "Office"));

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(2, a.getTotalEvents());
    assertEquals(2.0 / 3.0, a.getAverageEventsPerDay(), 1e-6);

    assertEquals(2, a.getEventsBySubject().size());
    assertEquals(Integer.valueOf(1), a.getEventsBySubject().get("Inside1"));
    assertEquals(Integer.valueOf(1), a.getEventsBySubject().get("Inside2"));
  }

  @Test
  public void testGroupingBySubjectWeekdayWeekAndMonth() {
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 2, 28);

    model.addEvent(createEvent("Work", "2025-01-06", "09:00", "10:00", "Office"));
    model.addEvent(createEvent("Work", "2025-01-06", "11:00", "12:00", "Office"));
    model.addEvent(createEvent("Gym", "2025-01-07", "18:00", "19:00", "Gym"));
    model.addEvent(createEvent("Study", "2025-01-14", "10:00", "11:00", "Library"));
    model.addEvent(createEvent("Study", "2025-01-14", "11:30", "12:30", "Library"));
    model.addEvent(createEvent("Study", "2025-01-14", "13:00", "14:00", "Library"));

    model.addEvent(createEvent("Misc", "2025-02-01", "09:00", "10:00", "Home"));
    model.addEvent(createEvent("Misc", "2025-02-15", "09:00", "10:00", "Home"));

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(Integer.valueOf(2), a.getEventsBySubject().get("Work"));
    assertEquals(Integer.valueOf(1), a.getEventsBySubject().get("Gym"));
    assertEquals(Integer.valueOf(3), a.getEventsBySubject().get("Study"));
    assertEquals(Integer.valueOf(2), a.getEventsBySubject().get("Misc"));

    DayOfWeek monday = LocalDate.of(2025, 1, 6).getDayOfWeek();
    assertEquals(Integer.valueOf(2), a.getEventsByWeekday().get(monday));

    YearMonth jan = YearMonth.of(2025, 1);
    YearMonth feb = YearMonth.of(2025, 2);
    assertEquals(Integer.valueOf(6), a.getEventsByMonth().get(jan));
    assertEquals(Integer.valueOf(2), a.getEventsByMonth().get(feb));

    assertTrue(a.getEventsByWeekIndex().size() >= 2);
    int totalFromWeeks = a.getEventsByWeekIndex()
        .values()
        .stream()
        .mapToInt(Integer::intValue)
        .sum();
    assertEquals(a.getTotalEvents(), totalFromWeeks);
  }

  @Test
  public void testBusiestAndLeastBusyDayWithZerosInBetween() {
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 5);

    model.addEvent(createEvent("A", "2025-01-02", "09:00", "10:00", "Office"));
    model.addEvent(createEvent("B", "2025-01-02", "11:00", "12:00", "Office"));

    model.addEvent(createEvent("C", "2025-01-04", "09:00", "10:00", "online"));

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(2, a.getBusiestDayCount());
    assertEquals(LocalDate.of(2025, 1, 2), a.getBusiestDay());

    Set<LocalDate> zeroEventDays = new HashSet<>(
        Arrays.asList(
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 3),
            LocalDate.of(2025, 1, 5)
        )
    );

    assertEquals(0, a.getLeastBusyDayCount());
    assertTrue(zeroEventDays.contains(a.getLeastBusyDay()));
  }

  @Test
  public void testOnlineVsOfflineClassification() {
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 10);

    model.addEvent(createEvent("E1", "2025-01-02", "09:00", "10:00", "online"));
    model.addEvent(createEvent("E2", "2025-01-03", "09:00", "10:00", "Online"));
    model.addEvent(createEvent("E3", "2025-01-04", "09:00", "10:00", "ONLINE"));

    model.addEvent(createEvent("E4", "2025-01-05", "09:00", "10:00", "Zoom"));
    model.addEvent(createEvent("E5", "2025-01-06", "09:00", "10:00", null));

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(5, a.getTotalEvents());
    assertEquals(3, a.getOnlineEvents());
    assertEquals(2, a.getOfflineEvents());
    assertEquals(60.0, a.getOnlinePercentage(), 1e-6);
  }

  @Test
  public void testAverageEventsPerDayNonInteger() {
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 4);

    model.addEvent(createEvent("E1", "2025-01-01", "09:00", "10:00", "Office"));
    model.addEvent(createEvent("E2", "2025-01-01", "11:00", "12:00", "Office"));
    model.addEvent(createEvent("E3", "2025-01-02", "09:00", "10:00", "Office"));
    model.addEvent(createEvent("E4", "2025-01-03", "09:00", "10:00", "Office"));
    model.addEvent(createEvent("E5", "2025-01-04", "09:00", "10:00", "Office"));

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(5, a.getTotalEvents());
    assertEquals(1.25, a.getAverageEventsPerDay(), 1e-6);
  }

  /**
   * Mock model for controller tests: extends CalendarModelImpl but overrides getAnalytics
   * so we can inject a known CalendarAnalytics and capture the requested range.
   */
  private static class DashboardMockModel extends CalendarModelImpl {
    LocalDate lastFrom;
    LocalDate lastTo;
    CalendarAnalytics analyticsToReturn;

    @Override
    public CalendarAnalytics getAnalytics(LocalDate from, LocalDate to) {
      this.lastFrom = from;
      this.lastTo = to;

      if (analyticsToReturn == null) {
        analyticsToReturn = new CalendarAnalytics(
            from,
            to,
            0,
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            Collections.emptyMap(),
            0.0,
            null,
            0,
            null,
            0,
            0,
            0,
            0.0
        );
      }
      return analyticsToReturn;
    }
  }

  /**
   * Mock view that records the last message printed by the controller.
   */
  private static class DashboardMockView implements CalendarView {
    String lastMessage;

    @Override
    public void displayMessage(String message) {
      this.lastMessage = message;
    }

    @Override
    public void displayError(String error) {

    }

    @Override
    public void displayEventsOnDate(LocalDate date, List<CalendarEvent> events) {

    }

    @Override
    public void displayEventsInRange(LocalDateTime start, LocalDateTime end,
                                     List<CalendarEvent> events) {

    }

    @Override
    public void displayStatus(LocalDateTime dateTime, boolean isBusy) {

    }

    @Override
    public void exportToCsv(String filename, List<CalendarEvent> events) {

    }

    @Override
    public void exportAuto(String filename, List<CalendarEvent> events) {

    }

    @Override
    public String getUserInput() {
      return "";
    }
  }

  /**
   * Helper to create a CalendarController instance using reflection.
   */
  private CalendarController createController(DashboardMockModel mm, DashboardMockView mv) {
    try {
      Constructor<?> ctor =
          CalendarController.class.getConstructor(model.CalendarModel.class, CalendarView.class);
      return (CalendarController) ctor.newInstance(mm, mv);
    } catch (Exception e) {
      throw new RuntimeException("Adjust createController(...) to match your CalendarController "
          + "constructor signature", e);
    }
  }

  @Test
  public void testHandleShowDashboardParsesDatesAndDelegatesToModel() throws Exception {
    Map<String, Integer> bySubject = new HashMap<>();
    bySubject.put("Work", 3);
    bySubject.put("Gym", 1);

    Map<DayOfWeek, Integer> byWeekday = new HashMap<>();
    byWeekday.put(DayOfWeek.MONDAY, 2);
    byWeekday.put(DayOfWeek.TUESDAY, 2);

    Map<Integer, Integer> byWeekIndex = new HashMap<>();
    byWeekIndex.put(1, 2);
    byWeekIndex.put(2, 2);

    Map<YearMonth, Integer> byMonth = new HashMap<>();
    byMonth.put(YearMonth.of(2025, 1), 4);
    DashboardMockModel mm = new DashboardMockModel();
    DashboardMockView mv = new DashboardMockView();
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 31);
    mm.analyticsToReturn = new CalendarAnalytics(
        from,
        to,
        4,
        bySubject,
        byWeekday,
        byWeekIndex,
        byMonth,
        4.0 / 31.0,
        LocalDate.of(2025, 1, 5),
        2,
        LocalDate.of(2025, 1, 10),
        0,
        1,
        3,
        25.0
    );

    String command = "show calendar dashboard from 2025-01-01 to 2025-01-31";
    CalendarController controller = createController(mm, mv);
    Method m = CalendarController.class
        .getDeclaredMethod("handleShowDashboard", String.class);
    m.setAccessible(true);
    m.invoke(controller, command);

    assertEquals(from, mm.lastFrom);
    assertEquals(to, mm.lastTo);

    String msg = mv.lastMessage;
    assertTrue(msg.contains("Calendar dashboard from 2025-01-01 to 2025-01-31"));
    assertTrue(msg.contains("Total events: 4"));
    assertTrue(msg.contains("Total events by subject:"));
    assertTrue(msg.contains("Work: 3"));
    assertTrue(msg.contains("Gym: 1"));
    assertTrue(msg.contains("Online events: 1"));
    assertTrue(msg.contains("Offline / other events: 3"));
    assertTrue(msg.contains("Percentage online: 25.0%"));
  }

  @Test
  public void testHandleShowDashboardInvalidFormatThrows() throws Exception {
    DashboardMockModel mm = new DashboardMockModel();
    DashboardMockView mv = new DashboardMockView();
    CalendarController controller = createController(mm, mv);

    String badCommand = "show calendar dashboard from 2025-01-01 2025-01-31";

    Method m = CalendarController.class
        .getDeclaredMethod("handleShowDashboard", String.class);
    m.setAccessible(true);

    try {
      m.invoke(controller, badCommand);
      fail("Expected IllegalArgumentException due to missing 'to'");
    } catch (InvocationTargetException e) {
      assertTrue(e.getCause() instanceof IllegalArgumentException);
      assertTrue(e.getCause().getMessage().contains("Invalid date range"));
    }
  }

  @Test
  public void testDisplayAnalyticsEmptySections() throws Exception {
    DashboardMockModel mm = new DashboardMockModel();
    DashboardMockView mv = new DashboardMockView();
    CalendarController controller = createController(mm, mv);

    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 7);

    CalendarAnalytics analytics = new CalendarAnalytics(
        from,
        to,
        0,
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap(),
        0.0,
        null,
        0,
        null,
        0,
        0,
        0,
        0.0
    );

    Method m = CalendarController.class
        .getDeclaredMethod("displayAnalytics", CalendarAnalytics.class);
    m.setAccessible(true);
    m.invoke(controller, analytics);

    String msg = mv.lastMessage;

    assertTrue(msg.contains("Calendar dashboard from 2025-01-01 to 2025-01-07"));
    assertTrue(msg.contains("Total events: 0"));

    assertTrue(msg.contains("Total events by subject:\n  (none)"));
    assertTrue(msg.contains("Total events by weekday:\n  (none)"));
    assertTrue(msg.contains("Total events by week (relative to start date):\n  (none)"));
    assertTrue(msg.contains("Total events by month:\n  (none)"));

    assertTrue(msg.contains("Online events: 0"));
    assertTrue(msg.contains("Offline / other events: 0"));
    assertTrue(msg.contains("Percentage online: 0.0%"));
  }

  @Test
  public void testDisplayAnalyticsNonEmptySections() throws Exception {
    Map<String, Integer> bySubject = new HashMap<>();
    bySubject.put("Work", 2);

    Map<DayOfWeek, Integer> byWeekday = new HashMap<>();
    byWeekday.put(DayOfWeek.MONDAY, 1);
    byWeekday.put(DayOfWeek.TUESDAY, 1);

    Map<Integer, Integer> byWeekIndex = new HashMap<>();
    byWeekIndex.put(1, 2);

    Map<YearMonth, Integer> byMonth = new HashMap<>();
    byMonth.put(YearMonth.of(2025, 1), 2);

    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 3);
    CalendarAnalytics analytics = new CalendarAnalytics(
        from,
        to,
        2,
        bySubject,
        byWeekday,
        byWeekIndex,
        byMonth,
        2.0 / 3.0,
        LocalDate.of(2025, 1, 1),
        1,
        LocalDate.of(2025, 1, 2),
        0,
        1,
        1,
        50.0
    );
    DashboardMockModel mm = new DashboardMockModel();
    DashboardMockView mv = new DashboardMockView();
    CalendarController controller = createController(mm, mv);
    Method m = CalendarController.class
        .getDeclaredMethod("displayAnalytics", CalendarAnalytics.class);
    m.setAccessible(true);
    m.invoke(controller, analytics);

    String msg = mv.lastMessage;

    assertTrue(msg.contains("Total events: 2"));
    assertTrue(msg.contains("Total events by subject:"));
    assertTrue(msg.contains("Work: 2"));

    assertTrue(msg.contains("Total events by weekday:"));
    assertTrue(msg.contains("MONDAY: 1"));
    assertTrue(msg.contains("TUESDAY: 1"));

    assertTrue(msg.contains("Total events by week (relative to start date):"));
    assertTrue(msg.contains("Week 1: 2"));

    assertTrue(msg.contains("Total events by month:"));
    assertTrue(msg.contains("2025-01: 2"));

    assertTrue(msg.contains("Busiest day: 2025-01-01 (1 events)"));
    assertTrue(msg.contains("Least busy day: 2025-01-02 (0 events)"));

    assertTrue(msg.contains("Online events: 1"));
    assertTrue(msg.contains("Offline / other events: 1"));
    assertTrue(msg.contains("Percentage online: 50.0%"));
  }

  @Test
  public void testMultiDayEventsCountedByStartDateOnly() {
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 5);

    CalendarEvent spanInside = new CalendarEvent(
        "Trip",
        LocalDateTime.of(2025, 1, 2, 9, 0),
        LocalDateTime.of(2025, 1, 4, 18, 0),
        "",
        "online",
        "BUSY",
        null
    );

    CalendarEvent spanIntoRange = new CalendarEvent(
        "Prev",
        LocalDateTime.of(2024, 12, 31, 9, 0),
        LocalDateTime.of(2025, 1, 2, 10, 0),
        "",
        "Office",
        "BUSY",
        null
    );

    model.addEvent(spanInside);
    model.addEvent(spanIntoRange);

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(1, a.getTotalEvents());
    assertEquals(Integer.valueOf(1), a.getEventsBySubject().get("Trip"));

    assertEquals(1.0 / 5.0, a.getAverageEventsPerDay(), 1e-6);

    assertEquals(LocalDate.of(2025, 1, 2), a.getBusiestDay());
    assertEquals(1, a.getBusiestDayCount());
  }

  @Test
  public void testAllDayEventSpanningMultipleDaysCountedOnce() {
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 3);

    CalendarEvent allDay = new CalendarEvent(
        "Conference",
        LocalDateTime.of(2025, 1, 1, 0, 0),
        LocalDateTime.of(2025, 1, 3, 23, 59),
        "",
        "online",
        "BUSY",
        null
    );

    model.addEvent(allDay);

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(1, a.getTotalEvents());
    assertEquals(Integer.valueOf(1), a.getEventsBySubject().get("Conference"));

    assertEquals(1.0 / 3.0, a.getAverageEventsPerDay(), 1e-6);

    assertEquals(LocalDate.of(2025, 1, 1), a.getBusiestDay());
    assertEquals(1, a.getBusiestDayCount());
  }

  @Test
  public void testBusiestDayTieUsesAnyOfMaxDaysButCountIsCorrect() {
    LocalDate from = LocalDate.of(2025, 1, 1);
    LocalDate to = LocalDate.of(2025, 1, 4);

    model.addEvent(createEvent("E1", "2025-01-01", "09:00", "10:00", "Office"));
    model.addEvent(createEvent("E2", "2025-01-01", "11:00", "12:00", "Office"));

    model.addEvent(createEvent("E3", "2025-01-03", "09:00", "10:00", "Office"));
    model.addEvent(createEvent("E4", "2025-01-03", "11:00", "12:00", "Office"));

    CalendarAnalytics a = model.getAnalytics(from, to);

    assertEquals(2, a.getBusiestDayCount());

    Set<LocalDate> candidates = new HashSet<>(
        Arrays.asList(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 3)));
    assertTrue(candidates.contains(a.getBusiestDay()));
  }

  @Test
  public void testHandleShowDashboardWithExtraWhitespace() throws Exception {
    DashboardMockModel mm = new DashboardMockModel();
    DashboardMockView mv = new DashboardMockView();
    CalendarController controller = createController(mm, mv);

    String command = "show calendar dashboard from   2025-01-01    to   2025-01-31   ";

    Method m = CalendarController.class
        .getDeclaredMethod("handleShowDashboard", String.class);
    m.setAccessible(true);
    m.invoke(controller, command);

    assertEquals(LocalDate.of(2025, 1, 1), mm.lastFrom);
    assertEquals(LocalDate.of(2025, 1, 31), mm.lastTo);
  }

  @Test
  public void testHandleShowDashboardInvalidDateString() throws Exception {
    DashboardMockModel mm = new DashboardMockModel();
    DashboardMockView mv = new DashboardMockView();
    CalendarController controller = createController(mm, mv);

    String badCommand = "show calendar dashboard from 2025-13-01 to 2025-01-31";

    Method m = CalendarController.class
        .getDeclaredMethod("handleShowDashboard", String.class);
    m.setAccessible(true);

    try {
      m.invoke(controller, badCommand);
      fail("Expected DateTimeParseException wrapped in InvocationTargetException");
    } catch (java.lang.reflect.InvocationTargetException e) {
      assertTrue(e.getCause() instanceof java.time.format.DateTimeParseException);
    }
  }
}
