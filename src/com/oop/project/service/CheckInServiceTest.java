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

public final class CheckInServiceTest {

    @FunctionalInterface
    private interface Attempt {

        void run() throws SQLException;
    }

    private static void expectRejected(
            Attempt attempt, String label
    ) throws SQLException {

        try {
            attempt.run();
        } catch (WellnessException expected) {
            System.out.println(label);
            return;
        }

        throw new IllegalStateException(
                "Expected rejection: " + label
        );
    }

    public static void main(String[] args) {
        WellnessPlanRepository plans
                = new WellnessPlanRepository();

        HabitRepository habits = new HabitRepository();
        CheckInRepository checkIns = new CheckInRepository();
        CheckInService service = new CheckInService();

        LocalDate today = LocalDate.now();

        try {
            // Commit fixtures so the service can see them.
            long[] ids = Transactions.execute(connection -> {
                long planId = plans.insert(
                        connection, "Check-in Service Test Plan"
                );

                long habitId = habits.insert(
                        connection,
                        new HabitRow(
                                0, planId, "Drink water",
                                Frequency.DAILY,
                                new BigDecimal("2000"),
                                "ml",
                                today.minusDays(1),
                                today.plusDays(7),
                                true
                        )
                );

                return new long[]{planId, habitId};
            });

            long planId = ids[0];
            long habitId = ids[1];

            try {
                long checkInId = service.add(
                        habitId, today, BigDecimal.ZERO, ""
                );

                try (Connection connection = ConnectionFactory.open()) {
                    var loaded = checkIns.findById(
                            connection, checkInId, habitId
                    ).orElseThrow();

                    if (loaded.measurement().signum() != 0) {
                        throw new IllegalStateException(
                                "The committed measurement is incorrect."
                        );
                    }
                }

                System.out.println("Valid check-in committed and reloaded.");

                expectRejected(
                        () -> service.add(
                                habitId, today.plusDays(1), BigDecimal.ONE, ""
                        ),
                        "Future date rejected."
                );

                expectRejected(
                        () -> service.add(
                                habitId, today.minusDays(2), BigDecimal.ONE, ""
                        ),
                        "Date outside active period rejected."
                );

                expectRejected(
                        () -> service.add(
                                habitId, today, new BigDecimal("-1"), ""
                        ),
                        "Negative measurement rejected."
                );

                expectRejected(
                        () -> service.add(
                                habitId, today, BigDecimal.ONE, ""
                        ),
                        "Duplicate daily check-in rejected."
                );

                Transactions.execute(connection -> {
                    if (!habits.setActive(connection, habitId, false)) {
                        throw new IllegalStateException(
                                "The test habit was not deactivated."
                        );
                    }

                    return null;
                });

                expectRejected(
                        () -> service.add(
                                habitId, today.minusDays(1), BigDecimal.ONE, ""
                        ),
                        "Inactive habit rejected."
                );

                try (Connection connection = ConnectionFactory.open()) {
                    if (checkIns.findByHabitAndPeriod(
                            connection, habitId,
                            today.minusDays(7), today.plusDays(7)
                    ).size() != 1) {
                        throw new IllegalStateException(
                                "Rejected operations changed the check-in count."
                        );
                    }
                }

                System.out.println("Rejected operations left data unchanged.");

            } finally {
                // Remove only fixtures created by this test.
                Transactions.execute(connection -> {
                    try (
                            PreparedStatement statement
                            = connection.prepareStatement(
                                    "DELETE FROM check_in WHERE habit_id = ?"
                            )) {
                                statement.setLong(1, habitId);
                                statement.executeUpdate();
                            }

                            if (!habits.delete(connection, habitId)
                                    || !plans.delete(connection, planId)) {
                                throw new IllegalStateException(
                                        "Test fixture cleanup failed."
                                );
                            }

                            return null;
                });
            }

            System.out.println("Test fixtures removed.");

        } catch (SQLException | RuntimeException e) {
            System.err.println("Check-in service test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
