package com.oop.project.service;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.db.Transactions;
import com.oop.project.exception.WellnessException;
import com.oop.project.repository.CheckInRepository;
import com.oop.project.repository.CheckInRepository.CheckInRow;
import com.oop.project.repository.HabitRepository;
import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import com.oop.project.repository.WellnessPlanRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public final class CheckInServiceUpdateTest {

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
        LocalDate yesterday = today.minusDays(1);

        try {
            long[] ids = Transactions.execute(connection -> {
                long planId = plans.insert(
                        connection, "Check-in Service Update Test Plan"
                );

                long habitId = habits.insert(
                        connection,
                        new HabitRow(
                                0, planId, "Drink water",
                                Frequency.DAILY,
                                new BigDecimal("2000"),
                                "ml",
                                today.minusDays(7),
                                today.plusDays(7),
                                true
                        )
                );

                return new long[]{planId, habitId};
            });

            long planId = ids[0];
            long habitId = ids[1];

            try {
                long firstId = service.add(
                        habitId, yesterday,
                        new BigDecimal("1000"), "Original note"
                );

                service.add(
                        habitId, today,
                        new BigDecimal("2000"), "Today's entry"
                );

                service.update(new CheckInRow(
                        firstId, habitId, yesterday,
                        new BigDecimal("1750.5"), "Updated note"
                ));

                CheckInRow expected;

                try (Connection connection = ConnectionFactory.open()) {
                    expected = checkIns.findById(
                            connection, firstId, habitId
                    ).orElseThrow();

                    if (!expected.checkDate().equals(yesterday)
                            || expected.measurement().compareTo(
                                    new BigDecimal("1750.5")) != 0
                            || !expected.note().equals("Updated note")) {
                        throw new IllegalStateException(
                                "The committed update is incorrect."
                        );
                    }
                }

                System.out.println("Valid update committed and reloaded.");

                expectRejected(
                        () -> service.update(new CheckInRow(
                                firstId, habitId, today.plusDays(1),
                                BigDecimal.ONE, ""
                        )),
                        "Future date update rejected."
                );

                expectRejected(
                        () -> service.update(new CheckInRow(
                                firstId, habitId, today.minusDays(8),
                                BigDecimal.ONE, ""
                        )),
                        "Outside-period update rejected."
                );

                expectRejected(
                        () -> service.update(new CheckInRow(
                                firstId, habitId, yesterday,
                                new BigDecimal("-1"), ""
                        )),
                        "Negative update rejected."
                );

                expectRejected(
                        () -> service.update(new CheckInRow(
                                firstId, habitId, today,
                                BigDecimal.ONE, "Duplicate date"
                        )),
                        "Duplicate date update rejected."
                );

                expectRejected(
                        () -> service.update(new CheckInRow(
                                -1L, habitId, yesterday,
                                BigDecimal.ONE, ""
                        )),
                        "Missing check-in update rejected."
                );

                expectRejected(
                        () -> service.update(new CheckInRow(
                                firstId, -1L, yesterday,
                                BigDecimal.ONE, ""
                        )),
                        "Wrong habit update rejected."
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
                        () -> service.update(new CheckInRow(
                                firstId, habitId, yesterday,
                                BigDecimal.ONE, ""
                        )),
                        "Inactive habit update rejected."
                );

                try (Connection connection = ConnectionFactory.open()) {
                    CheckInRow actual = checkIns.findById(
                            connection, firstId, habitId
                    ).orElseThrow();

                    if (!actual.equals(expected)
                            || checkIns.findByHabitAndPeriod(
                                    connection, habitId,
                                    yesterday, today).size() != 2) {
                        throw new IllegalStateException(
                                "Rejected updates changed the stored data."
                        );
                    }
                }

                System.out.println("Rejected updates left data unchanged.");

            } finally {
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
            System.err.println("Check-in service update test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
