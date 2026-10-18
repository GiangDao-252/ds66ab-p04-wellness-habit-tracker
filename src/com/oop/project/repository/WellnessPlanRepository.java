package com.oop.project.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class WellnessPlanRepository {

    public record PlanRow(
            long id,
            String name,
            Instant createdAt
            ) {

    }

    public List<PlanRow> findAll(Connection connection)
            throws SQLException {

        String sql = """
            SELECT id, name, created_at
            FROM wellness_plan
            ORDER BY id
            """;

        List<PlanRow> plans = new ArrayList<>();

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql); ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                PlanRow plan = new PlanRow(
                        result.getLong("id"),
                        result.getString("name"),
                        result.getTimestamp("created_at").toInstant()
                );

                plans.add(plan);
            }
        }

        return plans;
    }
}
