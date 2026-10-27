package com.oop.project.model.strategy;

import com.oop.project.model.Habit;

/**
 * Chiến lược 2: Đánh giá dựa trên số lượng buổi thực hiện (số lượng bản ghi check-in).
 */
public class FrequencyCountStrategy implements GoalEvaluationStrategy {
    private final int requiredCheckIns;

    public FrequencyCountStrategy(int requiredCheckIns) {
        this.requiredCheckIns = requiredCheckIns;
    }

    @Override
    public boolean evaluate(Habit habit) {
        if (habit == null) {
            return false;
        }
        // Đạt nếu số lượng lần check-in lớn hơn hoặc bằng số lượng yêu cầu tối thiểu
        return habit.getCheckIns().size() >= requiredCheckIns;
    }

    @Override
    public String getStrategyDescription() {
        return "Frequency Count Strategy: Evaluates based on minimum required check-in count (" + requiredCheckIns + ").";
    }
}