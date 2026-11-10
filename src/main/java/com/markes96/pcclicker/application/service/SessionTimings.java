package com.markes96.pcclicker.application.service;

import java.time.Duration;
import java.util.Objects;

/**
 * Pace of a session.
 *
 * @param countdownSeconds seconds given to the user to place the pointer
 * @param countdownStep    time between two countdown announcements
 * @param movementInterval time between two pointer moves (20 ms = 50 moves per second)
 * @param progressInterval how often progress is published and the time limit checked
 */
public record SessionTimings(int countdownSeconds, Duration countdownStep, Duration movementInterval,
                             Duration progressInterval) {

    public SessionTimings {
        if (countdownSeconds < 1) {
            throw new IllegalArgumentException("countdownSeconds must be positive but was " + countdownSeconds);
        }
        requirePositive(countdownStep, "countdownStep");
        requirePositive(movementInterval, "movementInterval");
        requirePositive(progressInterval, "progressInterval");
    }

    public static SessionTimings defaults() {
        return new SessionTimings(3, Duration.ofSeconds(1), Duration.ofMillis(20), Duration.ofMillis(200));
    }

    private static void requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name);
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException(name + " must be positive but was " + duration);
        }
    }
}
