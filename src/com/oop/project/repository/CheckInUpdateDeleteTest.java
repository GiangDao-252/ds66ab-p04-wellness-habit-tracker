package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.repository.CheckInRepository.CheckInRow;
import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public final class CheckInUpdateDeleteTest {

    public static void main(String[] args) {
        WellnessPlanRepository plans
                = new WellnessPlanRepository();

        HabitRepository habits = new HabitRepository();
        CheckInRepository checkIns = new CheckInRepository();

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                LocalDate today = LocalDate.now();

                long planId = plans.insert(
                        connection, "Check-in Update Test Plan"
                );

                long habitId = habits.insert(
                        connection,
                        new HabitRow(
                                0, planId, "Drink water",
                                Frequency.DAILY,
                                new BigDecimal("2000"),
                                "ml",
                                today.minusDays(7),
                                today.plusDays(30),
                                true
                        )
                );

                long checkInId = checkIns.insert(
                        connection,
                        new CheckInRow(
                                0, habitId, today.minusDays(1),
                                new BigDecimal("1000"), "Original note"
                        )
                );

                CheckInRow original = checkIns.findById(
                        connection, checkInId, habitId
                ).orElseThrow(() -> new IllegalStateException(
                        "The inserted check-in was not found."
                ));

                if (original.measurement().compareTo(
                        new BigDecimal("1000")) != 0) {
                    throw new IllegalStateException(
                            "The original measurement is incorrect."
                    );
                }

                System.out.println("Check-in lookup successful.");

                CheckInRow update = new CheckInRow(
                        checkInId, habitId, today,
                        new BigDecimal("1750.5"), "Updated note"
                );

                if (!checkIns.update(connection, update)) {
                    throw new IllegalStateException(
                            "The check-in was not updated."
                    );
                }

                CheckInRow loaded = checkIns.findById(
                        connection, checkInId, habitId
                ).orElseThrow();

                if (!loaded.checkDate().equals(today)
                        || loaded.measurement().compareTo(
                                new BigDecimal("1750.5")) != 0
                        || !loaded.note().equals("Updated note")) {
                    throw new IllegalStateException(
                            "The updated check-in data is incorrect."
                    );
                }

                System.out.println("Check-in update successful.");

                // A wrong habit ID must not match this check-in.
                if (checkIns.findById(
                        connection, checkInId, -1L).isPresent()
                        || checkIns.update(
                                connection,
                                new CheckInRow(
                                        checkInId, -1L, today,
                                        BigDecimal.ONE, "Wrong habit"
                                ))
                        || checkIns.delete(
                                connection, checkInId, -1L)) {
                    throw new IllegalStateException(
                            "The habit filter was not enforced."
                    );
                }

                CheckInRow afterWrongHabit = checkIns.findById(
                        connection, checkInId, habitId
                ).orElseThrow();

                if (!afterWrongHabit.equals(loaded)) {
                    throw new IllegalStateException(
                            "The wrong-habit operation changed the check-in."
                    );
                }

                System.out.println("Wrong habit operations rejected.");

                boolean rejected = false;

                try {
                    checkIns.update(
                            connection,
                            new CheckInRow(
                                    checkInId, habitId, today,
                                    new BigDecimal("-1"), "Invalid value"
                            )
                    );
                } catch (IllegalArgumentException expected) {
                    rejected = true;
                }

                CheckInRow afterInvalidInput = checkIns.findById(
                        connection, checkInId, habitId
                ).orElseThrow();

                if (!rejected || !afterInvalidInput.equals(loaded)) {
                    throw new IllegalStateException(
                            "Invalid input was accepted or changed the data."
                    );
                }

                System.out.println("Negative update rejected.");

                if (!checkIns.delete(
                        connection, checkInId, habitId)
                        || checkIns.findById(
                                connection, checkInId, habitId).isPresent()) {
                    throw new IllegalStateException(
                            "The check-in was not deleted."
                    );
                }

                System.out.println("Check-in deletion successful.");

                if (checkIns.delete(connection, checkInId, habitId)
                        || checkIns.update(connection, update)) {
                    throw new IllegalStateException(
                            "A missing check-in was not handled correctly."
                    );
                }

                System.out.println("Missing check-in handled.");

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException | RuntimeException e) {
            System.err.println("Check-in update/delete test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
