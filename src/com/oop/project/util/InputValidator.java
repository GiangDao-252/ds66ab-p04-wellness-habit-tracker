package com.oop.project.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class InputValidator {
    private InputValidator() {}

    public static long id(String text) {
        try {
            long id = Long.parseLong(text);
            if (id > 0) return id;
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalArgumentException("Enter a positive integer ID.");
    }

    public static LocalDate date(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Enter a valid date in YYYY-MM-DD format.");
        }
    }

    public static BigDecimal measurement(String text) {
        BigDecimal value;
        try {
            value = new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a decimal number, for example 1500.5.");
        }
        if (value.signum() < 0) throw new IllegalArgumentException("Measurement cannot be negative.");
        return value;
    }
}
