package com.oop.project.model.strategy;

import com.oop.project.model.CheckIn;
import com.oop.project.model.Frequency;
import com.oop.project.model.Habit;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chiến lược tính tỷ lệ phần trăm hoàn thành (Completion Percentage Strategy).
 * Đáp ứng yêu cầu Task D06: Tính % hoàn thành theo period, xử lý target và
 * frequency rõ ràng.
 */
public class CompletionPercentageStrategy {

    /**
     * Tính tỷ lệ phần trăm hoàn thành thói quen trong một khoảng thời gian
     * (period).
     * 
     * @ đối tượng Habit cần tính
     * 
     * @param periodStart ngày bắt đầu khoảng thời gian đánh giá
     * @param periodEnd   ngày kết thúc khoảng thời gian đánh giá
     * @return tỷ lệ phần trăm hoàn thành (từ 0.0 đến 100.0% hoặc hơn nếu vượt mức)
     */
    public double calculateCompletionPercentage(Habit habit, LocalDate periodStart, LocalDate periodEnd) {
        if (habit == null || periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("Habit and evaluation periods cannot be null.");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start date cannot be after period end date.");
        }

        // 1. Lọc các check-in nằm trong khoảng thời gian (period) yêu cầu
        List<CheckIn> periodCheckIns = habit.getCheckIns().stream()
                .filter(c -> !c.getDate().isBefore(periodStart) && !c.getDate().isAfter(periodEnd))
                .collect(Collectors.toList());

        // Tổng giá trị thực tế đã đạt được trong period
        double totalAchieved = periodCheckIns.stream()
                .mapToDouble(CheckIn::getValue)
                .sum();

        // 2. Tính toán tổng mục tiêu kỳ vọng (Target) dựa trên frequency và period
        double expectedTarget = calculateExpectedTarget(habit, periodStart, periodEnd);

        if (expectedTarget <= 0) {
            return 0.0;
        }

        // 3. Tính toán phần trăm hoàn thành (Math logic)
        double percentage = (totalAchieved / expectedTarget) * 100.0;

        // Làm tròn đến 2 chữ số thập phân cho đẹp
        return Math.round(percentage * 100.0) / 100.0;
    }

    /**
     * Xử lý logic tính tổng target kỳ vọng dựa vào Frequency và khoảng thời gian.
     */
    private double calculateExpectedTarget(Habit habit, LocalDate start, LocalDate end) {
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        Frequency freq = habit.getFrequency();
        double baseTarget = habit.getTargetValue();

        if (freq == Frequency.DAILY) {
            // Mỗi ngày đều phải đạt targetValue
            return totalDays * baseTarget;
        } else if (freq == Frequency.WEEKLY) {
            // Tính số tuần trong khoảng thời gian (ít nhất là 1 tuần tính trên mỗi 7 ngày)
            long weeks = Math.max(1, totalDays / 7);
            return weeks * baseTarget;
        } else {
            // Mặc định tính theo số lượng ngày
            return totalDays * baseTarget;
        }
    }
}