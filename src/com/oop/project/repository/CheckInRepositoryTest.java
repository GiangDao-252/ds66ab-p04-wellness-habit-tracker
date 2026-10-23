package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.repository.CheckInRepository.CheckInRow;
import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.LocalDate;
import java.util.List;

public final class CheckInRepositoryTest {

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
                        connection, "Check-in Repository Test Plan"
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

                long firstId = checkIns.insert(
                        connection,
                        new CheckInRow(
                                0, habitId, today.minusDays(1),
                                BigDecimal.ZERO, null
                        )
                );

                long secondId = checkIns.insert(
                        connection,
                        new CheckInRow(
                                0, habitId, today,
                                new BigDecimal("1500.5"),
                                "Afternoon measurement"
                        )
                );

                List<CheckInRow> loaded
                        = checkIns.findByHabitAndPeriod(
                                connection, habitId,
                                today.minusDays(1), today
                        );

                if (loaded.size() != 2
                        || loaded.get(0).id() != firstId
                        || loaded.get(1).id() != secondId
                        || loaded.get(0).habitId() != habitId
                        || !loaded.get(0).checkDate().equals(
                                today.minusDays(1))
                        || !loaded.get(1).checkDate().equals(today)
                        || loaded.get(0).measurement().compareTo(
                                BigDecimal.ZERO) != 0
                        || !loaded.get(0).note().equals("")
                        || loaded.get(1).measurement().compareTo(
                                new BigDecimal("1500.5")) != 0
                        || !loaded.get(1).note().equals(
                                "Afternoon measurement")) {
                    throw new IllegalStateException(
                            "Check-in data or ordering is incorrect."
                    );
                }

                System.out.println("Check-in insertion and mapping successful.");

                if (checkIns.findByHabitAndPeriod(
                        connection, habitId, today, today).size() != 1) {
                    throw new IllegalStateException(
                            "The single-day query returned incorrect results."
                    );
                }

                System.out.println("Inclusive date filtering successful.");

                boolean negativeRejected = false;

                try {
                    checkIns.insert(
                            connection,
                            new CheckInRow(
                                    0, habitId, today.minusDays(2),
                                    new BigDecimal("-1"), ""
                            )
                    );
                } catch (IllegalArgumentException expected) {
                    negativeRejected = true;
                }

                if (!negativeRejected) {
                    throw new IllegalStateException(
                            "A negative measurement was accepted."
                    );
                }

                System.out.println("Negative measurement rejected.");

                Savepoint beforeDuplicate
                        = connection.setSavepoint();

                boolean duplicateRejected = false;

                try {
                    checkIns.insert(
                            connection,
                            new CheckInRow(
                                    0, habitId, today,
                                    BigDecimal.ONE, "Duplicate date"
                            )
                    );
                } catch (SQLException e) {
                    if (!"23505".equals(e.getSQLState())) {
                        throw e;
                    }

                    connection.rollback(beforeDuplicate);
                    duplicateRejected = true;
                }

                if (!duplicateRejected
                        || checkIns.findByHabitAndPeriod(
                                connection, habitId,
                                today.minusDays(1), today).size() != 2) {
                    throw new IllegalStateException(
                            "Duplicate check-in protection failed."
                    );
                }

                System.out.println("Duplicate daily check-in rejected.");

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException | RuntimeException e) {
            System.err.println("Check-in repository test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
