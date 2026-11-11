package com.markes96.pcclicker.domain.settings;

import com.markes96.pcclicker.domain.configuration.IntRange;

/** Seconds given to place the pointer before a session acts. Zero starts it straight away. */
public record CountdownSeconds(int value) {

    public static final IntRange RANGE = new IntRange(0, 10);
    public static final CountdownSeconds DEFAULT = new CountdownSeconds(3);

    public CountdownSeconds {
        RANGE.require(value, "Countdown seconds");
    }

    public boolean isImmediate() {
        return value == 0;
    }
}
