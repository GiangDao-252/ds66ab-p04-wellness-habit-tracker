package com.oop.project.model;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WellnessPlan {
    private String planId;
    private String name;
    private final List<Habit> habits; // Composition

    public WellnessPlan(String planId, String name) {
        this.planId = planId;
        this.name = name;
        this.habits = new ArrayList<>();
    }

    public void addHabit(Habit habit) {
        if (habit == null) {
            throw new IllegalArgumentException("Habit cannot be null");
        }
        this.habits.add(habit);
    }

    public boolean removeHabit(String habitId) {
        return this.habits.removeIf(h -> h.getId().equals(habitId));
    }

    // Encapsulation
    public List<Habit> getHabits() {
        return Collections.unmodifiableList(habits);
    }

    public String getPlanId() { return planId; }
    public String getName() { return name; }
}