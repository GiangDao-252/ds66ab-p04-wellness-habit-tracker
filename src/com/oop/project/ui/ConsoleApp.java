package com.oop.project.ui;

import com.oop.project.exception.WellnessException;
import com.oop.project.service.ApplicationService;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class ConsoleApp {
    public void run(Scanner scanner, PrintStream out) {
        var app = new ApplicationService();
        var input = new ConsoleInput(scanner, out);
        var habits = new HabitsView(app, input, out);
        var checkIns = new CheckInView(app, input, out);
        var dashboard = new DashboardObserver(app, new DashboardView(out), out);
        var reports = new ReportsView(app, input, dashboard);
        app.observe(dashboard);

        while (true) {
            out.println("""
                Wellness Habit Tracker
                1 List plans       2 Create plan
                3 List habits      4 Create habit
                5 Edit habit       6 Deactivate habit
                7 List check-ins   8 Add check-in
                9 Edit check-in   10 Delete check-in
                11 Dashboard      12 Switch strategy
                0 Exit
                """);
            try {
                switch (input.read("Choose: ")) {
                    case "0" -> { return; }
                    case "1" -> app.plans().forEach(plan -> out.printf("%d | %s%n", plan.id(), plan.name()));
                    case "2" -> out.println("Plan created. ID: " + app.createPlan(input.read("Plan name: ")));
                    case "3" -> habits.list();
                    case "4" -> habits.create();
                    case "5" -> habits.edit();
                    case "6" -> habits.deactivate();
                    case "7" -> checkIns.list();
                    case "8" -> checkIns.add();
                    case "9" -> checkIns.edit();
                    case "10" -> checkIns.delete();
                    case "11" -> reports.show();
                    case "12" -> reports.switchStrategy();
                    default -> out.println("Choose a menu number from 0 to 12.");
                }
            } catch (NoSuchElementException e) {
                return;
            } catch (WellnessException | IllegalArgumentException | IllegalStateException e) {
                out.println("Operation failed: " + e.getMessage());
            } catch (SQLException e) {
                out.println("Database operation failed (SQLSTATE " + e.getSQLState() + ").");
            }
        }
    }
}
