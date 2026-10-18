package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;

public final class WellnessPlanInsertTest {

    public static void main(String[] args) {
        WellnessPlanRepository repository
                = new WellnessPlanRepository();

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                String name = "JDBC Insert Test Plan";

                long id = repository.insert(connection, name);

                boolean found = false;

                for (WellnessPlanRepository.PlanRow plan
                        : repository.findAll(connection)) {

                    if (plan.id() == id && plan.name().equals(name)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    throw new IllegalStateException(
                            "The inserted plan could not be read back."
                    );
                }

                System.out.println("Insert and read-back successful.");
                System.out.println("Generated plan ID: " + id);

            } finally {
                connection.rollback();
            }

            System.out.println("Test data rolled back.");

        } catch (SQLException
                | IllegalArgumentException
                | IllegalStateException e) {
            System.err.println("Plan insert test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
