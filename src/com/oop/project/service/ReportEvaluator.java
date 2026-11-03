package com.oop.project.service;

import com.oop.project.model.Frequency;
import com.oop.project.model.Habit;
import com.oop.project.model.strategy.CompletionPercentageStrategy;
import com.oop.project.model.strategy.StreakStrategy;
import com.oop.project.repository.CheckInRepository.CheckInRow;
import com.oop.project.repository.EvaluationSettingRepository.StrategyType;
import com.oop.project.repository.HabitRepository.HabitRow;
import java.time.LocalDate;
import java.util.List;

final class ReportEvaluator {
    ProgressReport.Entry evaluate(StrategyType type, HabitRow row, List<CheckInRow> entries,
                                  LocalDate from, LocalDate to) {
        Habit habit = new Habit(Long.toString(row.id()), row.name(),
                Frequency.valueOf(row.frequency().name()), row.targetValue().doubleValue(),
                row.unit(), row.activeFrom(), row.activeTo());
        for (var entry : entries) {
            // Zero contributes no progress and the supplied domain rejects zero.
            if (entry.measurement().signum() > 0) {
                habit.addCheckIn(entry.checkDate(), entry.measurement().doubleValue());
            }
        }

        return switch (type) {
            case COMPLETION_PERCENTAGE -> new ProgressReport.Entry(row.id(), row.name(),
                    new CompletionPercentageStrategy().calculateCompletionPercentage(habit, from, to), "%");
            case STREAK -> new ProgressReport.Entry(row.id(), row.name(),
                    new StreakStrategy().calculateCurrentStreak(habit, to), "days");
        };
    }
}
