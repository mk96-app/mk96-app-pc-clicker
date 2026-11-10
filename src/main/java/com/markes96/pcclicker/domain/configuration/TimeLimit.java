package com.markes96.pcclicker.domain.configuration;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/** Maximum active time of a session; a session may also run without limit. */
public final class TimeLimit {

    public static final IntRange MINUTES_RANGE = new IntRange(1, 99_999_999);

    private static final TimeLimit UNLIMITED = new TimeLimit(null);

    private final Duration duration;

    private TimeLimit(Duration duration) {
        this.duration = duration;
    }

    public static TimeLimit unlimited() {
        return UNLIMITED;
    }

    public static TimeLimit ofMinutes(int minutes) {
        MINUTES_RANGE.require(minutes, "Time limit in minutes");
        return new TimeLimit(Duration.ofMinutes(minutes));
    }

    public boolean isLimited() {
        return duration != null;
    }

    public Optional<Duration> duration() {
        return Optional.ofNullable(duration);
    }

    public boolean isReachedBy(Duration elapsed) {
        return isLimited() && elapsed.compareTo(duration) >= 0;
    }

    /** Time left once {@code elapsed} has passed; never negative. Empty when unlimited. */
    public Optional<Duration> remainingAfter(Duration elapsed) {
        return duration().map(limit -> {
            final Duration remaining = limit.minus(elapsed);
            return remaining.isNegative() ? Duration.ZERO : remaining;
        });
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof TimeLimit that && Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(duration);
    }

    @Override
    public String toString() {
        return isLimited() ? "TimeLimit[" + duration + "]" : "TimeLimit[unlimited]";
    }
}
