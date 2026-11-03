package com.oop.project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PlanSubject {
    private final List<PlanObserver> observers = new ArrayList<>();

    public void subscribe(PlanObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    void publish(long planId) {
        for (PlanObserver observer : List.copyOf(observers)) {
            observer.planChanged(planId);
        }
    }
}
