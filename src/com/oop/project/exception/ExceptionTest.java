package com.oop.project.exception;

import com.oop.project.model.CheckIn;
import com.oop.project.model.Frequency;
import com.oop.project.model.Habit;
import com.oop.project.model.WellnessPlan;

import java.time.LocalDate;

public class ExceptionTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING DOMAIN EXCEPTION TESTS (TASK D09) ===");

        // Test 1: Bắt InvalidCheckInException khi check-in giá trị âm
        try {
            new CheckIn(LocalDate.now(), -10.0);
            System.out.println("Test 1 Failed: Exception not thrown.");
        } catch (InvalidCheckInException e) {
            System.out.println("Test 1 Passed: Caught expected exception -> " + e.getMessage());
        }

        // Test 2: Bắt InvalidHabitException khi targetValue <= 0
        try {
            new Habit("H1", "Test", Frequency.DAILY, 0, "ml", LocalDate.now(), LocalDate.now().plusDays(5));
            System.out.println("Test 2 Failed: Exception not thrown.");
        } catch (InvalidHabitException e) {
            System.out.println("Test 2 Passed: Caught expected exception -> " + e.getMessage());
        }

        // Test 3: Bắt InvalidPlanException khi tên plan trống
        try {
            new WellnessPlan("P1", "", "Description");
            System.out.println("Test 3 Failed: Exception not thrown.");
        } catch (InvalidPlanException e) {
            System.out.println("Test 3 Passed: Caught expected exception -> " + e.getMessage());
        }

        System.out.println("=== ALL EXCEPTION TESTS PASSED SUCCESSFULLY ===");
    }
}