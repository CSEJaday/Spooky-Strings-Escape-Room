package com.model;

import java.util.Timer;
import java.util.TimerTask;

public class PenaltyTimer {
    private Timer timer;
    private boolean active;

    public void applyPenalty(int seconds) {
        if (active) return; // already penalized
        active = true;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                active = false;
                timer.cancel();
            }
        }, seconds * 1000);
        System.out.println("Penalty applied for " + seconds + " seconds.");
    }

    public boolean isActive() {
        return active;
    }
}









