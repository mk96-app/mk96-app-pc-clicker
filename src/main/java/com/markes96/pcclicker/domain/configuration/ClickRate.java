package com.markes96.pcclicker.domain.configuration;

import java.time.Duration;

/** How many clicks are performed every second. */
public record ClickRate(int clicksPerSecond) {

    public static final IntRange RANGE = new IntRange(1, 1000);
    public static final ClickRate DEFAULT = new ClickRate(1);

    private static final long NANOS_PER_SECOND = Duration.ofSeconds(1).toNanos();

    public ClickRate {
        RANGE.require(clicksPerSecond, "Clicks per second");
    }

    /** Time between two consecutive clicks. */
    public Duration interval() {
        return Duration.ofNanos(NANOS_PER_SECOND / clicksPerSecond);
    }
}
