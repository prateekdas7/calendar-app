package view;

import controller.GuiController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import model.CalendarAnalytics;
import model.CalendarEvent;

/**
 * Graphical user interface for the calendar application.
 * Provides a month view with event management capabilities.
 */
public class CalendarGui extends JFrame {
  private GuiController controller;
  private YearMonth currentMonth;
  private JPanel calendarPanel;
  private JLabel monthYearLabel;
  private JPanel dayGridPanel;
  private JComboBox<String> calendarSelector;
  private JLabel timezoneLabel;
  private EventDialogFactory eventDialogFactory;

  private static final Color BG_COLOR = new Color(248, 248, 248);
  private static final Color HEADER_BG = new Color(255, 255, 255);
  private static final Color ACCENT_COLOR = new Color(0, 122, 255);
  private static final Color TEXT_PRIMARY = new Color(29, 29, 31);
  private static final Color TEXT_SECONDARY = new Color(142, 142, 147);
  private static final Color TODAY_BORDER = new Color(255, 149, 0);
  private static final Color SELECTED_BG = new Color(0, 122, 255, 10);
  private static final Color EVENT_DOT = new Color(52, 199, 89);
  private static final Color BORDER_COLOR = new Color(229, 229, 234);

  private static final int WINDOW_WIDTH = 1100;
  private static final int WINDOW_HEIGHT = 750;
  private static final int PANEL_PADDING_VERTICAL = 20;
  private static final int PANEL_PADDING_HORIZONTAL = 30;

  private static final int TODAY_CIRCLE_SIZE = 40;
  private static final int NAV_BUTTON_PADDING_VERTICAL = 5;
  private static final int NAV_BUTTON_PADDING_HORIZONTAL = 12;
  private static final int SECONDARY_BUTTON_PADDING_VERTICAL = 6;
  private static final int SECONDARY_BUTTON_PADDING_HORIZONTAL = 12;
  private static final int PRIMARY_BUTTON_PADDING_VERTICAL = 8;
  private static final int PRIMARY_BUTTON_PADDING_HORIZONTAL = 16;
  private static final int DAY_HEADER_PADDING_VERTICAL = 12;
  private static final int DAY_HEADER_PADDING_HORIZONTAL = 8;
  private static final int DAY_CELL_PADDING = 8;
  private static final int DAY_LABEL_PADDING = 4;

  private static final int CREATE_CALENDAR_DIALOG_WIDTH = 400;
  private static final int CREATE_CALENDAR_DIALOG_HEIGHT = 150;

  /**
   * Constructs the Calendar GUI.
   *
   * @param controller the GUI controller
   */
  public CalendarGui(GuiController controller) {
    this.controller = controller;
    this.currentMonth = YearMonth.now();
    this.eventDialogFactory = new EventDialogFactory(this, controller, this::updateCalendarDisplay);

    setTitle("Calendar");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setLocationRelativeTo(null);
    getContentPane().setBackground(BG_COLOR);

    initializeUi();
  }

  /**
   * Initializes the user interface components.
   */
  private void initializeUi() {
    setLayout(new BorderLayout(0, 0));

    add(createTopPanel(), BorderLayout.NORTH);

    calendarPanel = new JPanel(new BorderLayout());
    calendarPanel.setBackground(BG_COLOR);
    calendarPanel.setBorder(new EmptyBorder(PANEL_PADDING_VERTICAL, PANEL_PADDING_HORIZONTAL,
        PANEL_PADDING_VERTICAL, PANEL_PADDING_HORIZONTAL));
    add(calendarPanel, BorderLayout.CENTER);
    add(createBottomPanel(), BorderLayout.SOUTH);
    updateCalendarDisplay();
  }

  /**
   * Creates the top panel with navigation and calendar selection.
   */
  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout(0, 0));
    topPanel.setBackground(HEADER_BG);
    topPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
        new EmptyBorder(15, PANEL_PADDING_HORIZONTAL, 15, PANEL_PADDING_HORIZONTAL)
    ));

    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
    navPanel.setBackground(HEADER_BG);

    JButton prevButton = createNavButton("‹");
    prevButton.addActionListener(e -> {
      currentMonth = currentMonth.minusMonths(1);
      updateCalendarDisplay();
    });

    monthYearLabel = new JLabel();
    monthYearLabel.setFont(new Font("SF Pro Display", Font.BOLD, 22));
    monthYearLabel.setForeground(TEXT_PRIMARY);

    JButton nextButton = createNavButton("›");
    nextButton.addActionListener(e -> {
      currentMonth = currentMonth.plusMonths(1);
      updateCalendarDisplay();
    });

    JButton todayButton = createSecondaryButton("Today");
    todayButton.addActionListener(e -> {
      currentMonth = YearMonth.now();
      updateCalendarDisplay();
    });

    navPanel.add(prevButton);
    navPanel.add(monthYearLabel);
    navPanel.add(nextButton);
    navPanel.add(Box.createHorizontalStrut(20));
    navPanel.add(todayButton);

    JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
    selectorPanel.setBackground(HEADER_BG);

    JLabel calLabel = new JLabel("Calendar:");
    calLabel.setForeground(TEXT_SECONDARY);
    calLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 13));

    calendarSelector = new JComboBox<>();
    calendarSelector.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
    calendarSelector.setBackground(Color.WHITE);
    calendarSelector.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        new EmptyBorder(DAY_LABEL_PADDING, DAY_CELL_PADDING, DAY_LABEL_PADDING, DAY_CELL_PADDING)
    ));
    calendarSelector.addItem("Default Calendar");
    calendarSelector.addActionListener(e -> {
      String selected = (String) calendarSelector.getSelectedItem();
      if (selected != null) {
        controller.switchCalendar(selected);
        updateTimezoneLabel();
        updateCalendarDisplay();
      }
    });

    timezoneLabel = new JLabel();
    timezoneLabel.setForeground(TEXT_SECONDARY);
    timezoneLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 12));

    JButton newCalButton = createSecondaryButton("+ New Calendar");
    newCalButton.addActionListener(e -> showCreateCalendarDialog());

    selectorPanel.add(calLabel);
    selectorPanel.add(calendarSelector);
    selectorPanel.add(timezoneLabel);
    selectorPanel.add(Box.createHorizontalStrut(10));
    selectorPanel.add(newCalButton);

    topPanel.add(navPanel, BorderLayout.CENTER);
    topPanel.add(selectorPanel, BorderLayout.SOUTH);

    updateTimezoneLabel();

    return topPanel;
  }

  /**
   * Creates a navigation button (< or >).
   */
  private JButton createNavButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font("SF Pro Display", Font.PLAIN, 28));
    button.setForeground(ACCENT_COLOR);
    button.setBackground(HEADER_BG);
    button.setBorder(new EmptyBorder(NAV_BUTTON_PADDING_VERTICAL, NAV_BUTTON_PADDING_HORIZONTAL,
        NAV_BUTTON_PADDING_VERTICAL, NAV_BUTTON_PADDING_HORIZONTAL));
    button.setFocusPainted(false);
    button.setContentAreaFilled(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    button.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        button.setForeground(ACCENT_COLOR.darker());
      }

      public void mouseExited(java.awt.event.MouseEvent evt) {
        button.setForeground(ACCENT_COLOR);
      }
    });

    return button;
  }

  /**
   * Creates a secondary style button.
   */
  private JButton createSecondaryButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
    button.setForeground(ACCENT_COLOR);
    button.setBackground(Color.WHITE);
    button.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        new EmptyBorder(SECONDARY_BUTTON_PADDING_VERTICAL, SECONDARY_BUTTON_PADDING_HORIZONTAL,
            SECONDARY_BUTTON_PADDING_VERTICAL, SECONDARY_BUTTON_PADDING_HORIZONTAL)
    ));
    button.setFocusPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    button.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        button.setBackground(new Color(250, 250, 250));
      }

      public void mouseExited(java.awt.event.MouseEvent evt) {
        button.setBackground(Color.WHITE);
      }
    });

    return button;
  }

  /**
   * Creates a primary style button.
   */
  private JButton createPrimaryButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font("SF Pro Text", Font.BOLD, 13));
    button.setForeground(Color.WHITE);
    button.setBackground(ACCENT_COLOR);
    button.setBorder(new EmptyBorder(PRIMARY_BUTTON_PADDING_VERTICAL,
        PRIMARY_BUTTON_PADDING_HORIZONTAL,
        PRIMARY_BUTTON_PADDING_VERTICAL, PRIMARY_BUTTON_PADDING_HORIZONTAL));
    button.setFocusPainted(false);
    button.setOpaque(true);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    button.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        button.setBackground(ACCENT_COLOR.darker());
      }

      public void mouseExited(java.awt.event.MouseEvent evt) {
        button.setBackground(ACCENT_COLOR);
      }
    });

    return button;
  }

  /**
   * Creates the bottom panel with action buttons.
   */
  private JPanel createBottomPanel() {
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
    bottomPanel.setBackground(HEADER_BG);
    bottomPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
        new EmptyBorder(15, PANEL_PADDING_HORIZONTAL, 15, PANEL_PADDING_HORIZONTAL)
    ));

    JButton createEventButton = createPrimaryButton("Create Event");
    createEventButton.addActionListener(e -> showCreateEventDialog());

    JButton viewEventsButton = createSecondaryButton("View Day Events");
    viewEventsButton.addActionListener(e -> showSelectDateForViewDialog());

    JButton editEventButton = createSecondaryButton("Edit Event");
    editEventButton.addActionListener(e -> showEditEventDialog());

    JButton analyticsButton = createSecondaryButton("Analytics");
    analyticsButton.addActionListener(e -> showAnalyticsDialog());

    JButton exportButton = createSecondaryButton("Export Calendar");
    exportButton.addActionListener(e -> showExportDialog());

    bottomPanel.add(createEventButton);
    bottomPanel.add(viewEventsButton);
    bottomPanel.add(editEventButton);
    bottomPanel.add(analyticsButton);
    bottomPanel.add(exportButton);

    return bottomPanel;
  }

  /**
   * Updates the calendar display for the current month.
   */
  private void updateCalendarDisplay() {
    calendarPanel.removeAll();

    String monthYear = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        + " " + currentMonth.getYear();
    monthYearLabel.setText(monthYear);

    JPanel gridContainer = new JPanel(new BorderLayout());
    gridContainer.setBackground(Color.WHITE);
    gridContainer.setBorder(BorderFactory.createCompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        new EmptyBorder(0, 0, 0, 0)
    ));

    dayGridPanel = new JPanel(new GridLayout(0, 7, 0, 0));
    dayGridPanel.setBackground(Color.WHITE);

    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : dayNames) {
      JLabel header = new JLabel(day, SwingConstants.CENTER);
      header.setFont(new Font("SF Pro Text", Font.BOLD, 12));
      header.setForeground(TEXT_SECONDARY);
      header.setOpaque(true);
      header.setBackground(new Color(250, 250, 250));
      header.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_COLOR),
          new EmptyBorder(DAY_HEADER_PADDING_VERTICAL, DAY_HEADER_PADDING_HORIZONTAL,
              DAY_HEADER_PADDING_VERTICAL, DAY_HEADER_PADDING_HORIZONTAL)
      ));
      dayGridPanel.add(header);
    }

    LocalDate firstOfMonth = currentMonth.atDay(1);
    int daysInMonth = currentMonth.lengthOfMonth();
    DayOfWeek firstDayOfWeek = firstOfMonth.getDayOfWeek();

    int blankDays = firstDayOfWeek.getValue() % 7;
    for (int i = 0; i < blankDays; i++) {
      JPanel blank = new JPanel();
      blank.setBackground(Color.WHITE);
      blank.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_COLOR));
      dayGridPanel.add(blank);
    }

    LocalDate today = LocalDate.now();
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), day);
      JPanel dayPanel = createDayPanel(date, date.equals(today));
      dayGridPanel.add(dayPanel);
    }

    gridContainer.add(dayGridPanel, BorderLayout.CENTER);
    calendarPanel.add(gridContainer, BorderLayout.CENTER);
    calendarPanel.revalidate();
    calendarPanel.repaint();
  }

  /**
   * Creates a panel for a single day in the calendar.
   */
  private JPanel createDayPanel(LocalDate date, boolean isToday) {
    JPanel panel = new JPanel(new BorderLayout(0, 4));
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 1, BORDER_COLOR),
        new EmptyBorder(DAY_CELL_PADDING, DAY_CELL_PADDING, DAY_CELL_PADDING, DAY_CELL_PADDING)
    ));

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    topPanel.setBackground(Color.WHITE);
    topPanel.setOpaque(false);

    JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
    dayLabel.setFont(new Font("SF Pro Text", Font.PLAIN, 15));
    dayLabel.setForeground(TEXT_PRIMARY);

    if (isToday) {
      dayLabel.setForeground(TEXT_PRIMARY);
      dayLabel.setOpaque(true);
      dayLabel.setBackground(TODAY_BORDER);
      dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
      dayLabel.setVerticalAlignment(SwingConstants.CENTER);
      dayLabel.setBorder(new EmptyBorder(DAY_LABEL_PADDING, DAY_CELL_PADDING,
          DAY_LABEL_PADDING, DAY_CELL_PADDING));
      Dimension size = new Dimension(TODAY_CIRCLE_SIZE, TODAY_CIRCLE_SIZE);
      dayLabel.setPreferredSize(size);
      dayLabel.setMinimumSize(size);
      dayLabel.setMaximumSize(size);
      dayLabel.setVisible(true);
    }

    topPanel.add(dayLabel);
    panel.add(topPanel, BorderLayout.NORTH);

    if (!isToday) {
      List<CalendarEvent> events = controller.getEventsOnDate(date);
      if (!events.isEmpty()) {
        JPanel eventPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        eventPanel.setBackground(Color.WHITE);
        eventPanel.setOpaque(false);

        int dotsToShow = Math.min(events.size(), 3);
        for (int i = 0; i < dotsToShow; i++) {
          JLabel dot = new JLabel("●");
          dot.setFont(new Font("SF Pro Text", Font.PLAIN, 10));
          dot.setForeground(EVENT_DOT);
          eventPanel.add(dot);
        }

        if (events.size() > 3) {
          JLabel more = new JLabel("+" + (events.size() - 3));
          more.setFont(new Font("SF Pro Text", Font.PLAIN, 9));
          more.setForeground(TEXT_SECONDARY);
          eventPanel.add(more);
        }

        panel.add(eventPanel, BorderLayout.CENTER);
      }
    }

    final boolean isTodayDate = isToday;
    panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    panel.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        showDayEventsDialog(date);
      }

      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        if (isTodayDate) {
          panel.setBackground(Color.WHITE);
          dayLabel.setBackground(new Color(255, 169, 50));
        } else {
          panel.setBackground(SELECTED_BG);
          dayLabel.setForeground(Color.WHITE);
        }
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        panel.setBackground(Color.WHITE);
        if (!isTodayDate) {
          dayLabel.setForeground(TEXT_PRIMARY);
        } else {
          dayLabel.setBackground(TODAY_BORDER);
        }
      }
    });

    return panel;
  }

  /**
   * Shows dialog to create a new calendar.
   */
  private void showCreateCalendarDialog() {
    JDialog dialog = new JDialog(this, "Create New Calendar", true);
    dialog.setLayout(new GridLayout(3, 2, 10, 10));
    dialog.setSize(CREATE_CALENDAR_DIALOG_WIDTH, CREATE_CALENDAR_DIALOG_HEIGHT);
    dialog.setLocationRelativeTo(this);

    final JLabel nameLabel = new JLabel("Calendar Name:");
    JTextField nameField = new JTextField();

    final JLabel timezoneLabel = new JLabel("Timezone:");
    String[] popularTimezones = {
        "America/New_York", "America/Chicago", "America/Denver",
        "America/Los_Angeles", "America/Phoenix", "America/Anchorage",
        "Pacific/Honolulu", "Europe/London", "Europe/Paris",
        "Europe/Berlin", "Asia/Tokyo", "Asia/Shanghai",
        "Asia/Dubai", "Asia/Kolkata", "Australia/Sydney", "UTC"
    };
    JComboBox<String> timezoneCombo = new JComboBox<>(popularTimezones);
    timezoneCombo.setSelectedItem("America/New_York");

    JButton createButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");

    createButton.addActionListener(e -> {
      String name = nameField.getText().trim();
      String timezone = (String) timezoneCombo.getSelectedItem();

      if (name.isEmpty()) {
        JOptionPane.showMessageDialog(dialog, "Calendar name cannot be empty!");
        return;
      }

      try {
        controller.createCalendar(name, timezone);
        calendarSelector.addItem(name);
        calendarSelector.setSelectedItem(name);
        dialog.dispose();
        JOptionPane.showMessageDialog(this, "Calendar created successfully!");
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
      }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    dialog.add(nameLabel);
    dialog.add(nameField);
    dialog.add(timezoneLabel);
    dialog.add(timezoneCombo);
    dialog.add(createButton);
    dialog.add(cancelButton);

    dialog.setVisible(true);
  }

  /**
   * Shows dialog to create a new event.
   */
  private void showCreateEventDialog() {
    eventDialogFactory.showCreateEventDialog();
  }

  /**
   * Shows events for a specific day.
   */
  private void showDayEventsDialog(LocalDate date) {
    eventDialogFactory.showDayEventsDialog(date);
  }

  /**
   * Shows dialog to select date for viewing events.
   */
  private void showSelectDateForViewDialog() {
    String dateStr = JOptionPane.showInputDialog(this,
        "Enter date (YYYY-MM-DD):",
        LocalDate.now().toString());

    if (dateStr != null && !dateStr.trim().isEmpty()) {
      try {
        LocalDate date = LocalDate.parse(dateStr.trim());
        showDayEventsDialog(date);
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Invalid date format: " + ex.getMessage());
      }
    }
  }

  /**
   * Shows dialog to edit an event.
   */
  private void showEditEventDialog() {
    eventDialogFactory.showEditEventDialog();
  }

  /**
   * Shows dialog to export calendar.
   */
  private void showExportDialog() {
    String filename = JOptionPane.showInputDialog(this,
        "Enter filename (with .csv or .ics extension):",
        "calendar.csv");

    if (filename != null && !filename.trim().isEmpty()) {
      try {
        controller.exportCalendar(filename.trim());
        JOptionPane.showMessageDialog(this, "Calendar exported successfully!");
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
      }
    }
  }

  /**
   * Updates the timezone label.
   */
  private void updateTimezoneLabel() {
    String timezone = controller.getCurrentTimezone();
    timezoneLabel.setText("(" + timezone + ")");
  }

  /**
   * Displays the GUI.
   */
  public void display() {
    setVisible(true);
  }

  private static class Box {
    public static javax.swing.Box.Filler createHorizontalStrut(int width) {
      return new javax.swing.Box.Filler(
          new Dimension(width, 0),
          new Dimension(width, 0),
          new Dimension(width, Short.MAX_VALUE)
      );
    }
  }

  /**
   * Shows the analytics dashboard for a user-selected date interval.
   */
  private void showAnalyticsDialog() {
    try {
      String startStr = JOptionPane.showInputDialog(
          this,
          "Enter start date (YYYY-MM-DD):",
          LocalDate.now().withDayOfMonth(1).toString()
      );
      if (startStr == null || startStr.trim().isEmpty()) {
        return;
      }

      String endStr = JOptionPane.showInputDialog(
          this,
          "Enter end date (YYYY-MM-DD):",
          LocalDate.now().toString()
      );
      if (endStr == null || endStr.trim().isEmpty()) {
        return;
      }

      LocalDate startDate = LocalDate.parse(startStr.trim());
      LocalDate endDate = LocalDate.parse(endStr.trim());
      if (endDate.isBefore(startDate)) {
        JOptionPane.showMessageDialog(this, "End date must be on or after start date.");
        return;
      }

      CalendarAnalytics analytics = controller.getCalendarAnalytics(startDate, endDate);
      showAnalyticsResultDialog(analytics);

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
    }
  }

  /**
   * Renders the analytics object in a scrollable text dialog.
   */
  private void showAnalyticsResultDialog(CalendarAnalytics a) {
    JDialog dialog = new JDialog(this, "Calendar Analytics", true);
    dialog.setSize(600, 500);
    dialog.setLocationRelativeTo(this);
    dialog.setLayout(new BorderLayout(10, 10));

    javax.swing.JTextArea textArea = new javax.swing.JTextArea();
    textArea.setEditable(false);
    textArea.setFont(new Font("SF Pro Text", Font.PLAIN, 13));

    StringBuilder sb = new StringBuilder();
    sb.append("Analytics for ")
        .append(a.getFromDate())
        .append(" to ")
        .append(a.getToDate())
        .append("\n\n");

    sb.append("Total events: ").append(a.getTotalEvents()).append("\n\n");

    sb.append("Total events by subject:\n");
    if (a.getEventsBySubject().isEmpty()) {
      sb.append("  (none)\n");
    } else {
      a.getEventsBySubject().forEach((subj, count) ->
          sb.append("  ").append(subj).append(": ").append(count).append("\n"));
    }
    sb.append("\n");

    sb.append("Total events by weekday:\n");
    if (a.getEventsByWeekday().isEmpty()) {
      sb.append("  (none)\n");
    } else {
      a.getEventsByWeekday().forEach((dow, count) ->
          sb.append("  ").append(dow).append(": ").append(count).append("\n"));
    }
    sb.append("\n");

    sb.append("Total events by week (relative to start date):\n");
    if (a.getEventsByWeekIndex().isEmpty()) {
      sb.append("  (none)\n");
    } else {
      a.getEventsByWeekIndex().forEach((week, count) ->
          sb.append("  Week ").append(week).append(": ").append(count).append("\n"));
    }
    sb.append("\n");

    sb.append("Total events by month:\n");
    if (a.getEventsByMonth().isEmpty()) {
      sb.append("  (none)\n");
    } else {
      a.getEventsByMonth().forEach((ym, count) ->
          sb.append("  ").append(ym).append(": ").append(count).append("\n"));
    }
    sb.append("\n");

    sb.append("Average events per day: ")
        .append(String.format("%.2f", a.getAverageEventsPerDay()))
        .append("\n");
    sb.append("Busiest day: ").append(a.getBusiestDay())
        .append(" (").append(a.getBusiestDayCount()).append(" events)\n");
    sb.append("Least busy day: ").append(a.getLeastBusyDay())
        .append(" (").append(a.getLeastBusyDayCount()).append(" events)\n\n");

    sb.append("Online events: ").append(a.getOnlineEvents()).append("\n");
    sb.append("Offline / other events: ").append(a.getOfflineEvents()).append("\n");
    sb.append("Percentage online: ")
        .append(String.format("%.1f", a.getOnlinePercentage()))
        .append("%\n");

    textArea.setText(sb.toString());
    textArea.setCaretPosition(0);

    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    dialog.add(scroll, BorderLayout.CENTER);

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dialog.dispose());
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(closeButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
  }
}