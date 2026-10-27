package com.oop.project.model.strategy;

import com.oop.project.model.Frequency;
import com.oop.project.model.Habit;

import java.time.LocalDate;

public class CompletionPercentageTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING COMPLETION PERCENTAGE TESTS (TASK D06) ===");

        // Chuẩn bị dữ liệu: Thói quen uống nước hàng ngày, mục tiêu 2000ml/ngày
        LocalDate startDate = LocalDate.now().minusDays(3);
        LocalDate endDate = LocalDate.now();

        Habit habit = new Habit("H1", "Uống nước", Frequency.DAILY, 2000.0, "ml", startDate, endDate);

        // Check-in trong 4 ngày (tổng kỳ vọng = 4 ngày * 2000ml = 8000ml)
        // Thực tế check-in 2 ngày đầu, mỗi ngày 2000ml -> tổng thực tế = 4000ml (đạt
        // 50%)
        habit.addCheckIn(LocalDate.now().minusDays(3), 2000.0);
        habit.addCheckIn(LocalDate.now().minusDays(2), 2000.0);

        CompletionPercentageStrategy strategy = new CompletionPercentageStrategy();

        // Test 1: Tính % hoàn thành trong khoảng 4 ngày
        double percentage = strategy.calculateCompletionPercentage(habit, startDate, endDate);
        System.out.println("Calculated Completion Percentage: " + percentage + "%");
        assert percentage == 50.0 : "Expected 50.0%, but got " + percentage + "%";
        System.out.println("Test 1 Passed: Completion percentage calculated correctly for DAILY frequency.");

        // Test 2: Kiểm tra validation (Period không hợp lệ)
        try {
            strategy.calculateCompletionPercentage(habit, endDate, startDate); // Ngày bắt đầu sau ngày kết thúc
            System.out.println("Test 2 Failed: Did not reject invalid period.");
        } catch (IllegalArgumentException e) {
            System.out.println("Test 2 Passed: Successfully caught invalid period (" + e.getMessage() + ")");
        }

        System.out.println("=== ALL COMPLETION PERCENTAGE TESTS PASSED SUCCESSFULLY ===");
    }
}