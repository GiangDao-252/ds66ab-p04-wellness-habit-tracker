package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;

public final class WellnessPlanUpdateTest {

    public static void main(String[] args) {
        WellnessPlanRepository repository
                = new WellnessPlanRepository();

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                long id = repository.insert(
                        connection, "Original Plan Name"
                );

                boolean updated = repository.updateName(
                        connection, id, "Updated Plan Name"
                );

                if (!updated) {
                    throw new IllegalStateException(
                            "The existing plan was not updated."
                    );
                }

                WellnessPlanRepository.PlanRow plan
                        = repository.findById(connection, id)
                                .orElseThrow(() -> new IllegalStateException(
                                        "The updated plan was not found."
                                ));

                if (!plan.name().equals("Updated Plan Name")) {
                    throw new IllegalStateException(
                            "The stored plan name is incorrect."
                    );
                }

                System.out.println("Plan name update successful.");

                if (repository.updateName(
                        connection, -1L, "Missing Plan")) {
                    throw new IllegalStateException(
                            "Updating a missing plan returned true."
                    );
                }

                System.out.println("Missing plan update handled.");

                boolean invalidNameRejected = false;

                try {
                    repository.updateName(connection, id, "   ");
                } catch (IllegalArgumentException expected) {
                    invalidNameRejected = true;
                }

                if (!invalidNameRejected) {
                    throw new IllegalStateException(
                            "A blank plan name was accepted."
                    );
                }

                String storedName
                        = repository.findById(connection, id)
                                .orElseThrow(() -> new IllegalStateException(
                                        "The plan was not found after validation."
                                ))
                                .name();

                if (!storedName.equals("Updated Plan Name")) {
                    throw new IllegalStateException(
                            "Invalid input changed the stored name."
                    );
                }

                System.out.println("Blank name rejected without changes.");

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException
                | IllegalArgumentException
                | IllegalStateException e) {
            System.err.println("Plan update test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
