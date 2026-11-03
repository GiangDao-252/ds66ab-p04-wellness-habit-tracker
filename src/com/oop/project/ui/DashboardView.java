package com.oop.project.ui;

import com.oop.project.service.ProgressReport;
import java.io.PrintStream;

final class DashboardView {
    private final PrintStream out;

    DashboardView(PrintStream out) { this.out = out; }

    void render(ProgressReport report) {
        out.printf("Dashboard | Plan %d | %s to %s | %s%n",
                report.planId(), report.from(), report.to(), report.strategy());
        if (report.entries().isEmpty()) out.println("No habits in this period.");
        for (var entry : report.entries()) {
            out.printf(java.util.Locale.ROOT, "%d | %s | %.2f %s%n",
                    entry.habitId(), entry.name(), entry.value(), entry.unit());
        }
    }
}
