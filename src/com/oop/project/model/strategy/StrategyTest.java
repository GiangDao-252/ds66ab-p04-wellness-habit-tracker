package com.oop.project.model.strategy;

import com.oop.project.model.Frequency;
import com.oop.project.model.Habit;

import java.time.LocalDate;

public class StrategyTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING STRATEGY TESTS (TASK D05) ===");

        // Chuẩn bị dữ liệu mẫu Habit (ví dụ: Uống tổng cộng 2000ml nước)
        Habit habit = new Habit("H1", "Uống nước", Frequency.DAILY, 2000.0, "ml", 
                                LocalDate.now().minusDays(5), LocalDate.now());
        
        habit.addCheckIn(LocalDate.now().minusDays(2), 1000.0);
        habit.addCheckIn(LocalDate.now().minusDays(1), 1200.0);

        // Test 1: Sử dụng TargetCompletionStrategy (Tổng giá trị = 2200 >= 2000 -> true)
        GoalEvaluationStrategy targetStrategy = new TargetCompletionStrategy();
        System.out.println(targetStrategy.getStrategyDescription());
        boolean result1 = targetStrategy.evaluate(habit);
        assert result1 == true;
        System.out.println("Test 1 Passed: TargetCompletionStrategy evaluated successfully (Result: " + result1 + ")");

        // Test 2: Hoán đổi sang FrequencyCountStrategy (Yêu cầu ít nhất 3 lần check-in, thực tế có 2 lần -> false)
        GoalEvaluationStrategy freqStrategy = new FrequencyCountStrategy(3);
        System.out.println(freqStrategy.getStrategyDescription());
        boolean result2 = freqStrategy.evaluate(habit);
        assert result2 == false;
        System.out.println("Test 2 Passed: FrequencyCountStrategy evaluated successfully (Result: " + result2 + ")");

        // Test 3: Kiểm tra tính Dependency Inversion & Polymorphism qua Interface chung
        GoalEvaluationStrategy polymorphicStrategy;
        polymorphicStrategy = new FrequencyCountStrategy(2); // Gán bằng chiến lược 2 nhưng chỉnh yêu cầu thành 2 lần
        assert polymorphicStrategy.evaluate(habit) == true;
        System.out.println("Test 3 Passed: Polymorphism works seamlessly.");

        System.out.println("=== ALL STRATEGY TESTS PASSED SUCCESSFULLY ===");
    }
}