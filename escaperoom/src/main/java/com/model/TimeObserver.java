package com.model;

public interface TimeObserver {
    /**
     * called every time the timer ticks
     * @param timeRemaining The time remaining in seconds
     */
    void onTick(int timeRemaining);

    /**
     * Called once when the timer reaches zero
     */
    void onTimeUp();
    
}
