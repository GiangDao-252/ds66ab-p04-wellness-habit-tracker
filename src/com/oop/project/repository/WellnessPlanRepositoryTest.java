package com.oop.project.repository;

import com.oop.project.db.ConnectionFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class WellnessPlanRepositoryTest {

    public static void main(String[] args) {
        WellnessPlanRepository repository
                = new WellnessPlanRepository();

        try (Connection connection = ConnectionFactory.open()) {

            List<WellnessPlanRepository.PlanRow> plans
                    = repository.findAll(connection);

            System.out.println("Plans found: " + plans.size());

            for (WellnessPlanRepository.PlanRow plan : plans) {
                System.out.println(
                        plan.id() + " | "
                        + plan.name() + " | "
                        + plan.createdAt()
                );
            }

        } catch (SQLException | IllegalStateException e) {
            System.err.println("Failed to load wellness plans.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
