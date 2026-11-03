package com.oop.project.ui;

import com.oop.project.service.ApplicationService;
import com.oop.project.service.PlanObserver;
import java.io.PrintStream;
import java.sql.SQLException;
import java.time.LocalDate;

final class DashboardObserver implements PlanObserver {
    private final ApplicationService app;
    private final DashboardView view;
    private final PrintStream out;
    private long selectedPlan;
    private LocalDate from;
    private LocalDate to;

    DashboardObserver(ApplicationService app, DashboardView view, PrintStream out) {
        this.app = app;
        this.view = view;
        this.out = out;
    }

    void show(long planId, LocalDate from, LocalDate to) throws SQLException {
        view.render(app.report(planId, from, to));
        selectedPlan = planId;
        this.from = from;
        this.to = to;
    }

    @Override
    public void planChanged(long planId) {
        if (planId != selectedPlan) return;
        try {
            out.println("Refreshing dashboard...");
            show(planId, from, to);
        } catch (SQLException | RuntimeException e) {
            out.println("Change saved. Dashboard refresh failed: " + e.getMessage());
        }
    }
}
