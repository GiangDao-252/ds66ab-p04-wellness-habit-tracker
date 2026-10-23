package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;

public final class WellnessPlanDeleteTest {

    public static void main(String[] args) {
        WellnessPlanRepository repository
                = new WellnessPlanRepository();

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                // Test deleting a plan without related records.
                long emptyPlanId = repository.insert(
                        connection, "Empty Plan Delete Test"
                );

                if (!repository.delete(connection, emptyPlanId)) {
                    throw new IllegalStateException(
                            "The empty plan was not deleted."
                    );
                }

                if (repository.findById(
                        connection, emptyPlanId).isPresent()) {
                    throw new IllegalStateException(
                            "The deleted plan still exists."
                    );
                }

                System.out.println("Empty plan deletion successful.");

                // Test deleting a missing plan.
                if (repository.delete(connection, emptyPlanId)) {
                    throw new IllegalStateException(
                            "Deleting a missing plan returned true."
                    );
                }

                System.out.println("Missing plan deletion handled.");

                // Create a plan with a related evaluation setting.
                long linkedPlanId = repository.insert(
                        connection, "Linked Plan Delete Test"
                );

                String sql = """
                    INSERT INTO evaluation_setting (
                        plan_id, strategy_type, config_value
                    )
                    VALUES (?, ?, ?)
                    """;

                try (
                        PreparedStatement statement
                        = connection.prepareStatement(sql)) {
                    statement.setLong(1, linkedPlanId);
                    statement.setString(2, "COMPLETION_PERCENTAGE");
                    statement.setInt(3, 7);
                    statement.executeUpdate();
                }

                // Recover from the expected foreign-key error.
                Savepoint beforeDelete = connection.setSavepoint();
                boolean deletionBlocked = false;

                try {
                    repository.delete(connection, linkedPlanId);
                } catch (SQLException e) {
                    if (!"23503".equals(e.getSQLState())) {
                        throw e;
                    }

                    connection.rollback(beforeDelete);
                    deletionBlocked = true;
                }

                if (!deletionBlocked) {
                    throw new IllegalStateException(
                            "Deleting a linked plan was not blocked."
                    );
                }

                if (repository.findById(
                        connection, linkedPlanId).isEmpty()) {
                    throw new IllegalStateException(
                            "The linked plan was unexpectedly removed."
                    );
                }

                System.out.println("Linked plan deletion blocked.");

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException
                | IllegalArgumentException
                | IllegalStateException e) {
            System.err.println("Plan deletion test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
