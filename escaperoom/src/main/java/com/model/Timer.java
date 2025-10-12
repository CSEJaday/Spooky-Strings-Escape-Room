package com.model;

import java.util.ArrayList;
import java.util.List;

public class Timer {

    private int duration; // in seconds
    private int timeRemaining;
    private boolean running;

    private final List<TimeObserver> observers = new ArrayList<>();

    public Timer(int durationSeconds) {
        this.duration = durationSeconds;
        this.timeRemaining = durationSeconds;
        this.running = false;
    }

    public void start() {
        if (running) return;
        running = true;

        new Thread(() -> {
            while (running && timeRemaining > 0) {
                notifyTick(timeRemaining);

                try {
                    Thread.sleep(1000); // wait 1 second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                timeRemaining--;
            }

            if (timeRemaining == 0) {
                notifyTick(0);
                notifyTimeUp();
                running = false;
            }
        }).start();
    }

    public void pause() {
        running = false;
    }

    public void reset() {
        pause();
        timeRemaining = duration;
    }

    public void addObserver(TimeObserver observer) {
        observers.add(observer);
    }

    private void notifyTick(int time) {
        for (TimeObserver observer : observers) {
            observer.onTick(time);
        }
    }

    private void notifyTimeUp() {
        for (TimeObserver observer : observers) {
            observer.onTimeUp();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }
}
