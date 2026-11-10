package com.markes96.pcclicker.domain.session;

import java.time.Duration;
import java.time.Instant;

/**
 * Measures active time, leaving pauses out. Time is always passed in, so the stopwatch is pure and
 * deterministic; it never reads the system clock.
 */
public final class Stopwatch {

    private enum State { IDLE, RUNNING, PAUSED, STOPPED }

    private State state = State.IDLE;
    private Duration accumulated = Duration.ZERO;
    private Instant runningSince;

    public void start(Instant now) {
        requireState(State.IDLE, "start");
        runningSince = now;
        state = State.RUNNING;
    }

    public void pause(Instant now) {
        requireState(State.RUNNING, "pause");
        accumulated = accumulated.plus(positiveBetween(runningSince, now));
        runningSince = null;
        state = State.PAUSED;
    }

    public void resume(Instant now) {
        requireState(State.PAUSED, "resume");
        runningSince = now;
        state = State.RUNNING;
    }

    /** Freezes the reading forever. Allowed from any state except an already stopped one. */
    public void stop(Instant now) {
        if (state == State.STOPPED) {
            throw new IllegalStateException("The stopwatch is already stopped");
        }
        if (state == State.RUNNING) {
            accumulated = accumulated.plus(positiveBetween(runningSince, now));
        }
        runningSince = null;
        state = State.STOPPED;
    }

    public Duration elapsed(Instant now) {
        return state == State.RUNNING ? accumulated.plus(positiveBetween(runningSince, now)) : accumulated;
    }

    private void requireState(State expected, String operation) {
        if (state != expected) {
            throw new IllegalStateException("Cannot " + operation + " a stopwatch that is " + state);
        }
    }

    private static Duration positiveBetween(Instant from, Instant to) {
        final Duration duration = Duration.between(from, to);
        return duration.isNegative() ? Duration.ZERO : duration;
    }
}
