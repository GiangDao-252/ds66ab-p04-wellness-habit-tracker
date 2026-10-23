package com.oop.project.service;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.db.Transactions;
import com.oop.project.exception.WellnessException;
import com.oop.project.repository.CheckInRepository;
import com.oop.project.repository.HabitRepository;
import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import com.oop.project.repository.WellnessPlanRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public final class CheckInServiceDeleteTest {

    @FunctionalInterface
    private interface Attempt {

        void run() throws SQLException;
    }

    public static void main(String[] args) {
        try {
            runTest();
        } catch (SQLException | RuntimeException e) {
            System.err.println("Check-in service delete test failed.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runTest() throws SQLException {
        WellnessPlanRepository plans = new WellnessPlanRepository();
        HabitRepository habits = new HabitRepository();
        CheckInRepository checkIns = new CheckInRepository();
        CheckInService service = new CheckInService();

        LocalDate today = LocalDate.now();

        long[] ids = Transactions.execute(connection -> {
            long planId = plans.insert(
                    connection, "Check-In Delete Test Plan"
            );

            long habitId = habits.insert(connection, new HabitRow(
                    0,
                    planId,
                    "Walking",
                    Frequency.DAILY,
                    new BigDecimal("5000"),
                    "steps",
                    today.minusDays(7),
                    today,
                    true
            ));

            long otherHabitId = habits.insert(connection, new HabitRow(
                    0,
                    planId,
                    "Reading",
                    Frequency.DAILY,
                    new BigDecimal("20"),
                    "minutes",
                    today.minusDays(7),
                    today,
                    true
            ));

            return new long[]{planId, habitId, otherHabitId};
        });

        long planId = ids[0];
        long habitId = ids[1];
        long otherHabitId = ids[2];

        try {
            long checkInId = service.add(
                    habitId,
                    today,
                    new BigDecimal("3500"),
                    "Delete test"
            );

            expectRejected(
                    () -> service.delete(checkInId, otherHabitId),
                    "Wrong habit deletion rejected."
            );

            try (Connection connection = ConnectionFactory.open()) {
                if (checkIns.findById(
                        connection, checkInId, habitId
                ).isEmpty()) {
                    throw new IllegalStateException(
                            "The rejected deletion removed the check-in."
                    );
                }
            }

            System.out.println(
                    "Rejected deletion left the check-in unchanged."
            );

            Transactions.execute(connection -> {
                if (!habits.setActive(connection, habitId, false)) {
                    throw new IllegalStateException(
                            "The test habit could not be deactivated."
                    );
                }

                return null;
            });

            service.delete(checkInId, habitId);

            try (Connection connection = ConnectionFactory.open()) {
                if (checkIns.findById(
                        connection, checkInId, habitId
                ).isPresent()) {
                    throw new IllegalStateException(
                            "The deleted check-in still exists."
                    );
                }
            }

            System.out.println(
                    "Inactive habit check-in deletion committed and verified."
            );

            expectRejected(
                    () -> service.delete(checkInId, habitId),
                    "Repeated deletion rejected."
            );

            expectRejected(
                    () -> service.delete(checkInId, -1L),
                    "Missing habit deletion rejected."
            );

        } finally {
            Transactions.execute(connection -> {
                String sql = """
                        DELETE FROM check_in
                        WHERE habit_id IN (?, ?)
                        """;

                try (PreparedStatement statement
                        = connection.prepareStatement(sql)) {
                    statement.setLong(1, habitId);
                    statement.setLong(2, otherHabitId);
                    statement.executeUpdate();
                }

                habits.delete(connection, habitId);
                habits.delete(connection, otherHabitId);
                plans.delete(connection, planId);

                return null;
            });

            System.out.println("Test fixtures removed.");
        }
    }

    private static void expectRejected(
            Attempt attempt, String message
    ) throws SQLException {
        try {
            attempt.run();
        } catch (WellnessException e) {
            System.out.println(message);
            return;
        }

        throw new IllegalStateException(
                "An invalid deletion was unexpectedly accepted."
        );
    }
}
