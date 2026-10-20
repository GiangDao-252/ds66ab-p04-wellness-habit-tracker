package com.oop.project.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public final class EvaluationSettingRepository {

    public enum StrategyType {
        COMPLETION_PERCENTAGE,
        STREAK
    }

    public record SettingRow(
            long planId,
            StrategyType strategyType,
            int configValue
            ) {

    }

    // Inserts a setting or updates the existing setting for the plan.
    public void save(Connection connection, SettingRow setting)
            throws SQLException {

        if (setting == null || setting.strategyType() == null) {
            throw new IllegalArgumentException(
                    "An evaluation setting and strategy are required."
            );
        }

        if (setting.configValue() <= 0) {
            throw new IllegalArgumentException(
                    "The report window must be greater than zero."
            );
        }

        String sql = """
            INSERT INTO evaluation_setting (
                plan_id, strategy_type, config_value
            )
            VALUES (?, ?, ?)
            ON CONFLICT (plan_id)
            DO UPDATE SET
                strategy_type = EXCLUDED.strategy_type,
                config_value = EXCLUDED.config_value
            """;

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setLong(1, setting.planId());
            statement.setString(2, setting.strategyType().name());
            statement.setInt(3, setting.configValue());

            statement.executeUpdate();
        }
    }

    // Finds the current evaluation setting for a plan.
    public Optional<SettingRow> findByPlanId(
            Connection connection, long planId
    ) throws SQLException {

        String sql = """
            SELECT plan_id, strategy_type, config_value
            FROM evaluation_setting
            WHERE plan_id = ?
            """;

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    SettingRow setting = new SettingRow(
                            result.getLong("plan_id"),
                            StrategyType.valueOf(
                                    result.getString("strategy_type")
                            ),
                            result.getInt("config_value")
                    );

                    return Optional.of(setting);
                }

                return Optional.empty();
            }
        }
    }
}
