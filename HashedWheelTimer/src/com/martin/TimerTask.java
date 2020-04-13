package com.martin;

public interface TimerTask {

    void run(HashedWheelTimeout timeout) throws Exception;
}
