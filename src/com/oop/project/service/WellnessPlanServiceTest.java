package com.oop.project.service;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.repository.EvaluationSettingRepository;
import com.oop.project.repository.EvaluationSettingRepository.StrategyType;
import com.oop.project.repository.WellnessPlanRepository;
import java.sql.Connection;
import java.sql.SQLException;

public final class WellnessPlanServiceTest {

    public static void main(String[] args) {
        WellnessPlanService service = new WellnessPlanService();

        WellnessPlanRepository plans
                = new WellnessPlanRepository();

        EvaluationSettingRepository settings
                = new EvaluationSettingRepository();

        try {
            String name = "JDBC Service Demo Plan";

            long planId = service.createPlan(name);

            // Verify committed data through a new connection.
            try (Connection connection = ConnectionFactory.open()) {

                WellnessPlanRepository.PlanRow plan
                        = plans.findById(connection, planId)
                                .orElseThrow(() -> new IllegalStateException(
                                        "The committed plan was not found."
                                ));

                EvaluationSettingRepository.SettingRow setting
                        = settings.findByPlanId(connection, planId)
                                .orElseThrow(() -> new IllegalStateException(
                                        "The default setting was not found."
                                ));

                if (!plan.name().equals(name)) {
                    throw new IllegalStateException(
                            "The plan name is incorrect."
                    );
                }

                if (setting.planId() != planId
                        || setting.strategyType()
                        != StrategyType.COMPLETION_PERCENTAGE
                        || setting.configValue() != 7) {
                    throw new IllegalStateException(
                            "The default setting is incorrect."
                    );
                }

                System.out.println(
                        "Plan and default setting committed successfully."
                );
                System.out.println(
                        "Reload through a new connection successful."
                );
                System.out.println("Created plan ID: " + planId);
            }

        } catch (SQLException | RuntimeException e) {
            System.err.println("Plan service test failed.");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
