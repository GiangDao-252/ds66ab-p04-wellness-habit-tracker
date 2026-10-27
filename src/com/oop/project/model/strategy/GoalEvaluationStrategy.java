package com.oop.project.model.strategy;

import com.oop.project.model.Habit;

/**
 * Interface đại diện cho chiến lược đánh giá mục tiêu thói quen (Strategy Pattern).
 * Cho phép linh hoạt thay đổi các thuật toán đánh giá khác nhau mà không làm ảnh hưởng
 * đến lớp ngữ cảnh (Context) sử dụng nó.
 * 
 * Đáp ứng các nguyên lý: Abstraction, Polymorphism, Dependency Inversion.
 */
public interface GoalEvaluationStrategy {

    /**
     * Đánh giá mức độ hoàn thành mục tiêu của một thói quen dựa trên dữ liệu check-in.
     * 
     * @ đối tượng Habit cần đánh giá
     * @return true nếu đạt mục tiêu theo chiến lược quy định, ngược lại trả về false
     */
    boolean evaluate(Habit habit);

    /**
     * Lấy mô tả chi tiết về chiến lược đánh giá hiện tại.
     * 
     * @return chuỗi mô tả tên hoặc quy tắc của chiến lược
     */
    String getStrategyDescription();
}