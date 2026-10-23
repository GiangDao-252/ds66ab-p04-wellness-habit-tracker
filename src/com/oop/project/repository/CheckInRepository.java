package com.oop.project.repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class CheckInRepository {

    public record CheckInRow(
            long id,
            long habitId,
            LocalDate checkDate,
            BigDecimal measurement,
            String note
            ) {

    }

    // Inserts a daily check-in and returns its generated ID.
    public long insert(Connection connection, CheckInRow checkIn)
            throws SQLException {

        validate(checkIn);

        String sql = """
            INSERT INTO check_in (
                habit_id, check_date, measurement, note
            )
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setLong(1, checkIn.habitId());
            statement.setObject(2, checkIn.checkDate());
            statement.setBigDecimal(3, checkIn.measurement());
            statement.setString(
                    4, checkIn.note() == null ? "" : checkIn.note()
            );

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("id");
                }

                throw new SQLException(
                        "Creating the check-in did not return an ID."
                );
            }
        }
    }

    // Returns check-ins within an inclusive date range.
    public List<CheckInRow> findByHabitAndPeriod(
            Connection connection,
            long habitId,
            LocalDate from,
            LocalDate to
    ) throws SQLException {

        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException(
                    "The check-in query period is invalid."
            );
        }

        String sql = """
            SELECT id, habit_id, check_date, measurement, note
            FROM check_in
            WHERE habit_id = ?
              AND check_date BETWEEN ? AND ?
            ORDER BY check_date, id
            """;

        List<CheckInRow> checkIns = new ArrayList<>();

        try (
                PreparedStatement statement
                = connection.prepareStatement(sql)) {
            statement.setLong(1, habitId);
            statement.setObject(2, from);
            statement.setObject(3, to);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    checkIns.add(mapRow(result));
                }
            }
        }

        return checkIns;
    }

    private CheckInRow mapRow(ResultSet result)
            throws SQLException {

        return new CheckInRow(
                result.getLong("id"),
                result.getLong("habit_id"),
                result.getObject("check_date", LocalDate.class),
                result.getBigDecimal("measurement"),
                result.getString("note")
        );
    }

    private void validate(CheckInRow checkIn) {
        if (checkIn == null) {
            throw new IllegalArgumentException(
                    "Check-in data is required."
            );
        }

        if (checkIn.checkDate() == null) {
            throw new IllegalArgumentException(
                    "The check-in date is required."
            );
        }

        if (checkIn.measurement() == null
                || checkIn.measurement().signum() < 0) {
            throw new IllegalArgumentException(
                    "Measurement must be greater than or equal to zero."
            );
        }

        if (checkIn.note() != null
                && checkIn.note().length() > 1000) {
            throw new IllegalArgumentException(
                    "The note must not exceed 1000 characters."
            );
        }
    }
}
