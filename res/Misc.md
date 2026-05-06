# Design Changes

1. **Added `GuiController` in `controller` to separate GUI interactions from the text-based `CalendarController`.**
    - In GUI, parameters such as the event subject, start date, start time, and others are already
      collected as individual pieces of data from UI components like text fields, selectors, and 
      check boxes. There's no need to parse a single string command to retrieve these parameters as
      `CalendarController` did in the text-based interface.
    - Thus, `GuiController` calls well-typed methods with these structured values directly,
      including `createCalendar`, `switchCalendar`, `createSingleEvent`, `createAllDayEvent`,
      `createRepeatingEvent`, `createAllDayRepeatingEvent`, `getEventsOnDate`, `exportCalendar`,
      `getCurrentTimezone`, and `getActiveCalendarName`.
    - These methods internally operate on the underlying `MulriCalendarModel` and the active 
      `CalendarModel`.


2. **Added a graphical user interface `CalendarGui` in `view` using Java Swing.**

    In addition to the existing text-based interface, `CalendarGui` provides a more user-friendly
      month view and supports interactive calendar and event management visually.
    - At the top of the window, there is a navigation panel built with a `JPanel` and `JButton` for
      user to navigate between months: 
      - "<"     goes to previous month, 
      - ">"     goes to next month, or 
      - "Today" goes to current month
    - Below that, there's a selector panel with 
      - a `JComboBox<String>` for choosing which calendar to view, 
      - a `JLabel` showing the current timezone, and 
      - a `JButton` "+ New Calendar" that opens a dialog to create a new calendar.
    - The GUI displays a month view as a 7-column grid `GridLayout` inside a `JPanel`, where each 
      day is rendered as its own `JPanel` cell.
      - Each day cell is clickable and has a mouse listener that opens a dialog showing the events
        for that date.
      - Each day cell, except today, draws small event dots using `JLabel` to represent existing
        events on that day.
    - At the bottom, another `JPanel` holds action buttons:
      - "Create Event"
      - "View Day Events"
      - "Edit Event"
      - "Export Calendar"
      
      These buttons open Swing dialogs to collect user input and calls the corresponding meethods 
      on `GuiController` to manage events and calendars.


3. **Extended `CalendarRunner` to support three modes: GUI (default), interactive, and headless.**
    - Three modes as following: 
      - GUI mode is trigger without arguments;
      - Interactive mode is trigger by `--mode interactive`;
      - Headless mode is trigger by `--mode headless`
    - All three modes share the same calendar model classes and the same controller behavior. 
    - In the interactive and headless modes, the way the user interacts with the program remains 
     unchanged.
    - In the newly added GUI mode, `CalendarRunner` creates a `GuiController` and a `CalendarGui` 
     window, where the user interacts with the calendar by clicking buttons and day cells, filling
     in dialogs to manage calendars and events in Swing components instead of typing commands.


# Features
The following features have been implemented and supported:

1. **Modes**
   - GUI mode (default)
   - Interactive mode
   - Headless mode


2. **Calendar Functionality**
    - Creating calendars with a specific timezone
    - Switching the active calendar
    - Displaying and updating the current timezone when switching calendars


3. **Calendar Export**
    - Exporting to CSV `csv`
    - Exporting to iCal `.ics`


4. **Event Functionality**
    - Creating single events
    - Creating all-day events
    - Creating repeating events
    - Creating repeating all-day events
    - Retrieving events on a specific date
    - Editing event properties (single event / this and future / all in series)
        

5. **GUI features**
    - Displaying the name of the current active calendar
    - Displaying the current timezone
    - "Create calendar" dialog
    - Month view with clickable day cells; can be navigated between months
    - Event dots in day cells showing how busy the day is
    - "Create event" dialog (single / all-day)
    - "Create recurring event" dialog (single / all-day)
    - "View day events" dialog
    - "Edit event" dialog
    - "Export calendar" dialog (from GUI)