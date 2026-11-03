package com.oop.project.service;

import com.oop.project.repository.EvaluationSettingRepository.SettingRow;
import com.oop.project.repository.EvaluationSettingRepository.StrategyType;
import java.util.ArrayList;

import com.oop.project.repository.CheckInRepository.CheckInRow;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.oop.project.db.ConnectionFactory;
import com.oop.project.db.Transactions;
import com.oop.project.repository.*;
import com.oop.project.repository.HabitRepository.HabitRow;
import com.oop.project.repository.HabitRepository.Frequency;
import java.sql.SQLException;
import java.util.List;

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

    public List<CheckInRow> checkIns(long habitId, LocalDate from, LocalDate to) throws SQLException {
        try (var connection = ConnectionFactory.open()) {
            return checkIns.findByHabitAndPeriod(connection, habitId, from, to);
        }
    }

    private final EvaluationSettingRepository settings = new EvaluationSettingRepository();
    private final ReportEvaluator evaluator = new ReportEvaluator();

    public void setStrategy(long planId, StrategyType type, int days) throws SQLException {
        if (days < 1) throw new IllegalArgumentException("Report window must be positive.");
        Transactions.execute(connection -> {
            settings.save(connection, new SettingRow(planId, type, days));
            return null;
        });
        changes.publish(planId);
    }

    public ProgressReport report(long planId, LocalDate from, LocalDate to) throws SQLException {
        LocalDate today = LocalDate.now();
        try (var connection = ConnectionFactory.open()) {
            var setting = settings.findByPlanId(connection, planId)
                    .orElse(new SettingRow(planId, StrategyType.COMPLETION_PERCENTAGE, 7));
            if (from == null && to == null) {
                to = today;
                from = today.minusDays(setting.configValue() - 1L);
            }
            if (from == null || to == null || from.isAfter(to) || to.isAfter(today)) {
                throw new IllegalArgumentException("Choose a valid report period ending today or earlier.");
            }

            var results = new ArrayList<ProgressReport.Entry>();
            for (var habit : habits.findByPlanId(connection, planId)) {
                LocalDate start = from.isBefore(habit.activeFrom()) ? habit.activeFrom() : from;
                LocalDate end = to.isAfter(habit.activeTo()) ? habit.activeTo() : to;
                if (!start.isAfter(end)) {
                    var rows = checkIns.findByHabitAndPeriod(connection, habit.id(), start, end);
                    results.add(evaluator.evaluate(setting.strategyType(), habit, rows, start, end));
                }
            }
            return new ProgressReport(planId, from, to, setting.strategyType(), results);
        }
    }
}
