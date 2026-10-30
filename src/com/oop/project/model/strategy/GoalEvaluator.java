package com.oop.project.model.strategy;

import com.oop.project.model.Habit;

/**
 * Lớp GoalEvaluator đóng vai trò là Context trong Strategy Pattern.
 * - Đáp ứng yêu cầu: Giữ strategy hiện tại, cho phép chuyển đổi chiến lược (switch strategy) 
 *   tại runtime mà hoàn toàn không cần sửa đổi lớp Habit.
 * - Ứng dụng kiến thức: Composition, Polymorphism, Strategy Pattern.
 */
public class GoalEvaluator {
    private GoalEvaluationStrategy strategy;

    /**
     * Khởi tạo bộ đánh giá với một chiến lược ban đầu cụ thể.
     */
    public GoalEvaluator(GoalEvaluationStrategy initialStrategy) {
        if (initialStrategy == null) {
            throw new IllegalArgumentException("Initial strategy cannot be null.");
        }
        this.strategy = initialStrategy;
    }

    /**
     * Cho phép chuyển đổi chiến lược (switch strategy) linh hoạt tại runtime.
     */
    public void setStrategy(GoalEvaluationStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null.");
        }
        this.strategy = strategy;
    }

    /**
     * Lấy chiến lược đang được áp dụng hiện tại.
     */
    public GoalEvaluationStrategy getStrategy() {
        return strategy;
    }

    /**
     * Thực hiện đánh giá thói quen thông qua chiến lược hiện tại (Tính đa hình - Polymorphism).
     */
    public boolean evaluateHabit(Habit habit) {
        return strategy.evaluate(habit);
    }

    /**
     * Lấy mô tả chi tiết của chiến lược đang hoạt động.
     */
    public String getCurrentStrategyDescription() {
        return strategy.getStrategyDescription();
    }
}