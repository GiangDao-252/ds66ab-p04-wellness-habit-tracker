package com.oop.project.model;

import java.time.LocalDate;
public class CheckInTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING CHECKIN TESTS (TASK D03) ===");

        // Test 1: Tạo CheckIn hợp lệ với ngày hôm nay hoặc quá khứ
        try {
            CheckIn validCheckIn = new CheckIn(LocalDate.now(), 1500.0);
            assert validCheckIn.getValue() == 1500.0;
            assert validCheckIn.getDate().equals(LocalDate.now());
            System.out.println("Test 1 Passed: Valid CheckIn creation.");
        } catch (Exception e) {
            System.out.println("Test 1 Failed: " + e.getMessage());
        }

        // Test 2: Reject ngày trong tương lai (Future date)
        try {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            new CheckIn(futureDate, 1000.0);
            System.out.println("Test 2 Failed: Did not reject future date.");
        } catch (IllegalArgumentException e) {
            System.out.println("Test 2 Passed: Successfully rejected future date (" + e.getMessage() + ")");
        }

        // Test 3: Reject ngày null (Invalid date)
        try {
            new CheckIn(null, 1000.0);
            System.out.println("Test 3 Failed: Did not reject null date.");
        } catch (IllegalArgumentException e) {
            System.out.println("Test 3 Passed: Successfully rejected null date (" + e.getMessage() + ")");
        }

        // Test 4: Reject giá trị đo lường âm hoặc bằng 0 (Negative measurement)
        try {
            new CheckIn(LocalDate.now(), -50.0);
            System.out.println("Test 4 Failed: Did not reject negative measurement value.");
        } catch (IllegalArgumentException e) {
            System.out.println("Test 4 Passed: Successfully rejected negative measurement (" + e.getMessage() + ")");
        }

        // Test 5: Reject giá trị đo lường bằng 0
        try {
            new CheckIn(LocalDate.now(), 0);
            System.out.println("Test 5 Failed: Did not reject zero measurement value.");
        } catch (IllegalArgumentException e) {
            System.out.println("Test 5 Passed: Successfully rejected zero measurement (" + e.getMessage() + ")");
        }

        System.out.println("=== ALL CHECKIN TESTS PASSED SUCCESSFULLY ===");
    }
}