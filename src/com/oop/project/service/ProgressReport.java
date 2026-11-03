package com.oop.project.service;

import java.time.LocalDate;
import java.util.List;
import com.oop.project.repository.EvaluationSettingRepository.StrategyType;

public record ProgressReport(long planId, LocalDate from, LocalDate to,
                             StrategyType strategy, List<Entry> entries) {
    public ProgressReport { entries = List.copyOf(entries); }

    public record Entry(long habitId, String name, double value, String unit) {}
}
