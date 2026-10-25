package com.oop.project.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Habit {
    private String id;
    private String name;
    private Frequency frequency;
    private double targetValue;
    private String unit;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active; // Trạng thái hoạt động (cho tính năng deactivate)
    private final List<CheckIn> checkIns;

    public Habit(String id, String name, Frequency frequency, double targetValue, 
                 String unit, LocalDate startDate, LocalDate endDate) {
        validate(targetValue, startDate, endDate);
        this.id = id;
        this.name = name;
        this.frequency = frequency;
        this.targetValue = targetValue;
        this.unit = unit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = true; // Mặc định khi tạo mới là đang hoạt động
        this.checkIns = new ArrayList<>();
    }

    // --- Validation logic ---
    private void validate(double targetValue, LocalDate startDate, LocalDate endDate) {
        if (targetValue <= 0) {
            throw new IllegalArgumentException("Target value must be greater than zero");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    // --- Update functionality ---
    public void updateHabit(String name, Frequency frequency, double targetValue, 
                            String unit, LocalDate startDate, LocalDate endDate) {
        validate(targetValue, startDate, endDate);
        this.name = name;
        this.frequency = frequency;
        this.targetValue = targetValue;
        this.unit = unit;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // --- Deactivate functionality ---
    public void deactivate() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    // --- Quản lý CheckIns (đã làm ở D01) ---
    public void addCheckIn(LocalDate date, double value) {
        CheckIn checkIn = new CheckIn(date, value);
        this.checkIns.add(checkIn);
    }

    public boolean removeCheckIn(LocalDate date) {
        return this.checkIns.removeIf(c -> c.getDate().equals(date));
    }

    public List<CheckIn> getCheckIns() {
        return Collections.unmodifiableList(checkIns);
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public Frequency getFrequency() { return frequency; }
    public double getTargetValue() { return targetValue; }
    public String getUnit() { return unit; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
}