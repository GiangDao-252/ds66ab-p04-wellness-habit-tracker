package com.oop.project.model.strategy;

import com.oop.project.model.Frequency;
import com.oop.project.model.Habit;
import java.time.LocalDate;

/**
 * Kịch bản chấp nhận (Acceptance Demo 2) cho Task D11.
 * - Mục đích: Minh chứng cơ chế Runtime Polymorphism và Strategy Pattern.
 * - Kịch bản: Đánh giá thói quen bằng Completion Strategy -> Chuyển đổi (Switch) sang Streak Strategy lúc runtime -> Giải thích report thay đổi.
 */
public class AcceptanceDemo2 {
    public static void main(String[] args) {
        System.out.println("=== ACCEPTANCE DEMO 2: RUNTIME STRATEGY SWITCHING (COMPLETION -> STREAK) ===");

        // 1. Chuẩn bị dữ liệu mẫu Habit với các check-in thực tế trong những ngày qua
        LocalDate today = LocalDate.now();
        Habit habit = new Habit("H1", "Uống nước đủ 2L", Frequency.DAILY, 2000.0, "ml",
                                today.minusDays(5), today);
        
        // Thêm dữ liệu check-in thực tế (Đạt mục tiêu liên tiếp 3 ngày gần đây)
        habit.addCheckIn(today.minusDays(2), 2000.0);
        habit.addCheckIn(today.minusDays(1), 2000.0);
        habit.addCheckIn(today, 2000.0);

        // 2. Giai đoạn A: Sử dụng GoalEvaluator (Context) với chiến lược ban đầu: TargetCompletionStrategy
        GoalEvaluationStrategy completionStrategy = new TargetCompletionStrategy();
        GoalEvaluator evaluator = new GoalEvaluator(completionStrategy);

        System.out.println("\n--- [BÁO CÁO 1] Đánh giá bằng Completion Strategy ---");
        System.out.println("Chiến lược đang hoạt động: " + evaluator.getCurrentStrategyDescription());
        boolean isCompletedToday = evaluator.evaluateHabit(habit);
        System.out.println("Report kết quả: Hôm nay thói quen đã đạt mục tiêu định mức chưa? -> " + (isCompletedToday ? "ĐẠT" : "CHƯA ĐẠT"));

        // 3. Giai đoạn B: Thực hiện Switch Strategy tại Runtime sang Streak Strategy (Runtime Polymorphism)
        // Thay vì chỉ xét ngày hôm nay, ta chuyển sang đánh giá sức bền chuỗi ngày liên tiếp (Streak)
        StreakStrategy streakStrategy = new StreakStrategy();
        int currentStreak = streakStrategy.calculateCurrentStreak(habit, today);

        System.out.println("\n--- [BÁO CÁO 2] Chuyển đổi chiến lược sang Streak Strategy tại Runtime ---");
        System.out.println("Hành động: Switch sang thuật toán đếm chuỗi liên tiếp (StreakStrategy)...");
        System.out.println("Số ngày liên tiếp thực hiện (Streak) tính được: " + currentStreak + " ngày.");
        
        // Đặt điều kiện báo cáo mới dựa trên chuỗi (Ví dụ: Đạt streak >= 3 ngày được coi là xuất sắc)
        boolean isStreakGoalMet = currentStreak >= 3;
        System.out.println("Report kết quả mới: Thói quen có duy trì chuỗi liên tiếp >= 3 ngày không? -> " + (isStreakGoalMet ? "XUẤT SẮC (ĐẠT CHUỖI)" : "CẦN CỐ GẮNG"));

        System.out.println("\n=== ACCEPTANCE DEMO 2 HOÀN TẤT THÀNH CÔNG ===");
    }
}