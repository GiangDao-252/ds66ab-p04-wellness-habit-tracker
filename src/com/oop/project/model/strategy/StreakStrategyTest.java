package com.oop.project.model.strategy;

import com.oop.project.model.Frequency;
import com.oop.project.model.Habit;

import java.time.LocalDate;

public class StreakStrategyTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING STREAK STRATEGY TESTS (TASK D07) ===");

        LocalDate today = LocalDate.now();
        Habit habit = new Habit("H1", "Thiền định", Frequency.DAILY, 1.0, "lần", 
                                today.minusDays(10), today);

        StreakStrategy strategy = new StreakStrategy();

        // Test 1: Chuỗi liên tiếp 3 ngày gần nhất (Hôm nay, hôm qua, ngày kia)
        habit.addCheckIn(today.minusDays(2), 1.0);
        habit.addCheckIn(today.minusDays(1), 1.0);
        habit.addCheckIn(today, 1.0);

        int streak1 = strategy.calculateCurrentStreak(habit, today);
        System.out.println("Calculated Streak (Expected 3): " + streak1);
        assert streak1 == 3 : "Test 1 Failed: Expected streak to be 3";
        System.out.println("Test 1 Passed: Consecutive-day streak calculated correctly.");

        // Test 2: Xử lý Duplicate Check-in (Thêm nhiều check-in trong cùng ngày hôm nay)
        habit.addCheckIn(today, 1.0); // Duplicate check-in ngày hôm nay
        int streak2 = strategy.calculateCurrentStreak(habit, today);
        assert streak2 == 3 : "Test 2 Failed: Duplicate check-ins incorrectly increased streak.";
        System.out.println("Test 2 Passed: Duplicate check-ins handled properly (streak remains 3).");

        // Test 3: Xử lý ngày thiếu (Missing day làm đứt chuỗi)
        // Xóa sạch hoặc tạo habit mới bị khuyết mất ngày hôm qua
        Habit habitMissing = new Habit("H2", "Tập yoga", Frequency.DAILY, 1.0, "lần", 
                                       today.minusDays(10), today);
        habitMissing.addCheckIn(today.minusDays(3), 1.0); // Có check-in cách đây 3 ngày
        // Bỏ qua ngày hôm qua (missing day)
        habitMissing.addCheckIn(today, 1.0); // Check-in hôm nay nhưng bị đứt quãng ở giữa

        int streakMissing = strategy.calculateCurrentStreak(habitMissing, today);
        System.out.println("Calculated Streak with missing day (Expected 1): " + streakMissing);
        assert streakMissing == 1 : "Test 3 Failed: Missing day should break the consecutive streak.";
        System.out.println("Test 3 Passed: Missing days break streak correctly.");

        System.out.println("=== ALL STREAK STRATEGY TESTS PASSED SUCCESSFULLY ===");
    }
}