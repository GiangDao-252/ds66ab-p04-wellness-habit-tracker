package com.oop.project.exception;

public class WellnessException extends RuntimeException {
    public WellnessException(String message) { super(message); }
    public WellnessException(String message, Throwable cause) { super(message, cause); }
}
