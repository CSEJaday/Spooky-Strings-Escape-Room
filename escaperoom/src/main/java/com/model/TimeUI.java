package com.model;

public class TimeUI implements TimeObserver {
    public TimeUI() {

    }

    @Override
    public void onTick(int timeRemaining) {
        System.out.println("Time remaining: " + timeRemaining);
    }

    @Override
    public void onTimeUp() {
        System.out.println("Time's up!");
    }
    
}
