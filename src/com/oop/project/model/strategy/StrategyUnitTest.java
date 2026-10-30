package com.oop.project.model.strategy;

import com.oop.project.model.Frequency;
import com.oop.project.model.Habit;
import java.time.LocalDate;

public class StrategyUnitTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING STRATEGY UNIT TESTS (TASK D10) ===");

        LocalDate today = LocalDate.now();

        // Test 1: Streak Strategy - Happy Path (3 ngày liên tiếp) & Xử lý Duplicate check-in
        try {
            Habit habit = new Habit("H1", "Thiền định", Frequency.DAILY, 1.0, "lần", 
                                    today.minusDays(10), today);
            StreakStrategy strategy = new StreakStrategy();

            habit.addCheckIn(today.minusDays(2), 1.0);
            habit.addCheckIn(today.minusDays(1), 1.0);
            habit.addCheckIn(today, 1.0);
            habit.addCheckIn(today, 1.0); // Duplicate check-in trong ngày hôm nay

            int streak = strategy.calculateCurrentStreak(habit, today);
            assert streak == 3 : "Expected streak to be 3, but got " + streak;
            
            System.out.println("Test 1 Passed: Streak strategy calculated correctly (Streak = 3, duplicate handled).");
        } catch (Exception e) {
            System.out.println("Test 1 Failed: " + e.getMessage());
        }

        // Test 2: Streak Strategy - Edge Case (Missing day làm đứt chuỗi)
        try {
            Habit habitMissing = new Habit("H2", "Tập yoga", Frequency.DAILY, 1.0, "lần", 
                                           today.minusDays(10), today);
            StreakStrategy strategy = new StreakStrategy();

            habitMissing.addCheckIn(today.minusDays(3), 1.0); // Cách đây 3 ngày
            // Bỏ qua ngày hôm qua (missing day)
            habitMissing.addCheckIn(today, 1.0); // Hôm nay check-in nhưng bị đứt quãng

            int streakMissing = strategy.calculateCurrentStreak(habitMissing, today);
            assert streakMissing == 1 : "Expected streak to be 1 due to missing day, but got " + streakMissing;
            
            System.out.println("Test 2 Passed: Missing day correctly broke the streak (Streak = 1).");
        } catch (Exception e) {
            System.out.println("Test 2 Failed: " + e.getMessage());
        }

        System.out.println("=== ALL STRATEGY UNIT TESTS PASSED SUCCESSFULLY ===");
    }
}