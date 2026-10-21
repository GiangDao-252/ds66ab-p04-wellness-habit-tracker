package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public final class HabitUpdateTest {

    public static void main(String[] args) {
        WellnessPlanRepository plans
                = new WellnessPlanRepository();

        HabitRepository habits = new HabitRepository();

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                long planId = plans.insert(
                        connection, "Habit Update Test Plan"
                );

                LocalDate today = LocalDate.now();

                long habitId = habits.insert(
                        connection,
                        new HabitRow(
                                0,
                                planId,
                                "Drink water",
                                Frequency.DAILY,
                                new BigDecimal("2000"),
                                "ml",
                                today,
                                today.plusDays(30),
                                true
                        )
                );

                HabitRow original
                        = habits.findById(connection, habitId)
                                .orElseThrow(() -> new IllegalStateException(
                                        "The inserted habit was not found."
                                ));

                if (!original.name().equals("Drink water")) {
                    throw new IllegalStateException(
                            "The wrong habit was returned."
                    );
                }

                System.out.println("Habit lookup successful.");

                if (!habits.updateTarget(
                        connection, habitId, new BigDecimal("2500"))) {
                    throw new IllegalStateException(
                            "The habit target was not updated."
                    );
                }

                HabitRow updated
                        = habits.findById(connection, habitId).orElseThrow();

                if (updated.targetValue().compareTo(
                        new BigDecimal("2500")) != 0
                        || !updated.unit().equals("ml")) {
                    throw new IllegalStateException(
                            "The target or unit is incorrect."
                    );
                }

                System.out.println("Habit target update successful.");

                if (!habits.setActive(connection, habitId, false)) {
                    throw new IllegalStateException(
                            "The habit was not deactivated."
                    );
                }

                HabitRow inactive
                        = habits.findById(connection, habitId).orElseThrow();

                if (inactive.active()
                        || habits.findByPlanId(
                                connection, planId).size() != 1) {
                    throw new IllegalStateException(
                            "Deactivation did not preserve the habit."
                    );
                }

                System.out.println("Habit deactivated without deletion.");

                if (!habits.setActive(connection, habitId, true)
                        || !habits.findById(
                                connection, habitId).orElseThrow().active()) {
                    throw new IllegalStateException(
                            "The habit was not reactivated."
                    );
                }

                System.out.println("Habit reactivation successful.");

                if (habits.findById(connection, -1L).isPresent()
                        || habits.updateTarget(
                                connection, -1L, BigDecimal.ONE)
                        || habits.setActive(connection, -1L, false)) {
                    throw new IllegalStateException(
                            "A missing habit was not handled correctly."
                    );
                }

                System.out.println("Missing habit handled.");

                boolean rejected = false;

                try {
                    habits.updateTarget(
                            connection, habitId, BigDecimal.ZERO
                    );
                } catch (IllegalArgumentException expected) {
                    rejected = true;
                }

                HabitRow afterInvalidInput
                        = habits.findById(connection, habitId).orElseThrow();

                if (!rejected
                        || afterInvalidInput.targetValue().compareTo(
                                new BigDecimal("2500")) != 0) {
                    throw new IllegalStateException(
                            "Invalid input was accepted or changed the target."
                    );
                }

                System.out.println("Invalid target rejected.");

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException | RuntimeException e) {
            System.err.println("Habit update test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
