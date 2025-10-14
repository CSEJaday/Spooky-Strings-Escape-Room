package com.model;

<<<<<<< HEAD
public class TimeUI implements TimeObserver {
    public TimeUI() {

    }

    @Override
    public void onTick(int timeRemaining) {
        System.out.println("Time remaining: " + timeRemaining);
    }

    @Override
    public void onTimeUp() {
        System.out.println("Time is up!");
    }
    
}
=======
public class TimeUI {
    
    public TimeUI() {
        System.out.println("This is the timerUI");
    }//end constructor

    public void onTick(int timeRemaining) {

    }//end timRemaining()

    public void onTimeUp() {

    }//end onTimeUp()
}//end TimeUI
>>>>>>> 7f605275ee5fc170cb1f8050f73c2eedf9251cc7
