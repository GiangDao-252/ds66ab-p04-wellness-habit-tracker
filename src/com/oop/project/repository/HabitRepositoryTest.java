package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class HabitRepositoryTest {

    public static void main(String[] args) {
        WellnessPlanRepository plans
                = new WellnessPlanRepository();

        HabitRepository habits = new HabitRepository();

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                long planId = plans.insert(
                        connection, "Habit Repository Test Plan"
                );

                LocalDate from = LocalDate.now().minusDays(7);
                LocalDate to = LocalDate.now().plusDays(30);

                HabitRow input = new HabitRow(
                        0,
                        planId,
                        "Sleep",
                        Frequency.DAILY,
                        new BigDecimal("7.5"),
                        "hours",
                        from,
                        to,
                        true
                );

                long habitId = habits.insert(connection, input);

                List<HabitRow> loaded
                        = habits.findByPlanId(connection, planId);

                if (loaded.size() != 1) {
                    throw new IllegalStateException(
                            "Expected exactly one habit."
                    );
                }

                HabitRow habit = loaded.get(0);

                if (habit.id() != habitId
                        || habit.planId() != planId
                        || !habit.name().equals("Sleep")
                        || habit.frequency() != Frequency.DAILY
                        || habit.targetValue().compareTo(
                                new BigDecimal("7.5")) != 0
                        || !habit.unit().equals("hours")
                        || !habit.activeFrom().equals(from)
                        || !habit.activeTo().equals(to)
                        || !habit.active()) {
                    throw new IllegalStateException(
                            "The loaded habit does not match."
                    );
                }

                System.out.println(
                        "Habit insertion and row mapping successful."
                );

                long otherPlanId = plans.insert(
                        connection, "Empty Habit Test Plan"
                );

                if (!habits.findByPlanId(
                        connection, otherPlanId).isEmpty()) {
                    throw new IllegalStateException(
                            "Habits from another plan were returned."
                    );
                }

                System.out.println("Plan filtering successful.");

                boolean rejected = false;

                try {
                    habits.insert(connection, new HabitRow(
                            0,
                            planId,
                            "Invalid Habit",
                            Frequency.DAILY,
                            BigDecimal.ZERO,
                            "hours",
                            from,
                            to,
                            true
                    ));
                } catch (IllegalArgumentException expected) {
                    rejected = true;
                }

                if (!rejected) {
                    throw new IllegalStateException(
                            "A zero target was accepted."
                    );
                }

                if (habits.findByPlanId(
                        connection, planId).size() != 1) {
                    throw new IllegalStateException(
                            "Invalid input changed the habit count."
                    );
                }

                System.out.println("Invalid target rejected.");

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException | RuntimeException e) {
            System.err.println("Habit repository test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
