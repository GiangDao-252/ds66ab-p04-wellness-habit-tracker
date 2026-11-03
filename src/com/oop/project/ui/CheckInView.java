package com.oop.project.ui;

import com.oop.project.repository.CheckInRepository.CheckInRow;
import com.oop.project.service.ApplicationService;
import java.io.PrintStream;
import java.sql.SQLException;

final class CheckInView {
    private final ApplicationService app;
    private final ConsoleInput input;
    private final PrintStream out;

    CheckInView(ApplicationService app, ConsoleInput input, PrintStream out) {
        this.app = app;
        this.input = input;
        this.out = out;
    }

    void list() throws SQLException {
        var rows = app.checkIns(input.id("Habit ID: "), input.date("From: "), input.date("To: "));
        if (rows.isEmpty()) out.println("No check-ins found.");
        for (var row : rows) {
            out.printf("%d | %s | %s | %s%n", row.id(), row.checkDate(), row.measurement(), row.note());
        }
    }

    void add() throws SQLException {
        long id = app.addCheckIn(input.id("Habit ID: "), input.date("Date: "),
                input.measurement("Measurement: "), input.read("Note: "));
        out.println("Check-in saved. ID: " + id);
    }

    void edit() throws SQLException {
        long habitId = input.id("Habit ID: ");
        app.editCheckIn(new CheckInRow(input.id("Check-in ID: "), habitId,
                input.date("New date: "), input.measurement("New measurement: "), input.read("New note: ")));
        out.println("Check-in updated.");
    }

    void delete() throws SQLException {
        long habitId = input.id("Habit ID: ");
        long id = input.id("Check-in ID: ");
        if ("DELETE".equals(input.read("Type DELETE to confirm: "))) {
            app.deleteCheckIn(id, habitId);
            out.println("Check-in deleted.");
        } else {
            out.println("Deletion cancelled.");
        }
    }
}
