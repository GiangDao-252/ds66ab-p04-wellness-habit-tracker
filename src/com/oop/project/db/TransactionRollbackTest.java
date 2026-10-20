package com.oop.project.db;

import com.oop.project.repository.EvaluationSettingRepository;
import com.oop.project.repository.WellnessPlanRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class TransactionRollbackTest {

    public static void main(String[] args) {
        WellnessPlanRepository plans
                = new WellnessPlanRepository();

        EvaluationSettingRepository settings
                = new EvaluationSettingRepository();

        long[] insertedPlanId = {0};

        try {
            boolean expectedFailure = false;

            try {
                Transactions.execute(connection -> {
                    long planId = plans.insert(
                            connection, "Rollback Test Plan"
                    );

                    insertedPlanId[0] = planId;

                    String sql = """
                        INSERT INTO evaluation_setting (
                            plan_id, strategy_type, config_value
                        )
                        VALUES (?, ?, ?)
                        """;

                    try (
                            PreparedStatement statement
                            = connection.prepareStatement(sql)) {
                        statement.setLong(1, planId);
                        statement.setString(
                                2, "COMPLETION_PERCENTAGE"
                        );

                        // Deliberately violate the database CHECK.
                        statement.setInt(3, 0);
                        statement.executeUpdate();
                    }

                    return planId;
                });

            } catch (SQLException e) {
                if (!"23514".equals(e.getSQLState())) {
                    throw e;
                }

                expectedFailure = true;
                System.out.println(
                        "Expected database constraint failure received."
                );
            }

            if (!expectedFailure || insertedPlanId[0] == 0) {
                throw new IllegalStateException(
                        "The expected second-write failure did not occur."
                );
            }

            try (Connection connection = ConnectionFactory.open()) {
                long planId = insertedPlanId[0];

                if (plans.findById(connection, planId).isPresent()) {
                    throw new IllegalStateException(
                            "The plan remained after rollback."
                    );
                }

                if (settings.findByPlanId(
                        connection, planId).isPresent()) {
                    throw new IllegalStateException(
                            "The setting remained after rollback."
                    );
                }
            }

            System.out.println("Inserted plan was rolled back.");
            System.out.println("No evaluation setting was saved.");
            System.out.println("Transaction rollback test successful.");

        } catch (SQLException | RuntimeException e) {
            System.err.println("Transaction rollback test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
