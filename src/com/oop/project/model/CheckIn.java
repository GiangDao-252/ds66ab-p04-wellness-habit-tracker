package com.oop.project.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * CheckIn được thiết kế như một Value Object (Immutable).
 * Đáp ứng yêu cầu Task D03: Date + measurement, validation quy tắc ngày/giá trị.
 */
public final class CheckIn {
    private final LocalDate date;
    private final double value;

    public CheckIn(LocalDate date, double value) {
        validate(date, value);
        this.date = date;
        this.value = value;
    }

    /**
     * Hàm kiểm tra dữ liệu đầu vào (Validation Rule):
     * - Đáp ứng: Reject invalid/null date & future date
     * - Đáp ứng: Reject negative/zero measurement
     */
    private void validate(LocalDate date, double value) {
        if (date == null) {
            throw new IllegalArgumentException("Check-in date cannot be null.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the future.");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Check-in measurement value must be greater than zero.");
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckIn checkIn = (CheckIn) o;
        return Double.compare(checkIn.value, value) == 0 && Objects.equals(date, checkIn.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, value);
    }

    @Override
    public String toString() {
        return "CheckIn{" +
                "date=" + date +
                ", value=" + value +
                '}';
    }
}