package com.oop.project.service;

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
}
