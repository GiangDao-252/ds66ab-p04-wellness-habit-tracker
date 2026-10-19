package com.oop.project.service;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.repository.EvaluationSettingRepository;
import com.oop.project.repository.EvaluationSettingRepository.SettingRow;
import com.oop.project.repository.EvaluationSettingRepository.StrategyType;
import com.oop.project.repository.WellnessPlanRepository;
import java.sql.Connection;
import java.sql.SQLException;

public final class WellnessPlanService {

    private final WellnessPlanRepository plans
            = new WellnessPlanRepository();

    private final EvaluationSettingRepository settings
            = new EvaluationSettingRepository();

    public long createPlan(String name) throws SQLException {

        try (Connection connection = ConnectionFactory.open()) {
            connection.setAutoCommit(false);

            try {
                long planId = plans.insert(connection, name);

                SettingRow defaultSetting = new SettingRow(
                        planId,
                        StrategyType.COMPLETION_PERCENTAGE,
                        7
                );

                settings.save(connection, defaultSetting);

                connection.commit();

                return planId;

            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }
        }
    }
}
