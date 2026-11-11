package com.markes96.pcclicker.domain.configuration;

/**
 * How much the time between clicks may randomly deviate from the configured rate, as a percentage
 * of the interval. Makes the clicking less mechanical; the average rate is kept.
 */
public record RateVariation(int percent) {

    public static final IntRange RANGE = new IntRange(0, 50);
    public static final RateVariation NONE = new RateVariation(0);

    public RateVariation {
        RANGE.require(percent, "Rate variation percentage");
    }

    public boolean isNone() {
        return percent == 0;
    }

    /** Variation as a fraction of the interval, from 0 to 0.5. */
    public double fraction() {
        return percent / 100.0;
    }
}
