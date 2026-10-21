package com.oop.project.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HabitRepository {

    public enum Frequency {
        DAILY,
        WEEKLY
    }

    public record HabitRow(
            long id,
            long planId,
            String name,
            Frequency frequency,
            BigDecimal targetValue,
            String unit,
            LocalDate activeFrom,
            LocalDate activeTo,
            boolean active
            ) {

    }

    // Inserts a habit and returns its generated ID.
    public long insert(Connection connection, HabitRow habit)
            throws SQLException {

        validate(habit);

        String sql = """
            INSERT INTO habit (
                plan_id, name, frequency, target_value,
                unit, active_from, active_to, active
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setLong(1, habit.planId());
            statement.setString(2, habit.name().trim());
            statement.setString(3, habit.frequency().name());
            statement.setBigDecimal(4, habit.targetValue());
            statement.setString(5, habit.unit().trim());
            statement.setObject(6, habit.activeFrom());
            statement.setObject(7, habit.activeTo());
            statement.setBoolean(8, habit.active());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("id");
                }

                throw new SQLException(
                        "Creating the habit did not return an ID."
                );
            }
        }
    }

    // Returns habits belonging to the selected plan.
    public List<HabitRow> findByPlanId(
            Connection connection, long planId
    ) throws SQLException {

        String sql = """
            SELECT id, plan_id, name, frequency, target_value,
                   unit, active_from, active_to, active
            FROM habit
            WHERE plan_id = ?
            ORDER BY id
            """;

        List<HabitRow> habits = new ArrayList<>();

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setLong(1, planId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    habits.add(mapRow(result));
                }
            }
        }

        return habits;
    }

    // Finds one habit by its ID.
    public Optional<HabitRow> findById(
            Connection connection, long id
    ) throws SQLException {

        String sql = """
        SELECT id, plan_id, name, frequency, target_value,
               unit, active_from, active_to, active
        FROM habit
        WHERE id = ?
        """;

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapRow(result));
                }

                return Optional.empty();
            }
        }
    }

// Updates the target without changing its measurement unit.
    public boolean updateTarget(
            Connection connection, long id, BigDecimal target
    ) throws SQLException {

        if (target == null || target.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Habit target must be greater than zero."
            );
        }

        String sql = """
        UPDATE habit
        SET target_value = ?
        WHERE id = ?
        """;

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, target);
            statement.setLong(2, id);

            return statement.executeUpdate() == 1;
        }
    }

    // Enables or disables tracking while preserving habit history.
    public boolean setActive(
            Connection connection, long id, boolean active
    ) throws SQLException {

        String sql = """
            UPDATE habit
            SET active = ?
            WHERE id = ?
            """;

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setLong(2, id);

            return statement.executeUpdate() == 1;
        }
    }

    // Converts one database row into a Java record.
    private HabitRow mapRow(ResultSet result)
            throws SQLException {

        return new HabitRow(
                result.getLong("id"),
                result.getLong("plan_id"),
                result.getString("name"),
                Frequency.valueOf(result.getString("frequency")),
                result.getBigDecimal("target_value"),
                result.getString("unit"),
                result.getObject("active_from", LocalDate.class),
                result.getObject("active_to", LocalDate.class),
                result.getBoolean("active")
        );
    }

    private void validate(HabitRow habit) {
        if (habit == null) {
            throw new IllegalArgumentException(
                    "Habit data is required."
            );
        }

        if (habit.name() == null
                || habit.name().isBlank()
                || habit.name().length() > 120) {
            throw new IllegalArgumentException(
                    "Habit name must contain 1 to 120 characters."
            );
        }

        if (habit.frequency() == null) {
            throw new IllegalArgumentException(
                    "Habit frequency is required."
            );
        }

        if (habit.targetValue() == null
                || habit.targetValue().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Habit target must be greater than zero."
            );
        }

        if (habit.unit() == null
                || habit.unit().isBlank()
                || habit.unit().length() > 40) {
            throw new IllegalArgumentException(
                    "Habit unit must contain 1 to 40 characters."
            );
        }

        if (habit.activeFrom() == null
                || habit.activeTo() == null
                || habit.activeTo().isBefore(habit.activeFrom())) {
            throw new IllegalArgumentException(
                    "The habit active period is invalid."
            );
        }
    }
}
