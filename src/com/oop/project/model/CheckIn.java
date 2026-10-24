package com.oop.project.model;
import java.time.LocalDate;
public class CheckIn {
    private LocalDate date;
    private double value;

    public CheckIn(LocalDate date, double value) {
        setCheckIn(date, value);
    }

    private void setCheckIn(LocalDate date, double value) {
        // Invariant: date != null & value > 0
        if (date == null) {
            throw new IllegalArgumentException("Invalid date: Date cannot be null");
        }
        if (value <= 0) {
            throw new IllegalArgumentException("Invalid measurement: Value must be positive");
        }
        this.date = date;
        this.value = value;
    }

    // Getters and Setters
    public LocalDate getDate() {
        return date;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Invalid measurement: Value must be positive");
        }
        this.value = value;
    }
}