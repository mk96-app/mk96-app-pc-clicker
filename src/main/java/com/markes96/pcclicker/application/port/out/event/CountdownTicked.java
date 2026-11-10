package com.markes96.pcclicker.application.port.out.event;

/** The session will start in {@code secondsLeft} seconds; the user should place the pointer. */
public record CountdownTicked(int secondsLeft) implements SessionEvent {

    public CountdownTicked {
        if (secondsLeft < 1) {
            throw new IllegalArgumentException("secondsLeft must be positive but was " + secondsLeft);
        }
    }
}
