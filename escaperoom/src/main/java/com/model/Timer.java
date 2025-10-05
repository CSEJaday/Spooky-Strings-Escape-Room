package com.model;

public class Timer {

    private final int INTERVAL;
    private int duration;
    private int timeRemaining;
    private boolean isRunning;

    public Timer(int duration) {
        this.INTERVAL = 0;
    }

    public int getTimeRemaining() {
        return 0;
    }

    public void start() {
    }

    public void pause() {
    }

    public void resume() {
    }

    public void addTime(int time) {
    }

    public void removeTime(int time) {
    }

    public void addObserver(Observer obs) {
    }
}

}
