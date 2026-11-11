package com.markes96.pcclicker.domain.timing;

import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.RateVariation;
import java.time.Duration;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Time to wait before each click. Without variation it is the exact interval of the rate; with
 * variation every wait is drawn uniformly from {@code interval × (1 ± variation)}, so the clicks
 * stop being perfectly regular while the average rate stays the configured one.
 */
public final class ClickCadence {

    private final long intervalNanos;
    private final double variation;
    private final RandomGenerator random;

    public ClickCadence(ClickRate rate, RateVariation variation, RandomGenerator random) {
        this.intervalNanos = Objects.requireNonNull(rate, "rate").interval().toNanos();
        this.variation = Objects.requireNonNull(variation, "variation").fraction();
        this.random = Objects.requireNonNull(random, "random");
    }

    public boolean isSteady() {
        return variation == 0;
    }

    public Duration interval() {
        return Duration.ofNanos(intervalNanos);
    }

    /** Wait before the next click; always at least one nanosecond. */
    public Duration next() {
        if (isSteady()) {
            return interval();
        }
        final double factor = 1 + variation * (2 * random.nextDouble() - 1);
        return Duration.ofNanos(Math.max(1, Math.round(intervalNanos * factor)));
    }
}
