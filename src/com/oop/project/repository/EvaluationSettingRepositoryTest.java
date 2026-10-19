package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.repository.EvaluationSettingRepository.SettingRow;
import com.oop.project.repository.EvaluationSettingRepository.StrategyType;
import java.sql.Connection;
import java.sql.SQLException;

public final class EvaluationSettingRepositoryTest {

    public static void main(String[] args) {
        WellnessPlanRepository plans = new WellnessPlanRepository();

        EvaluationSettingRepository settings
                = new EvaluationSettingRepository();

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                long planId = plans.insert(
                        connection, "Evaluation Setting Test Plan"
                );

                if (settings.findByPlanId(
                        connection, planId).isPresent()) {
                    throw new IllegalStateException(
                            "The new plan unexpectedly has a setting."
                    );
                }

                System.out.println("Missing setting handled.");

                SettingRow initial = new SettingRow(
                        planId, StrategyType.COMPLETION_PERCENTAGE, 7
                );

                settings.save(connection, initial);

                SettingRow loaded = settings.findByPlanId(
                        connection, planId
                ).orElseThrow(() -> new IllegalStateException(
                        "The saved setting was not found."
                ));

                if (!loaded.equals(initial)) {
                    throw new IllegalStateException(
                            "The saved setting does not match."
                    );
                }

                System.out.println("Setting insertion successful.");

                SettingRow updated = new SettingRow(
                        planId, StrategyType.STREAK, 14
                );

                settings.save(connection, updated);

                SettingRow reloaded = settings.findByPlanId(
                        connection, planId
                ).orElseThrow(() -> new IllegalStateException(
                        "The updated setting was not found."
                ));

                if (!reloaded.equals(updated)) {
                    throw new IllegalStateException(
                            "The setting was not updated correctly."
                    );
                }

                System.out.println("Setting update successful.");

                boolean rejected = false;

                try {
                    settings.save(connection, new SettingRow(
                            planId, StrategyType.STREAK, 0
                    ));
                } catch (IllegalArgumentException expected) {
                    rejected = true;
                }

                if (!rejected) {
                    throw new IllegalStateException(
                            "A zero-day report window was accepted."
                    );
                }

                SettingRow afterInvalidInput
                        = settings.findByPlanId(connection, planId)
                                .orElseThrow(() -> new IllegalStateException(
                                        "The setting unexpectedly disappeared."
                                ));

                if (!afterInvalidInput.equals(updated)) {
                    throw new IllegalStateException(
                            "Invalid input changed the setting."
                    );
                }

                System.out.println("Invalid report window rejected.");

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException
                | IllegalArgumentException
                | IllegalStateException e) {
            System.err.println("Evaluation setting test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
