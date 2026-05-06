package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

/**
 * Represents a complete analytical summary of the events within a selected
 * date interval for a calendar. This class is immutable and stores all metrics
 * required by the analytics dashboard feature.
 */
public class CalendarAnalytics {
  private final LocalDate fromDate;
  private final LocalDate toDate;

  private final int totalEvents;
  private final Map<String, Integer> eventsBySubject;
  private final Map<DayOfWeek, Integer> eventsByWeekday;
  private final Map<Integer, Integer> eventsByWeekIndex;
  private final Map<YearMonth, Integer> eventsByMonth;

  private final double averageEventsPerDay;
  private final LocalDate busiestDay;
  private final int busiestDayCount;
  private final LocalDate leastBusyDay;
  private final int leastBusyDayCount;

  private final int onlineEvents;
  private final int offlineEvents;
  private final double onlinePercentage;

  /**
   * Constructs an immutable analytics summary for the given date interval.
   *
   * @param fromDate            the starting date of the interval (inclusive)
   * @param toDate              the ending date of the interval (inclusive)
   * @param totalEvents         total number of events within the interval
   * @param eventsBySubject     map of event subject -> number of occurrences
   * @param eventsByWeekday     map of DayOfWeek -> number of events on that weekday
   * @param eventsByWeekIndex   map of relative or ISO week index -> event count
   * @param eventsByMonth       map of YearMonth -> number of events occurring in that month
   * @param averageEventsPerDay average number of events per day in that interval
   * @param busiestDay          the date with the most number of events in that interval
   * @param busiestDayCount     number of events on the busiest day
   * @param leastBusyDay        the date with the lowest number of events (maybe zero)
   * @param leastBusyDayCount   number of events on the least busy day
   * @param onlineEvents        number of events whose location indicates they are online
   * @param offlineEvents       number of events not marked as online
   * @param onlinePercentage    percentage of total events that are online
   */
  public CalendarAnalytics(LocalDate fromDate,
                           LocalDate toDate,
                           int totalEvents,
                           Map<String, Integer> eventsBySubject,
                           Map<DayOfWeek, Integer> eventsByWeekday,
                           Map<Integer, Integer> eventsByWeekIndex,
                           Map<YearMonth, Integer> eventsByMonth,
                           double averageEventsPerDay,
                           LocalDate busiestDay,
                           int busiestDayCount,
                           LocalDate leastBusyDay,
                           int leastBusyDayCount,
                           int onlineEvents,
                           int offlineEvents,
                           double onlinePercentage) {
    this.fromDate = fromDate;
    this.toDate = toDate;
    this.totalEvents = totalEvents;
    this.eventsBySubject = eventsBySubject;
    this.eventsByWeekday = eventsByWeekday;
    this.eventsByWeekIndex = eventsByWeekIndex;
    this.eventsByMonth = eventsByMonth;
    this.averageEventsPerDay = averageEventsPerDay;
    this.busiestDay = busiestDay;
    this.busiestDayCount = busiestDayCount;
    this.leastBusyDay = leastBusyDay;
    this.leastBusyDayCount = leastBusyDayCount;
    this.onlineEvents = onlineEvents;
    this.offlineEvents = offlineEvents;
    this.onlinePercentage = onlinePercentage;
  }

  public LocalDate getFromDate() {
    return fromDate;
  }

  public LocalDate getToDate() {
    return toDate;
  }

  public int getTotalEvents() {
    return totalEvents;
  }

  public Map<String, Integer> getEventsBySubject() {
    return eventsBySubject;
  }

  public Map<DayOfWeek, Integer> getEventsByWeekday() {
    return eventsByWeekday;
  }

  public Map<Integer, Integer> getEventsByWeekIndex() {
    return eventsByWeekIndex;
  }

  public Map<YearMonth, Integer> getEventsByMonth() {
    return eventsByMonth;
  }

  public double getAverageEventsPerDay() {
    return averageEventsPerDay;
  }

  public LocalDate getBusiestDay() {
    return busiestDay;
  }

  public int getBusiestDayCount() {
    return busiestDayCount;
  }

  public LocalDate getLeastBusyDay() {
    return leastBusyDay;
  }

  public int getLeastBusyDayCount() {
    return leastBusyDayCount;
  }

  public int getOnlineEvents() {
    return onlineEvents;
  }

  public int getOfflineEvents() {
    return offlineEvents;
  }

  public double getOnlinePercentage() {
    return onlinePercentage;
  }
}