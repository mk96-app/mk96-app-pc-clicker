package com.markes96.pcclicker.adapter.in.ui.format;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** Text representations of durations. Units (h, min, s) are international, so no translation is needed. */
public final class DurationFormat {

    private DurationFormat() {
    }

    /** Stopwatch style, always with hours: {@code 00:02:14}. Fractions of a second are truncated. */
    public static String clock(Duration duration) {
        final Duration value = duration.isNegative() ? Duration.ZERO : duration;
        return "%02d:%02d:%02d".formatted(value.toHours(), value.toMinutesPart(), value.toSecondsPart());
    }

    /**
     * Compact natural style with the two most significant units: {@code 2 min 14 s}, {@code 1 h 2 min}.
     */
    public static String human(Duration duration) {
        final Duration value = duration.isNegative() ? Duration.ZERO : duration;
        final long hours = value.toHours();
        final int minutes = value.toMinutesPart();
        final int seconds = value.toSecondsPart();

        final List<String> parts = new ArrayList<>(2);
        if (hours > 0) {
            parts.add(hours + " h");
            if (minutes > 0) {
                parts.add(minutes + " min");
            }
        } else if (minutes > 0) {
            parts.add(minutes + " min");
            if (seconds > 0) {
                parts.add(seconds + " s");
            }
        } else {
            parts.add(seconds + " s");
        }
        return String.join(" ", parts);
    }
}
