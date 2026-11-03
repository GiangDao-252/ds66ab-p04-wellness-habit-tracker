package com.oop.project.service;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.db.Transactions;
import com.oop.project.repository.*;
import com.oop.project.repository.HabitRepository.HabitRow;
import com.oop.project.repository.HabitRepository.Frequency;
import java.sql.SQLException;
import java.util.List;
import com.oop.project.repository.CheckInRepository.CheckInRow;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class ApplicationService {
    private final WellnessPlanRepository plans = new WellnessPlanRepository();
    private final WellnessPlanService planService = new WellnessPlanService();
    private final HabitRepository habits = new HabitRepository();
    private final PlanSubject changes = new PlanSubject();

    public void observe(PlanObserver observer) { changes.subscribe(observer); }

    public List<WellnessPlanRepository.PlanRow> plans() throws SQLException {
        try (var connection = ConnectionFactory.open()) {
            return plans.findAll(connection);
        }
    }

    public long createPlan(String name) throws SQLException {
        return planService.createPlan(name);
    }

    public List<HabitRow> habits(long planId) throws SQLException {
        try (var connection = ConnectionFactory.open()) {
            return habits.findByPlanId(connection, planId);
        }
    }

    public long createHabit(HabitRow habit) throws SQLException {
        long id = Transactions.execute(connection -> habits.insert(connection, habit));
        changes.publish(habit.planId());
        return id;
    }

    public void editHabit(long id, String name, Frequency frequency) throws SQLException {
        long planId = Transactions.execute(connection -> {
            var habit = habits.findByIdForUpdate(connection, id)
                    .orElseThrow(() -> new IllegalArgumentException("Habit not found."));
            habits.updateDetails(connection, id, name, frequency);
            return habit.planId();
        });
        changes.publish(planId);
    }

    public void deactivateHabit(long id) throws SQLException {
        long planId = Transactions.execute(connection -> {
            var habit = habits.findByIdForUpdate(connection, id)
                    .orElseThrow(() -> new IllegalArgumentException("Habit not found."));
            habits.setActive(connection, id, false);
            return habit.planId();
        });
        changes.publish(planId);
    }
        private final CheckInService checkInService = new CheckInService();
    private final CheckInRepository checkIns = new CheckInRepository();

    private long planOfHabit(long habitId) throws SQLException {
        try (var connection = ConnectionFactory.open()) {
            return habits.findById(connection, habitId)
                    .orElseThrow(() -> new IllegalArgumentException("Habit not found.")).planId();
        }
    }

    public long addCheckIn(long habitId, LocalDate date, BigDecimal value, String note)
            throws SQLException {
        long planId = planOfHabit(habitId);
        long id = checkInService.add(habitId, date, value, note);
        changes.publish(planId);
        return id;
    }

    public void editCheckIn(CheckInRow row) throws SQLException {
        long planId = planOfHabit(row.habitId());
        checkInService.update(row);
        changes.publish(planId);
    }

    public void deleteCheckIn(long id, long habitId) throws SQLException {
        long planId = planOfHabit(habitId);
        checkInService.delete(id, habitId);
        changes.publish(planId);
    }

    public List<CheckInRow> checkIns(long habitId, LocalDate from, LocalDate to)
            throws SQLException {
        try (var connection = ConnectionFactory.open()) {
            return checkIns.findByHabitAndPeriod(connection, habitId, from, to);
        }
    }
}
