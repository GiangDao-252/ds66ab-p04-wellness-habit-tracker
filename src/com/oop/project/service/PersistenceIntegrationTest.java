package com.oop.project.service;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.db.Transactions;
import com.oop.project.repository.CheckInRepository;
import com.oop.project.repository.CheckInRepository.CheckInRow;
import com.oop.project.repository.EvaluationSettingRepository;
import com.oop.project.repository.HabitRepository;
import com.oop.project.repository.HabitRepository.Frequency;
import com.oop.project.repository.HabitRepository.HabitRow;
import com.oop.project.repository.WellnessPlanRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public final class PersistenceIntegrationTest {

    public static void main(String[] args) {
        try {
            runTest();
            System.out.println("Persistence integration test passed.");
        } catch (SQLException | RuntimeException e) {
            System.err.println("Persistence integration test failed.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void runTest() throws SQLException {
        WellnessPlanService planService = new WellnessPlanService();
        CheckInService checkInService = new CheckInService();

        WellnessPlanRepository plans = new WellnessPlanRepository();
        EvaluationSettingRepository settings
                = new EvaluationSettingRepository();
        HabitRepository habits = new HabitRepository();
        CheckInRepository checkIns = new CheckInRepository();

        LocalDate today = LocalDate.now();
        String planName = "Persistence Integration Test Plan";

        long planId = planService.createPlan(planName);

        try {
            // Reload committed data through a new connection.
            try (Connection connection = ConnectionFactory.open()) {
                var plan = plans.findById(connection, planId)
                        .orElseThrow(() -> new IllegalStateException(
                                "The committed plan was not found."
                        ));

                require(
                        plan.name().equals(planName),
                        "The stored plan name does not match."
                );

                var setting = settings.findByPlanId(connection, planId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Default evaluation settings were not found."
                        ));

                require(
                        setting.strategyType()
                        == EvaluationSettingRepository.StrategyType.COMPLETION_PERCENTAGE,
                        "The default evaluation strategy does not match."
                );

                require(
                        setting.configValue() == 7,
                        "The default configuration value does not match."
                );
            }

            System.out.println("Plan and default settings reloaded.");

            long habitId = Transactions.execute(connection
                    -> habits.insert(connection, new HabitRow(
                            0,
                            planId,
                            "Walking",
                            Frequency.DAILY,
                            new BigDecimal("5000"),
                            "steps",
                            today.minusDays(7),
                            today.plusDays(7),
                            true
                    ))
            );

            try (Connection connection = ConnectionFactory.open()) {
                HabitRow habit = habits.findById(connection, habitId)
                        .orElseThrow(() -> new IllegalStateException(
                                "The committed habit was not found."
                        ));

                require(
                        habit.planId() == planId,
                        "The habit belongs to an unexpected plan."
                );

                require(
                        habit.targetValue().compareTo(
                                new BigDecimal("5000")
                        ) == 0,
                        "The stored habit target does not match."
                );
            }

            System.out.println("Habit committed and reloaded.");

            long checkInId = checkInService.add(
                    habitId,
                    today,
                    new BigDecimal("3000"),
                    "Initial measurement"
            );

            verifyCheckIn(
                    checkIns,
                    checkInId,
                    habitId,
                    today,
                    new BigDecimal("3000"),
                    "Initial measurement"
            );

            System.out.println("Check-in insertion committed and reloaded.");

            checkInService.update(new CheckInRow(
                    checkInId,
                    habitId,
                    today,
                    new BigDecimal("4200.5"),
                    "Updated measurement"
            ));

            verifyCheckIn(
                    checkIns,
                    checkInId,
                    habitId,
                    today,
                    new BigDecimal("4200.5"),
                    "Updated measurement"
            );

            System.out.println("Check-in update committed and reloaded.");

            checkInService.delete(checkInId, habitId);

            try (Connection connection = ConnectionFactory.open()) {
                require(
                        checkIns.findById(
                                connection, checkInId, habitId
                        ).isEmpty(),
                        "The deleted check-in still exists."
                );

                require(
                        habits.findById(connection, habitId).isPresent(),
                        "Deleting the check-in unexpectedly removed its habit."
                );

                require(
                        plans.findById(connection, planId).isPresent(),
                        "Deleting the check-in unexpectedly removed its plan."
                );
            }

            System.out.println(
                    "Check-in deletion verified; habit and plan preserved."
            );

        } finally {
            removeFixtures(planId);
            System.out.println("Integration test fixtures removed.");
        }
    }

    private static void verifyCheckIn(
            CheckInRepository repository,
            long checkInId,
            long habitId,
            LocalDate expectedDate,
            BigDecimal expectedMeasurement,
            String expectedNote
    ) throws SQLException {
        try (Connection connection = ConnectionFactory.open()) {
            CheckInRow row = repository.findById(
                    connection, checkInId, habitId
            ).orElseThrow(() -> new IllegalStateException(
                    "The committed check-in was not found."
            ));

            require(
                    row.checkDate().equals(expectedDate),
                    "The stored check-in date does not match."
            );

            require(
                    row.measurement().compareTo(expectedMeasurement) == 0,
                    "The stored measurement does not match."
            );

            require(
                    row.note().equals(expectedNote),
                    "The stored note does not match."
            );
        }
    }

    private static void removeFixtures(long planId) throws SQLException {
        Transactions.execute(connection -> {
            executeDelete(connection, """
                    DELETE FROM check_in
                    WHERE habit_id IN (
                        SELECT id FROM habit WHERE plan_id = ?
                    )
                    """, planId);

            executeDelete(connection, """
                    DELETE FROM habit WHERE plan_id = ?
                    """, planId);

            executeDelete(connection, """
                    DELETE FROM evaluation_setting WHERE plan_id = ?
                    """, planId);

            executeDelete(connection, """
                    DELETE FROM wellness_plan WHERE id = ?
                    """, planId);

            return null;
        });
    }

    private static void executeDelete(
            Connection connection, String sql, long planId
    ) throws SQLException {
        try (PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);
            statement.executeUpdate();
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
