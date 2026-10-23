package com.oop.project;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.db.Transactions;
import com.oop.project.repository.CheckInRepository;
import com.oop.project.repository.CheckInRepository.CheckInRow;
import com.oop.project.repository.HabitRepository;
import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import com.oop.project.repository.WellnessPlanRepository;
import com.oop.project.service.CheckInService;
import com.oop.project.service.WellnessPlanService;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

public final class PersistenceMain {

    private static final Scanner INPUT = new Scanner(System.in);

    private static final WellnessPlanRepository PLANS
            = new WellnessPlanRepository();

    private static final HabitRepository HABITS
            = new HabitRepository();

    private static final CheckInRepository CHECK_INS
            = new CheckInRepository();

    private static final WellnessPlanService PLAN_SERVICE
            = new WellnessPlanService();

    private static final CheckInService CHECK_IN_SERVICE
            = new CheckInService();

    private PersistenceMain() {
    }

    public static void main(String[] args) {
        System.out.println("Wellness Habit Tracker - Persistence Console");
        System.out.println("Successful changes are saved to PostgreSQL.");

        while (true) {
            printMenu();

            if (!INPUT.hasNextLine()) {
                return;
            }

            String choice = INPUT.nextLine().trim();

            try {
                switch (choice) {
                    case "1" ->
                        listPlans();
                    case "2" ->
                        createPlan();
                    case "3" ->
                        createHabit();
                    case "4" ->
                        listHabits();
                    case "5" ->
                        addCheckIn();
                    case "6" ->
                        listCheckIns();
                    case "7" ->
                        updateCheckIn();
                    case "8" ->
                        deleteCheckIn();
                    case "0" -> {
                        System.out.println("Goodbye.");
                        return;
                    }
                    default ->
                        System.out.println(
                                "Please choose a number from 0 to 8."
                        );
                }
            } catch (SQLException e) {
                System.err.println("Database operation failed.");
                System.err.println(e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("Operation failed: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1. List wellness plans");
        System.out.println("2. Create a wellness plan");
        System.out.println("3. Create a habit");
        System.out.println("4. List habits in a plan");
        System.out.println("5. Add a check-in");
        System.out.println("6. List check-ins for a period");
        System.out.println("7. Update a check-in");
        System.out.println("8. Delete a check-in");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    private static void listPlans() throws SQLException {
        try (Connection connection = ConnectionFactory.open()) {
            var rows = PLANS.findAll(connection);

            if (rows.isEmpty()) {
                System.out.println("No wellness plans found.");
                return;
            }

            System.out.println("ID | Name | Created at");

            for (var row : rows) {
                System.out.printf(
                        "%d | %s | %s%n",
                        row.id(), row.name(), row.createdAt()
                );
            }
        }
    }

    private static void createPlan() throws SQLException {
        String name = read("Plan name: ");

        long id = PLAN_SERVICE.createPlan(name);

        System.out.println("Plan created. ID: " + id);
        System.out.println("Default evaluation settings created.");
    }

    private static void createHabit() throws SQLException {
        long planId = readId("Plan ID: ");
        String name = read("Habit name: ");

        Frequency frequency = Frequency.valueOf(
                read("Frequency (DAILY or WEEKLY): ")
                        .toUpperCase(java.util.Locale.ROOT)
        );

        BigDecimal target = new BigDecimal(read("Target value: "));
        String unit = read("Unit (for example, steps): ");

        LocalDate from = LocalDate.parse(
                read("Active from (YYYY-MM-DD): ")
        );

        LocalDate to = LocalDate.parse(
                read("Active to (YYYY-MM-DD): ")
        );

        HabitRow habit = new HabitRow(
                0,
                planId,
                name,
                frequency,
                target,
                unit,
                from,
                to,
                true
        );

        long id = Transactions.execute(
                connection -> HABITS.insert(connection, habit)
        );

        System.out.println("Active habit created. ID: " + id);
    }

    private static void listHabits() throws SQLException {
        long planId = readId("Plan ID: ");

        try (Connection connection = ConnectionFactory.open()) {
            var rows = HABITS.findByPlanId(connection, planId);

            if (rows.isEmpty()) {
                System.out.println("No habits found for this plan ID.");
                return;
            }

            System.out.println(
                    "ID | Name | Frequency | Target | Unit"
                    + " | Active from | Active to | Active"
            );

            for (var row : rows) {
                System.out.printf(
                        "%d | %s | %s | %s | %s | %s | %s | %s%n",
                        row.id(),
                        row.name(),
                        row.frequency(),
                        row.targetValue(),
                        row.unit(),
                        row.activeFrom(),
                        row.activeTo(),
                        row.active()
                );
            }
        }
    }

    private static void addCheckIn() throws SQLException {
        long habitId = readId("Habit ID: ");

        LocalDate date = LocalDate.parse(
                read("Check-in date (YYYY-MM-DD): ")
        );

        BigDecimal measurement = new BigDecimal(
                read("Measurement: ")
        );

        String note = read("Note (optional): ");

        long id = CHECK_IN_SERVICE.add(
                habitId, date, measurement, note
        );

        System.out.println("Check-in created. ID: " + id);
    }

    private static void listCheckIns() throws SQLException {
        long habitId = readId("Habit ID: ");

        LocalDate from = LocalDate.parse(
                read("From date (YYYY-MM-DD): ")
        );

        LocalDate to = LocalDate.parse(
                read("To date (YYYY-MM-DD): ")
        );

        try (Connection connection = ConnectionFactory.open()) {
            var rows = CHECK_INS.findByHabitAndPeriod(
                    connection, habitId, from, to
            );

            if (rows.isEmpty()) {
                System.out.println("No check-ins found for this selection.");
                return;
            }

            System.out.println("ID | Date | Measurement | Note");

            for (var row : rows) {
                System.out.printf(
                        "%d | %s | %s | %s%n",
                        row.id(),
                        row.checkDate(),
                        row.measurement(),
                        row.note()
                );
            }
        }
    }

    private static void updateCheckIn() throws SQLException {
        long habitId = readId("Habit ID: ");
        long checkInId = readId("Check-in ID: ");

        LocalDate date = LocalDate.parse(
                read("New date (YYYY-MM-DD): ")
        );

        BigDecimal measurement = new BigDecimal(
                read("New measurement: ")
        );

        String note = read("New note (optional): ");

        CHECK_IN_SERVICE.update(new CheckInRow(
                checkInId, habitId, date, measurement, note
        ));

        System.out.println("Check-in updated.");
    }

    private static void deleteCheckIn() throws SQLException {
        long habitId = readId("Habit ID: ");
        long checkInId = readId("Check-in ID: ");

        String confirmation = read(
                "Type DELETE to confirm deletion: "
        );

        if (!"DELETE".equals(confirmation)) {
            System.out.println("Deletion cancelled.");
            return;
        }

        CHECK_IN_SERVICE.delete(checkInId, habitId);

        System.out.println("Check-in deleted.");
    }

    private static String read(String prompt) {
        System.out.print(prompt);
        return INPUT.nextLine().trim();
    }

    private static long readId(String prompt) {
        long id = Long.parseLong(read(prompt));

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "The ID must be a positive integer."
            );
        }

        return id;
    }
}
