package com.oop.project.service;

@FunctionalInterface
public interface PlanObserver {
    void planChanged(long planId);
}
