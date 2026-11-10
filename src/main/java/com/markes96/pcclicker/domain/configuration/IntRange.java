package com.markes96.pcclicker.domain.configuration;

/**
 * Closed interval of integers. Single source of truth for the limits accepted by every numeric
 * setting, so the UI and the help screen can show them without duplicating the numbers.
 */
public record IntRange(int min, int max) {

    public IntRange {
        if (min > max) {
            throw new IllegalArgumentException("min (" + min + ") must not be greater than max (" + max + ")");
        }
    }

    public boolean contains(int value) {
        return value >= min && value <= max;
    }

    public int clamp(int value) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Returns the value when it is inside the range.
     *
     * @throws IllegalArgumentException naming the offending setting otherwise
     */
    public int require(int value, String name) {
        if (!contains(value)) {
            throw new IllegalArgumentException(
                    "%s must be between %d and %d but was %d".formatted(name, min, max, value));
        }
        return value;
    }
}
