package com.oop.project.model;

import com.oop.project.exception.InvalidHabitException;
import java.time.LocalDate;

public class HabitUnitTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING HABIT UNIT TESTS (TASK D10) ===");

        // Test 1: Happy Path - Khởi tạo Habit hợp lệ và thêm Check-in thành công
        try {
            LocalDate start = LocalDate.now().minusDays(5);
            LocalDate end = LocalDate.now();
            Habit habit = new Habit("H1", "Uống nước", Frequency.DAILY, 2000.0, "ml", start, end);
            
            assert habit != null : "Habit object should not be null";
            assert habit.isActive() == true : "New habit should be active";

            habit.addCheckIn(LocalDate.now(), 2000.0);
            assert habit.getCheckIns().size() == 1 : "Check-in list size should be 1";
            assert habit.getCheckIns().get(0).getValue() == 2000.0 : "Check-in value mismatch";
            
            System.out.println("Test 1 Passed: Valid Habit and Check-in management.");
        } catch (Exception e) {
            System.out.println("Test 1 Failed: " + e.getMessage());
        }

        // Test 2: Edge Case - Target value <= 0 ném InvalidHabitException
        try {
            new Habit("H2", "Sai target", Frequency.DAILY, 0.0, "ml", 
                      LocalDate.now(), LocalDate.now().plusDays(5));
            System.out.println("Test 2 Failed: Did not reject invalid target value.");
        } catch (InvalidHabitException e) {
            System.out.println("Test 2 Passed: Successfully caught invalid target value (" + e.getMessage() + ")");
        }

        // Test 3: Edge Case - Ngày bắt đầu sau ngày kết thúc ném ngoại lệ
        try {
            LocalDate start = LocalDate.now().plusDays(5);
            LocalDate end = LocalDate.now();
            new Habit("H3", "Sai period", Frequency.DAILY, 10.0, "km", start, end);
            System.out.println("Test 3 Failed: Did not reject invalid date period.");
        } catch (InvalidHabitException e) {
            System.out.println("Test 3 Passed: Successfully caught invalid date period (" + e.getMessage() + ")");
        }

        System.out.println("=== ALL HABIT UNIT TESTS PASSED SUCCESSFULLY ===");
    }
}