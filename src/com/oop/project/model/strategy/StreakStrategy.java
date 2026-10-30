package com.oop.project.model.strategy;

import com.oop.project.model.CheckIn;
import com.oop.project.model.Habit;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Chiến lược tính chuỗi ngày thực hiện liên tiếp (Streak Strategy).
 * Đáp ứng Task D07: Tính consecutive-day streak, xử lý duplicate check-in, ngày thiếu và period boundary.
 */
public class StreakStrategy {

    /**
     * Tính số ngày liên tiếp (streak) thực hiện thói quen tính đến một ngày mốc (referenceDate).
     * 
     * @param habit         đối tượng thói quen cần tính streak
     * @param referenceDate ngày mốc để tính streak (thường là LocalDate.now())
     * @return số ngày liên tiếp đạt streak
     */
    public int calculateCurrentStreak(Habit habit, LocalDate referenceDate) {
        if (habit == null || habit.getCheckIns().isEmpty() || referenceDate == null) {
            return 0;
        }

        // 1. Xử lý Duplicate Check-in & Period Boundary:
        // Sử dụng TreeSet để loại bỏ các bản ghi trùng ngày (duplicate) và tự động sắp xếp theo thứ tự giảm dần (mới nhất lên đầu).
        Set<LocalDate> uniqueCheckInDates = habit.getCheckIns().stream()
                .map(CheckIn::getDate)
                // Lọc bỏ những ngày nằm sau ngày mốc (period boundary)
                .filter(date -> !date.isAfter(referenceDate))
                .collect(Collectors.toCollection(() -> new TreeSet<LocalDate>(Comparator.reverseOrder())));

        if (uniqueCheckInDates.isEmpty()) {
            return 0;
        }

        // Kiểm tra xem ngày mốc hoặc ngày gần nhất có nằm trong chuỗi streak không.
        // Nếu ngày gần nhất cách quá xa ngày mốc (trừ đi > 1 ngày mà không có check-in), streak = 0.
        LocalDate latestDate = uniqueCheckInDates.iterator().next();
        
        // Nếu ngày check-in gần nhất cách ngày referenceDate quá 1 ngày (có ngày thiếu ở giữa), 
        // nghĩa là chuỗi streak hiện tại đã bị đứt gãy.
        if (referenceDate.toEpochDay() - latestDate.toEpochDay() > 1) {
            return 0;
        }

        // 2. Thuật toán tính Consecutive-day Streak (Xử lý ngày thiếu):
        int streak = 0;
        LocalDate expectedDate = latestDate;

        for (LocalDate date : uniqueCheckInDates) {
            if (date.equals(expectedDate)) {
                streak++;
                // Lùi lại 1 ngày để kiểm tra tính liên tiếp cho vòng lặp tiếp theo
                expectedDate = expectedDate.minusDays(1);
            } else {
                // Nếu gặp ngày bị thiếu (gap), ngắt chuỗi liên tiếp ngay lập tức
                break;
            }
        }

        return streak;
    }
}