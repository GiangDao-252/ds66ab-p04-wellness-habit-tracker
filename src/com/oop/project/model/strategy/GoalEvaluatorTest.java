package com.oop.project.model.strategy;

import com.oop.project.model.Frequency;
import com.oop.project.model.Habit;

import java.time.LocalDate;

public class GoalEvaluatorTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING GOAL EVALUATOR TESTS (TASK D08) ===");

        // Chuẩn bị dữ liệu mẫu Habit
        Habit habit = new Habit("H1", "Đọc sách", Frequency.DAILY, 2.0, "giờ", 
                                LocalDate.now().minusDays(2), LocalDate.now());
        habit.addCheckIn(LocalDate.now().minusDays(1), 1.5);
        habit.addCheckIn(LocalDate.now(), 1.0);

        // Test 1: Khởi tạo GoalEvaluator với TargetCompletionStrategy
        GoalEvaluationStrategy targetStrategy = new TargetCompletionStrategy();
        GoalEvaluator evaluator = new GoalEvaluator(targetStrategy);
        
        System.out.println("Initial Strategy: " + evaluator.getCurrentStrategyDescription());
        boolean result1 = evaluator.evaluateHabit(habit);
        System.out.println("Evaluation result with Target Strategy (Expected false): " + result1);
        assert result1 == false;
        System.out.println("Test 1 Passed: Initialized and evaluated with Target strategy successfully.");

        // Test 2: Switch strategy tại runtime sang FrequencyCountStrategy (yêu cầu ít nhất 2 lần check-in)
        GoalEvaluationStrategy freqStrategy = new FrequencyCountStrategy(2);
        evaluator.setStrategy(freqStrategy); // Chuyển đổi chiến lược lúc chạy không cần sửa Habit
        
        System.out.println("Switched Strategy: " + evaluator.getCurrentStrategyDescription());
        boolean result2 = evaluator.evaluateHabit(habit);
        System.out.println("Evaluation result with Frequency Strategy (Expected true): " + result2);
        assert result2 == true;
        System.out.println("Test 2 Passed: Switched strategy at runtime successfully without modifying Habit.");

        System.out.println("=== ALL GOAL EVALUATOR TESTS PASSED SUCCESSFULLY ===");
    }
}