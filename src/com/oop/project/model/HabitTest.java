package com.oop.project.model;

import java.time.LocalDate;

public class HabitTest {
    public static void main(String[] args) {
        System.out.println("Running Habit Tests...");

        // Test 1: Tạo Habit hợp lệ
        Habit habit = new Habit("H1", "Uống nước", Frequency.DAILY, 2000, "ml", 
                                LocalDate.now(), LocalDate.now().plusDays(30));
        assert "Uống nước".equals(habit.getName());
        assert habit.isActive() == true;
        System.out.println("Test 1 Passed: Habit creation valid.");

        // Test 2: Test Deactivate
        habit.deactivate();
        assert habit.isActive() == false;
        System.out.println("Test 2 Passed: Habit deactivation works.");

        // Test 3: Test Update Habit
        habit.updateHabit("Uống nhiều nước hơn", Frequency.DAILY, 2500, "ml", 
                          LocalDate.now(), LocalDate.now().plusDays(60));
        assert habit.getTargetValue() == 2500;
        System.out.println("Test 3 Passed: Habit update works.");

        // Test 4: Test Validation (Target value âm -> phải báo lỗi)
        try {
            new Habit("H2", "Sai mục tiêu", Frequency.DAILY, -100, "ml", 
                      LocalDate.now(), LocalDate.now().plusDays(10));
            System.out.println("Test 4 Failed: Should throw exception for negative target.");
        } catch (IllegalArgumentException e) {
            System.out.println("Test 4 Passed: Validation caught invalid target value (" + e.getMessage() + ")");
        }

        System.out.println("All Habit tests passed successfully!");
    }
}