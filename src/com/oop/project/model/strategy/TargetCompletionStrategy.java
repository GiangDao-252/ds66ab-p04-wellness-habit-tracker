package com.oop.project.model.strategy;

import com.oop.project.model.CheckIn;
import com.oop.project.model.Habit;

/**
 * Chiến lược 1: Đánh giá hoàn thành dựa trên tổng giá trị check-in so với targetValue.
 */
public class TargetCompletionStrategy implements GoalEvaluationStrategy {

    @Override
    public boolean evaluate(Habit habit) {
        if (habit == null || habit.getCheckIns().isEmpty()) {
            return false;
        }
        // Tính tổng giá trị các lần check-in
        double totalValue = habit.getCheckIns().stream()
                .mapToDouble(CheckIn::getValue)
                .sum();
        
        // Đạt nếu tổng giá trị đạt được >= targetValue của thói quen
        return totalValue >= habit.getTargetValue();
    }

    @Override
    public String getStrategyDescription() {
        return "Target Completion Strategy: Evaluates based on total achieved value vs target.";
    }
}