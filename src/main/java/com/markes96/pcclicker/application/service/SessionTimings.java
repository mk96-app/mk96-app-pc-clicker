package com.markes96.pcclicker.application.service;

import java.time.Duration;
import java.util.Objects;

/**
 * Pace of a session. How long the countdown lasts is a user preference, not a timing.
 *
 * @param countdownStep    time between two countdown announcements
 * @param movementInterval time between two pointer moves (20 ms = 50 moves per second)
 * @param progressInterval how often progress is published and the time limit checked
 */
public record SessionTimings(Duration countdownStep, Duration movementInterval, Duration progressInterval) {

    public SessionTimings {
        requirePositive(countdownStep, "countdownStep");
        requirePositive(movementInterval, "movementInterval");
        requirePositive(progressInterval, "progressInterval");
    }

    public static SessionTimings defaults() {
        return new SessionTimings(Duration.ofSeconds(1), Duration.ofMillis(20), Duration.ofMillis(200));
    }

    private static void requirePositive(Duration duration, String name) {
        Objects.requireNonNull(duration, name);
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException(name + " must be positive but was " + duration);
        }
    }
}
