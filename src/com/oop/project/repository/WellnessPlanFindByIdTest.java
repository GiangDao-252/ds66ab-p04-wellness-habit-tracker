package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public final class WellnessPlanFindByIdTest {

    public static void main(String[] args) {
        WellnessPlanRepository repository
                = new WellnessPlanRepository();

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                long id = repository.insert(
                        connection, "Find By ID Test Plan"
                );

                Optional<WellnessPlanRepository.PlanRow> result
                        = repository.findById(connection, id);

                if (result.isEmpty()) {
                    throw new IllegalStateException(
                            "The inserted plan was not found."
                    );
                }

                WellnessPlanRepository.PlanRow plan = result.get();

                if (plan.id() != id
                        || !plan.name().equals("Find By ID Test Plan")) {
                    throw new IllegalStateException(
                            "The returned plan does not match."
                    );
                }

                System.out.println("Existing plan lookup successful.");

                if (repository.findById(connection, -1L).isPresent()) {
                    throw new IllegalStateException(
                            "An unexpected plan was found for ID -1."
                    );
                }

                System.out.println("Missing plan lookup successful.");

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException
                | IllegalArgumentException
                | IllegalStateException e) {
            System.err.println("Find-by-ID test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
