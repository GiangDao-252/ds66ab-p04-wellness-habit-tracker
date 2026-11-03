package com.oop.project.ui;

import com.oop.project.db.Transactions;
import com.oop.project.repository.EvaluationSettingRepository.StrategyType;
import com.oop.project.repository.HabitRepository;
import com.oop.project.repository.WellnessPlanRepository;
import com.oop.project.service.ApplicationService;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class DashboardIntegrationTest {
    public static void main(String[] args) throws Exception {
        var app = new ApplicationService();
        var buffer = new ByteArrayOutputStream();
        var out = new PrintStream(buffer);
        var dashboard = new DashboardObserver(app, new DashboardView(out), out);
        app.observe(dashboard);
        LocalDate today = LocalDate.now();
        long plan = app.createPlan("Dashboard integration fixture");
        long habit = 0;
        long checkIn = 0;
        try {
            habit = app.createHabit(new HabitRepository.HabitRow(0, plan, "Walk",
                    HabitRepository.Frequency.DAILY, BigDecimal.TEN, "steps", today, today, true));
            dashboard.show(plan, today, today);
            buffer.reset();
            checkIn = app.addCheckIn(habit, today, BigDecimal.TEN, "");
            require(buffer, "100.00 %");
            buffer.reset();
            app.setStrategy(plan, StrategyType.STREAK, 7);
            require(buffer, "1.00 days");
            if (new ApplicationService().report(plan, today, today).strategy() != StrategyType.STREAK) {
                throw new AssertionError("Strategy did not survive service restart.");
            }
            buffer.reset();
            app.deleteCheckIn(checkIn, habit);
            checkIn = 0;
            require(buffer, "0.00 days");
        } finally {
            if (checkIn != 0) app.deleteCheckIn(checkIn, habit);
            long habitId = habit;
            Transactions.execute(connection -> {
                if (habitId != 0) new HabitRepository().delete(connection, habitId);
                try (var statement = connection.prepareStatement("DELETE FROM evaluation_setting WHERE plan_id = ?")) {
                    statement.setLong(1, plan);
                    statement.executeUpdate();
                }
                new WellnessPlanRepository().delete(connection, plan);
                return null;
            });
        }
    }

    private static void require(ByteArrayOutputStream buffer, String expected) {
        String log = buffer.toString();
        if (!log.contains("Refreshing dashboard") || !log.contains(expected)) {
            throw new AssertionError(log);
        }
    }
}
