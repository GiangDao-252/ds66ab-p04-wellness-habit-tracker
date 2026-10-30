package com.oop.project.exception;

/**
 * Ngoại lệ được ném ra khi thông tin Check-in không hợp lệ (ngày tương lai, giá trị âm, v.v.).
 */
public class InvalidCheckInException extends WellnessDomainException {
    public InvalidCheckInException(String message) {
        super("Invalid Check-In Error: " + message);
    }
}