package com.oop.project.exception;

/**
 * Lớp ngoại lệ gốc cho toàn bộ tầng Domain của dự án Wellness Habit Tracker.
 * Giúp phân biệt rõ ràng giữa ngoại lệ nghiệp vụ (domain exception) và ngoại lệ hệ thống.
 */
public class WellnessDomainException extends RuntimeException {
    public WellnessDomainException(String message) {
        super(message);
    }
}