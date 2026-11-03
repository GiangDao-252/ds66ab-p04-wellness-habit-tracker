package com.oop.project.ui;

import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import com.oop.project.service.ApplicationService;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.Locale;

final class HabitsView {
    private final ApplicationService app;
    private final ConsoleInput input;
    private final PrintStream out;

    HabitsView(ApplicationService app, ConsoleInput input, PrintStream out) {
        this.app = app;
        this.input = input;
        this.out = out;
    }

    void list() throws SQLException {
        var rows = app.habits(input.id("Plan ID: "));
        if (rows.isEmpty()) out.println("No habits found.");
        for (var row : rows) {
            out.printf("%d | %s | %s | %s %s | %s to %s | %s%n",
                    row.id(), row.name(), row.frequency(), row.targetValue(), row.unit(),
                    row.activeFrom(), row.activeTo(), row.active() ? "active" : "inactive");
        }
    }

    void create() throws SQLException {
        var habit = new HabitRow(0, input.id("Plan ID: "), input.read("Habit name: "),
                frequency(), input.measurement("Target (> 0): "), input.read("Unit: "),
                input.date("Start date: "), input.date("End date: "), true);
        out.println("Habit created. ID: " + app.createHabit(habit));
    }

    void edit() throws SQLException {
        app.editHabit(input.id("Habit ID: "), input.read("New name: "), frequency());
        out.println("Habit updated.");
    }

    void deactivate() throws SQLException {
        app.deactivateHabit(input.id("Habit ID: "));
        out.println("Habit deactivated.");
    }

    private Frequency frequency() {
        try {
            return Frequency.valueOf(input.read("Frequency (DAILY/WEEKLY): ").toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Choose DAILY or WEEKLY.");
        }
    }
}
