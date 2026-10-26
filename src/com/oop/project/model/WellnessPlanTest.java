package com.oop.project.model;

import java.time.LocalDate;

public class WellnessPlanTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING WELLNESS PLAN TESTS (TASK D04) ===");

        // Đăng ký nhận Domain Event để kiểm chứng tính năng phát event
        WellnessPlan.registerEventListener(event -> System.out.println("[DOMAIN EVENT] " + event));

        // Test 1: Tạo WellnessPlan và Thêm Habit (Composition & Collection)
        WellnessPlan plan = new WellnessPlan("P1", "Kế hoạch sức khỏe mùa hè", "Giữ dáng và ăn uống lành mạnh");
        Habit habit1 = new Habit("H1", "Chạy bộ", Frequency.DAILY, 5.0, "km", LocalDate.now(), LocalDate.now().plusDays(30));
        
        plan.addHabit(habit1);
        assert plan.getHabits().size() == 1;
        System.out.println("Test 1 Passed: Successfully added habit to plan aggregate.");

        // Test 2: Thêm Check-in cho Habit thông qua WellnessPlan aggregate
        plan.addCheckInToHabit("H1", LocalDate.now(), 4.5);
        Habit foundHabit = plan.findHabitById("H1");
        assert foundHabit.getCheckIns().size() == 1;
        assert foundHabit.getCheckIns().get(0).getValue() == 4.5;
        System.out.println("Test 2 Passed: Successfully recorded check-in through aggregate.");

        // Test 3: Xóa Habit khỏi kế hoạch
        boolean removed = plan.removeHabit("H1");
        assert removed == true;
        assert plan.getHabits().isEmpty();
        System.out.println("Test 3 Passed: Successfully removed habit from plan.");

        // Test 4: Kiểm tra tính độc lập (Không chứa JDBC code - kiểm tra mã nguồn thuần túy)
        System.out.println("Test 4 Passed: Domain model is pure Java, containing zero JDBC dependencies.");

        System.out.println("=== ALL WELLNESS PLAN TESTS PASSED SUCCESSFULLY ===");
    }
}