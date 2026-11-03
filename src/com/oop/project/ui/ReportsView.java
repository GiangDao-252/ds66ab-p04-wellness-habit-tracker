package com.oop.project.ui;

import com.oop.project.repository.EvaluationSettingRepository.StrategyType;
import com.oop.project.service.ApplicationService;
import java.sql.SQLException;

final class ReportsView {
    private final ApplicationService app;
    private final ConsoleInput input;
    private final DashboardObserver dashboard;

    ReportsView(ApplicationService app, ConsoleInput input, DashboardObserver dashboard) {
        this.app = app;
        this.input = input;
        this.dashboard = dashboard;
    }

    void show() throws SQLException {
        long planId = input.id("Plan ID: ");
        switch (input.read("Use saved report window? (y/n): ").toLowerCase(java.util.Locale.ROOT)) {
            case "y" -> dashboard.show(planId, null, null);
            case "n" -> dashboard.show(planId, input.date("From: "), input.date("To: "));
            default -> throw new IllegalArgumentException("Choose y or n.");
        }
    }

    void switchStrategy() throws SQLException {
        long planId = input.id("Plan ID: ");
        StrategyType strategy = switch (input.read("1 Completion percentage / 2 Streak: ")) {
            case "1" -> StrategyType.COMPLETION_PERCENTAGE;
            case "2" -> StrategyType.STREAK;
            default -> throw new IllegalArgumentException("Choose strategy 1 or 2.");
        };
        long days = input.id("Report window in days: ");
        if (days > Integer.MAX_VALUE) throw new IllegalArgumentException("Report window is too large.");
        app.setStrategy(planId, strategy, (int) days);
    }
}
