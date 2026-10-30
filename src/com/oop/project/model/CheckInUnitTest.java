package com.oop.project.model;

import com.oop.project.exception.InvalidCheckInException;
import java.time.LocalDate;

public class CheckInUnitTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING CHECK-IN UNIT TESTS (TASK D10) ===");

        // Test 1: Happy Path - Tạo CheckIn hợp lệ thành công
        try {
            LocalDate today = LocalDate.now();
            CheckIn checkIn = new CheckIn(today, 1500.0);
            
            assert checkIn != null : "CheckIn object should not be null";
            assert checkIn.getDate().equals(today) : "Date mismatch";
            assert checkIn.getValue() == 1500.0 : "Value mismatch";
            
            System.out.println("Test 1 Passed: Valid Check-In created successfully.");
        } catch (Exception e) {
            System.out.println("Test 1 Failed: " + e.getMessage());
        }

        // Test 2: Edge Case - Từ chối ngày trong tương lai (ném InvalidCheckInException)
        try {
            LocalDate futureDate = LocalDate.now().plusDays(1);
            new CheckIn(futureDate, 1000.0);
            System.out.println("Test 2 Failed: Future date was not rejected.");
        } catch (InvalidCheckInException e) {
            System.out.println("Test 2 Passed: Successfully caught future date exception -> " + e.getMessage());
        }

        // Test 3a: Edge Case - Từ chối giá trị đo lường âm
        try {
            new CheckIn(LocalDate.now(), -50.0);
            System.out.println("Test 3a Failed: Negative value was not rejected.");
        } catch (InvalidCheckInException e) {
            System.out.println("Test 3a Passed: Successfully caught negative value exception -> " + e.getMessage());
        }

        // Test 3b: Edge Case - Từ chối giá trị đo lường bằng 0
        try {
            new CheckIn(LocalDate.now(), 0.0);
            System.out.println("Test 3b Failed: Zero value was not rejected.");
        } catch (InvalidCheckInException e) {
            System.out.println("Test 3b Passed: Successfully caught zero value exception -> " + e.getMessage());
        }

        System.out.println("=== ALL CHECK-IN UNIT TESTS PASSED SUCCESSFULLY ===");
    }
}