package com.oop.project.model;

import com.oop.project.exception.InvalidPlanException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * WellnessPlan đóng vai trò là Aggregate Root.
 * - Đáp ứng Composition & Collection: Quản lý danh sách Habit bên trong kế hoạch.
 * - Đáp ứng SRP: Chỉ tập trung quản lý nghiệp vụ cấp độ kế hoạch (Plan), không chứa JDBC.
 */
public class WellnessPlan {
    private String id;
    private String name;
    private String description;
    private final List<Habit> habits;
    
    // Hỗ trợ phát Domain Event đơn giản (Observer pattern nhẹ ở mức Domain)
    private static final List<Consumer<String>> eventListeners = new ArrayList<>();

    public WellnessPlan(String id, String name, String description) {
        validate(name);
        this.id = id;
        this.name = name;
        this.description = description;
        this.habits = new ArrayList<>();
    }

    private void validate(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidPlanException("Plan name cannot be null or empty.");
        }
    }

    // --- Quản lý danh sách Habit (Aggregate management) ---
    public void addHabit(Habit habit) {
        if (habit == null) {
            throw new InvalidPlanException("Habit cannot be null.");
        }
        if (habits.stream().anyMatch(h -> h.getId().equals(habit.getId()))) {
            throw new InvalidPlanException("Habit with this ID ' " + habit.getId() + " ' already exists in the plan.");
        }
        this.habits.add(habit);
        
        // Phát Domain Event khi thêm thói quen thành công
        publishDomainEvent("HabitAdded: " + habit.getName() + " added to plan " + this.name);
    }

    public boolean removeHabit(String habitId) {
        boolean removed = this.habits.removeIf(h -> h.getId().equals(habitId));
        if (removed) {
            publishDomainEvent("HabitRemoved: Habit ID " + habitId + " removed from plan " + this.name);
        }
        return removed;
    }

    // --- Quản lý Check-in liên quan thông qua Aggregate Root ---
    public void addCheckInToHabit(String habitId, LocalDate date, double value) {
        Habit habit = findHabitById(habitId);
        if (habit == null) {
            throw new IllegalArgumentException("Habit not found with ID: " + habitId);
        }
        habit.addCheckIn(date, value);
        publishDomainEvent("CheckInRecorded: Added check-in for habit " + habit.getName());
    }

    public Habit findHabitById(String habitId) {
        return habits.stream()
                .filter(h -> h.getId().equals(habitId))
                .findFirst()
                .orElse(null);
    }

    // Encapsulation: Trả về danh sách không thể sửa đổi trực tiếp từ bên ngoài
    public List<Habit> getHabits() {
        return Collections.unmodifiableList(habits);
    }

    // --- Cơ chế phát Domain Event ---
    public static void registerEventListener(Consumer<String> listener) {
        eventListeners.add(listener);
    }

    private void publishDomainEvent(String eventMessage) {
        for (Consumer<String> listener : eventListeners) {
            listener.accept(eventMessage);
        }
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    public void updatePlanDetails(String name, String description) {
        validate(name);
        this.name = name;
        this.description = description;
    }
}