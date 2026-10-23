package com.oop.project.service;

import com.oop.project.db.Transactions;
import com.oop.project.exception.WellnessException;
import com.oop.project.repository.CheckInRepository;
import com.oop.project.repository.CheckInRepository.CheckInRow;
import com.oop.project.repository.HabitRepository;
import com.oop.project.repository.HabitRepository.HabitRow;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

public final class CheckInService {

    private final HabitRepository habits = new HabitRepository();

    private final CheckInRepository checkIns
            = new CheckInRepository();

    public long add(
            long habitId,
            LocalDate date,
            BigDecimal measurement,
            String note
    ) throws SQLException {

        validateInput(date, measurement, note);

        try {
            return Transactions.execute(connection -> {
                requireActiveHabit(connection, habitId, date);

                return checkIns.insert(
                        connection,
                        new CheckInRow(
                                0, habitId, date, measurement, note
                        )
                );
            });

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new WellnessException(
                        "This habit already has a check-in for that date. "
                        + "Edit the existing entry.",
                        e
                );
            }

            throw e;
        }
    }

    public void update(CheckInRow checkIn)
            throws SQLException {

        if (checkIn == null) {
            throw new WellnessException(
                    "Check-in data is required."
            );
        }

        validateInput(
                checkIn.checkDate(),
                checkIn.measurement(),
                checkIn.note()
        );

        try {
            Transactions.execute(connection -> {
                requireActiveHabit(
                        connection,
                        checkIn.habitId(),
                        checkIn.checkDate()
                );

                if (!checkIns.update(connection, checkIn)) {
                    throw new WellnessException(
                            "The check-in does not exist for the selected habit."
                    );
                }

                return null;
            });

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new WellnessException(
                        "This habit already has a check-in for that date. "
                        + "Choose another date.",
                        e
                );
            }

            throw e;
        }
    }

    public void delete(long checkInId, long habitId) throws SQLException {
        Transactions.execute(connection -> {
            habits.findByIdForUpdate(connection, habitId)
                    .orElseThrow(() -> new WellnessException(
                            "The selected habit does not exist."
                    ));

            boolean deleted = checkIns.delete(
                    connection, checkInId, habitId
            );

            if (!deleted) {
                throw new WellnessException(
                        "The check-in does not exist for the selected habit."
                );
            }

            return null;
        });
    }

    // Validates input shared by creation and update.
    private void validateInput(
            LocalDate date,
            BigDecimal measurement,
            String note
    ) {
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new WellnessException(
                    "The check-in date is required and cannot be in the future."
            );
        }

        if (measurement == null || measurement.signum() < 0) {
            throw new WellnessException(
                    "Measurement must be greater than or equal to zero."
            );
        }

        if (note != null && note.length() > 1000) {
            throw new WellnessException(
                    "The note must not exceed 1000 characters."
            );
        }
    }

    // Locks and validates the habit within the current transaction.
    private void requireActiveHabit(
            Connection connection,
            long habitId,
            LocalDate date
    ) throws SQLException {

        HabitRow habit
                = habits.findByIdForUpdate(connection, habitId)
                        .orElseThrow(() -> new WellnessException(
                                "The selected habit does not exist."
                        ));

        if (!habit.active()) {
            throw new WellnessException(
                    "This habit is inactive. Reactivate it before adding "
                    + "or editing a check-in."
            );
        }

        if (date.isBefore(habit.activeFrom())
                || date.isAfter(habit.activeTo())) {
            throw new WellnessException(
                    "The check-in date must be within the habit's active period."
            );
        }
    }
}
