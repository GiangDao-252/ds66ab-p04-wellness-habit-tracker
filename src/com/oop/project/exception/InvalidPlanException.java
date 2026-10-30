package com.oop.project.exception;

/**
 * Ngoại lệ được ném ra khi thông tin WellnessPlan hoặc thao tác quản lý plan gặp lỗi.
 */
public class InvalidPlanException extends WellnessDomainException {
    public InvalidPlanException(String message) {
        super("Invalid Wellness Plan Error: " + message);
    }
}