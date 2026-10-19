package com.oop.project.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class WellnessPlanRepository {

    //represents one plan row read from the database.
    public record PlanRow(
            long id,
            String name,
            Instant createdAt
            ) {

    }

    //inserts a plan and returns its generated ID.
    public long insert(Connection connection, String name)
            throws SQLException {

        if (name == null || name.isBlank() || name.length() > 120) {
            throw new IllegalArgumentException(
                    "Plan name must contain 1 to 120 characters."
            );
        }

        String sql = """
            INSERT INTO wellness_plan (name)
            VALUES (?)
            RETURNING id
            """;

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setString(1, name.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("id");
                }

                throw new SQLException(
                        "Creating the plan did not return an ID."
                );
            }
        }
    }

    //finds a plan by ID, returning an empty Optional if absent.
    public Optional<PlanRow> findById(
            Connection connection, long id
    ) throws SQLException {

        String sql = """
            SELECT id, name, created_at
            FROM wellness_plan
            WHERE id = ?
            """;

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    PlanRow plan = new PlanRow(
                            result.getLong("id"),
                            result.getString("name"),
                            result.getTimestamp("created_at").toInstant()
                    );

                    return Optional.of(plan);
                }

                return Optional.empty();
            }
        }
    }

    //returns all plans ordered by ID.
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
