package view;

import controller.GuiController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import model.CalendarEvent;

/**
 * Factory class for creating event-related dialogs.
 */
public class EventDialogFactory {
  private final JFrame parent;
  private final GuiController controller;
  private final Runnable updateCallback;

  private static final int CREATE_EVENT_DIALOG_WIDTH = 500;
  private static final int CREATE_EVENT_DIALOG_HEIGHT = 400;
  private static final int RECURRING_EVENT_DIALOG_WIDTH = 450;
  private static final int RECURRING_EVENT_DIALOG_HEIGHT = 250;
  private static final int EDIT_EVENT_DIALOG_WIDTH = 500;
  private static final int EDIT_EVENT_DIALOG_HEIGHT = 400;
  private static final int DAY_EVENTS_DIALOG_WIDTH = 500;
  private static final int DAY_EVENTS_DIALOG_HEIGHT = 400;
  private static final int DIALOG_GRID_SPACING = 10;

  private static final Color TEXT_PRIMARY = new Color(29, 29, 31);
  private static final Color TEXT_SECONDARY = new Color(142, 142, 147);
  private static final Color ACCENT_COLOR = new Color(0, 122, 255);
  private static final Color BORDER_COLOR = new Color(229, 229, 234);

  /**
   * Validates time format (HH:MM).
   *
   * @param time the time string to validate
   * @return true if valid, false otherwise
   */
  private boolean isValidTime(String time) {
    if (!time.matches("\\d{2}:\\d{2}")) {
      return false;
    }
    String[] parts = time.split(":");
    int hour = Integer.parseInt(parts[0]);
    int minute = Integer.parseInt(parts[1]);
    return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
  }

  /**
   * Creates a secondary styled button.
   *
   * @param text the button text
   * @return the styled button
   */
  private JButton createSecondaryButton(String text) {
    JButton button = new JButton(text);
    button.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
    button.setForeground(Color.BLACK);
    button.setBackground(Color.WHITE);
    button.setBorder(BorderFactory.createCompoundBorder(
        new javax.swing.border.LineBorder(BORDER_COLOR, 1, true),
        new EmptyBorder(6, 12, 6, 12)
    ));
    button.setFocusPainted(false);
    button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    return button;
  }

  /**
   * Constructs an EventDialogFactory.
   *
   * @param parent         the parent frame
   * @param controller     the GUI controller
   * @param updateCallback callback to refresh the calendar display
   */
  public EventDialogFactory(JFrame parent, GuiController controller, Runnable updateCallback) {
    this.parent = parent;
    this.controller = controller;
    this.updateCallback = updateCallback;
  }

  /**
   * Shows dialog to create a new event.
   */
  public void showCreateEventDialog() {
    JDialog dialog = new JDialog(parent, "Create Event", true);
    dialog.setLayout(new GridLayout(9, 2, 10, 10));
    dialog.setSize(CREATE_EVENT_DIALOG_WIDTH, CREATE_EVENT_DIALOG_HEIGHT);
    dialog.setLocationRelativeTo(parent);

    final JLabel subjectLabel = new JLabel("Subject:");
    JTextField subjectField = new JTextField();

    final JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
    JTextField dateField = new JTextField(LocalDate.now().toString());

    final JLabel startLabel = new JLabel("Start Time (HH:MM):");
    JTextField startField = new JTextField("09:00");

    final JLabel endLabel = new JLabel("End Time (HH:MM):");
    JTextField endField = new JTextField("10:00");

    final JLabel locationLabel = new JLabel("Location (optional):");
    final JTextField locationField = new JTextField();

    final JLabel descriptionLabel = new JLabel("Description (optional):");
    final JTextField descriptionField = new JTextField();


    JCheckBox allDayBox = new JCheckBox("All-day event");
    allDayBox.addActionListener(e -> {
      boolean allDay = allDayBox.isSelected();
      startField.setEnabled(!allDay);
      endField.setEnabled(!allDay);
    });

    JCheckBox recurringBox = new JCheckBox("Recurring event");

    JButton createButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");

    createButton.addActionListener(e -> {
      try {
        String subject = subjectField.getText().trim();
        String dateStr = dateField.getText().trim();

        if (subject.isEmpty() || dateStr.isEmpty()) {
          JOptionPane.showMessageDialog(dialog, "Subject and date are required!");
          return;
        }

        if (recurringBox.isSelected()) {
          showRecurringEventDialog(subject, dateStr, startField.getText(),
              endField.getText(), allDayBox.isSelected());
          dialog.dispose();
        } else {
          if (allDayBox.isSelected()) {
            controller.createAllDayEvent(subject, dateStr);
          } else {
            String startTime = startField.getText().trim();
            String endTime = endField.getText().trim();

            if (!isValidTime(startTime) || !isValidTime(endTime)) {
              JOptionPane.showMessageDialog(dialog,
                  "Invalid time format. Please use HH:MM (e.g., 09:00, 14:30)");
              return;
            }
            String dateTime1 = dateStr + "T" + startTime;
            String dateTime2 = dateStr + "T" + endTime;
            String location = locationField.getText().trim();
            String description = descriptionField.getText().trim();
            controller.createSingleEvent(subject, dateTime1, dateTime2, location, description);
          }

          updateCallback.run();
          dialog.dispose();
          JOptionPane.showMessageDialog(parent, "Event created successfully!");
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
      }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    dialog.add(subjectLabel);
    dialog.add(subjectField);
    dialog.add(dateLabel);
    dialog.add(dateField);
    dialog.add(startLabel);
    dialog.add(startField);
    dialog.add(endLabel);
    dialog.add(endField);
    dialog.add(locationLabel);
    dialog.add(locationField);
    dialog.add(descriptionLabel);
    dialog.add(descriptionField);
    dialog.add(allDayBox);
    dialog.add(recurringBox);
    dialog.add(createButton);
    dialog.add(cancelButton);
    dialog.setVisible(true);
  }

  /**
   * Shows dialog for recurring event creation.
   */
  public void showRecurringEventDialog(String subject, String dateStr,
                                       String startTime, String endTime, boolean allDay) {
    JDialog dialog = new JDialog(parent, "Recurring Event Details", true);
    dialog.setLayout(new GridLayout(5, 2, 10, 10));
    dialog.setSize(RECURRING_EVENT_DIALOG_WIDTH, RECURRING_EVENT_DIALOG_HEIGHT);
    dialog.setLocationRelativeTo(parent);

    final JLabel daysLabel = new JLabel("Repeat on (e.g., MWF):");
    JTextField daysField = new JTextField("MWF");

    final JLabel typeLabel = new JLabel("Repeat type:");
    String[] options = {"For number of times", "Until date"};
    JComboBox<String> typeCombo = new JComboBox<>(options);

    final JLabel countLabel = new JLabel("Number of occurrences:");
    JTextField countField = new JTextField("5");

    final JLabel untilLabel = new JLabel("Until date (YYYY-MM-DD):");
    JTextField untilField = new JTextField();
    untilField.setEnabled(false);

    typeCombo.addActionListener(e -> {
      boolean isCount = typeCombo.getSelectedIndex() == 0;
      countField.setEnabled(isCount);
      untilField.setEnabled(!isCount);
    });

    JButton createButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");

    createButton.addActionListener(e -> {
      try {
        String weekdays = daysField.getText().trim();

        if (typeCombo.getSelectedIndex() == 0) {
          int count = Integer.parseInt(countField.getText().trim());
          if (allDay) {
            controller.createAllDayRepeatingEvent(subject, dateStr, weekdays, count, null);
          } else {
            String dateTime1 = dateStr + "T" + startTime;
            String dateTime2 = dateStr + "T" + endTime;
            controller.createRepeatingEvent(subject, dateTime1, dateTime2, weekdays, count, null);
          }
        } else {
          String until = untilField.getText().trim();
          if (allDay) {
            controller.createAllDayRepeatingEvent(subject, dateStr, weekdays, -1, until);
          } else {
            String dateTime1 = dateStr + "T" + startTime;
            String dateTime2 = dateStr + "T" + endTime;
            controller.createRepeatingEvent(subject, dateTime1, dateTime2, weekdays, -1, until);
          }
        }

        updateCallback.run();
        dialog.dispose();
        JOptionPane.showMessageDialog(parent, "Recurring event created successfully!");
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
      }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    dialog.add(daysLabel);
    dialog.add(daysField);
    dialog.add(typeLabel);
    dialog.add(typeCombo);
    dialog.add(countLabel);
    dialog.add(countField);
    dialog.add(untilLabel);
    dialog.add(untilField);
    dialog.add(createButton);
    dialog.add(cancelButton);

    dialog.setVisible(true);
  }

  /**
   * Shows dialog to edit an event.
   */
  public void showEditEventDialog() {
    JDialog dialog = new JDialog(parent, "Edit Event", true);
    dialog.setLayout(new GridLayout(8, 2, 10, 10));
    dialog.setSize(EDIT_EVENT_DIALOG_WIDTH, EDIT_EVENT_DIALOG_HEIGHT);
    dialog.setLocationRelativeTo(parent);

    final JLabel subjectLabel = new JLabel("Event Subject:");
    final JTextField subjectField = new JTextField();

    final JLabel dateLabel = new JLabel("Event Date/Time:");
    final JTextField dateField = new JTextField("2025-01-01T09:00");

    final JLabel propertyLabel = new JLabel("Property to edit:");
    String[] properties = {"subject", "start", "end", "location", "description", "status"};
    JComboBox<String> propertyCombo = new JComboBox<>(properties);

    final JLabel valueLabel = new JLabel("New value:");
    final JTextField valueField = new JTextField();

    final JLabel endTimeLabel = new JLabel("New end time (HH:MM):");
    JTextField endTimeField = new JTextField();
    endTimeLabel.setVisible(false);
    endTimeField.setVisible(false);

    final JLabel scopeLabel = new JLabel("Edit scope:");
    String[] scopes = {"Single event", "This and future", "All in series"};
    JComboBox<String> scopeCombo = new JComboBox<>(scopes);
    JCheckBox editBothTimesBox = new JCheckBox("Edit both start and end times");
    editBothTimesBox.addActionListener(e -> {
      boolean editBoth = editBothTimesBox.isSelected();
      if (editBoth) {
        propertyCombo.setSelectedItem("start");
        propertyCombo.setEnabled(false);
        valueLabel.setText("New start time (HH:MM):");
        endTimeLabel.setVisible(true);
        endTimeField.setVisible(true);
      } else {
        propertyCombo.setEnabled(true);
        valueLabel.setText("New value:");
        endTimeLabel.setVisible(false);
        endTimeField.setVisible(false);
      }
    });

    JButton editButton = new JButton("Edit");
    JButton cancelButton = new JButton("Cancel");

    editButton.addActionListener(e -> {
      try {
        String subject = subjectField.getText().trim();
        String dateTime = dateField.getText().trim();
        int scope = scopeCombo.getSelectedIndex();

        if (editBothTimesBox.isSelected()) {
          String newStartTime = valueField.getText().trim();
          String newEndTime = endTimeField.getText().trim();

          String datePart = dateTime.substring(0, 10);
          if (newStartTime.matches("\\d{2}:\\d{2}")) {
            newStartTime = datePart + "T" + newStartTime;
          }
          if (newEndTime.matches("\\d{2}:\\d{2}")) {
            newEndTime = datePart + "T" + newEndTime;
          }

          controller.editEvent(subject, dateTime, "end", newEndTime, scope);
          controller.editEvent(subject, dateTime, "start", newStartTime, scope);
        } else {
          String property = (String) propertyCombo.getSelectedItem();
          String newValue = valueField.getText().trim();

          if (("start".equals(property) || "end".equals(property))
              && newValue.matches("\\d{2}:\\d{2}")) {
            String datePart = dateTime.substring(0, 10);
            newValue = datePart + "T" + newValue;
          }

          controller.editEvent(subject, dateTime, property, newValue, scope);
        }

        updateCallback.run();
        dialog.dispose();
        JOptionPane.showMessageDialog(parent, "Event updated successfully!");
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
      }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    dialog.add(subjectLabel);
    dialog.add(subjectField);
    dialog.add(dateLabel);
    dialog.add(dateField);
    dialog.add(propertyLabel);
    dialog.add(propertyCombo);
    dialog.add(valueLabel);
    dialog.add(valueField);
    dialog.add(endTimeLabel);
    dialog.add(endTimeField);
    dialog.add(scopeLabel);
    dialog.add(scopeCombo);
    dialog.add(editBothTimesBox);
    dialog.add(new JLabel(""));
    dialog.add(editButton);
    dialog.add(cancelButton);

    dialog.setVisible(true);
  }

  /**
   * Shows events for a specific day.
   */
  public void showDayEventsDialog(LocalDate date) {
    final List<CalendarEvent> events = controller.getEventsOnDate(date);

    JDialog dialog = new JDialog(parent, "Events on " + date, true);
    dialog.setSize(DAY_EVENTS_DIALOG_WIDTH, DAY_EVENTS_DIALOG_HEIGHT);
    dialog.setLocationRelativeTo(parent);
    dialog.setLayout(new BorderLayout(10, 10));

    if (events.isEmpty()) {
      JLabel noEvents = new JLabel("No events on this day", SwingConstants.CENTER);
      noEvents.setFont(new Font("SF Pro Text", Font.PLAIN, 14));
      noEvents.setForeground(TEXT_SECONDARY);
      dialog.add(noEvents, BorderLayout.CENTER);
    } else {
      DefaultListModel<String> listModel = new DefaultListModel<>();
      for (CalendarEvent event : events) {
        String eventStr = event.getSubject() + " ("
            + event.getStartDateTime().toLocalTime() + " - "
            + event.getEndDateTime().toLocalTime() + ")";
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          eventStr += " at " + event.getLocation();
        }
        listModel.addElement(eventStr);
      }

      JList<String> eventList = new JList<>(listModel);
      eventList.setFont(new Font("SF Pro Text", Font.PLAIN, 13));
      eventList.setForeground(TEXT_PRIMARY);
      JScrollPane scrollPane = new JScrollPane(eventList);
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
      dialog.add(scrollPane, BorderLayout.CENTER);
    }

    JButton closeButton = createSecondaryButton("Close");
    closeButton.addActionListener(e -> dialog.dispose());
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(Color.WHITE);
    buttonPanel.add(closeButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
  }
}