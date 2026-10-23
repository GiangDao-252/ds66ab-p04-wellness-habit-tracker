package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.LocalDate;

public final class HabitDetailsDeleteTest {

    public static void main(String[] args) {
        WellnessPlanRepository plans
                = new WellnessPlanRepository();

        HabitRepository habits = new HabitRepository();

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                long planId = plans.insert(
                        connection, "Habit Details Test Plan"
                );

                LocalDate today = LocalDate.now();

                long habitId = habits.insert(
                        connection,
                        new HabitRow(
                                0,
                                planId,
                                "Original Habit",
                                Frequency.DAILY,
                                new BigDecimal("30"),
                                "minutes",
                                today.minusDays(7),
                                today.plusDays(30),
                                true
                        )
                );

                if (!habits.updateDetails(
                        connection,
                        habitId,
                        "Weekly Exercise",
                        Frequency.WEEKLY)) {
                    throw new IllegalStateException(
                            "Habit details were not updated."
                    );
                }

                HabitRow updated
                        = habits.findById(connection, habitId).orElseThrow();

                if (!updated.name().equals("Weekly Exercise")
                        || updated.frequency() != Frequency.WEEKLY
                        || updated.targetValue().compareTo(
                                new BigDecimal("30")) != 0
                        || !updated.unit().equals("minutes")) {
                    throw new IllegalStateException(
                            "Updated habit data is incorrect."
                    );
                }

                System.out.println("Habit details update successful.");

                // Create a related check-in to test FK protection.
                String insertSql = """
                    INSERT INTO check_in (
                        habit_id, check_date, measurement, note
                    )
                    VALUES (?, ?, ?, ?)
                    """;

                try (
                        PreparedStatement statement
                        = connection.prepareStatement(insertSql)) {
                    statement.setLong(1, habitId);
                    statement.setObject(2, today);
                    statement.setBigDecimal(3, new BigDecimal("30"));
                    statement.setString(4, "Deletion protection test");
                    statement.executeUpdate();
                }

                Savepoint beforeDelete = connection.setSavepoint();
                boolean blocked = false;

                try {
                    habits.delete(connection, habitId);
                } catch (SQLException e) {
                    if (!"23503".equals(e.getSQLState())) {
                        throw e;
                    }

                    connection.rollback(beforeDelete);
                    blocked = true;
                }

                if (!blocked
                        || habits.findById(
                                connection, habitId).isEmpty()) {
                    throw new IllegalStateException(
                            "The linked habit was not protected."
                    );
                }

                System.out.println("Habit with check-in protected.");

                // Remove only the check-in created by this test.
                try (
                        PreparedStatement statement
                        = connection.prepareStatement(
                                "DELETE FROM check_in WHERE habit_id = ?"
                        )) {
                            statement.setLong(1, habitId);

                            if (statement.executeUpdate() != 1) {
                                throw new IllegalStateException(
                                        "Expected exactly one test check-in."
                                );
                            }
                        }

                        if (!habits.delete(connection, habitId)
                                || habits.findById(
                                        connection, habitId).isPresent()) {
                            throw new IllegalStateException(
                                    "The unlinked habit was not deleted."
                            );
                        }

                        System.out.println("Unlinked habit deletion successful.");

                        if (habits.delete(connection, habitId)
                                || habits.updateDetails(
                                        connection,
                                        habitId,
                                        "Missing Habit",
                                        Frequency.DAILY)) {
                            throw new IllegalStateException(
                                    "A missing habit was not handled correctly."
                            );
                        }

                        System.out.println("Missing habit handled.");

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException | RuntimeException e) {
            System.err.println("Habit details/deletion test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
