package com.oop.project.exception;

/**
 * Ngoại lệ được ném ra khi thông tin Habit không hợp lệ (target value <= 0, ngày sai lệch, v.v.).
 */
public class InvalidHabitException extends WellnessDomainException {
    public InvalidHabitException(String message) {
        super("Invalid Habit Error: " + message);
    }
}