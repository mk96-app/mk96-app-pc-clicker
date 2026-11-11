package com.markes96.pcclicker.domain.configuration;

import java.util.OptionalLong;

/** Number of clicks after which the session stops by itself; a session may also click without limit. */
public final class ClickLimit {

    public static final IntRange RANGE = new IntRange(1, 99_999_999);

    private static final ClickLimit UNLIMITED = new ClickLimit(0);

    /** Zero means unlimited. */
    private final long clicks;

    private ClickLimit(long clicks) {
        this.clicks = clicks;
    }

    public static ClickLimit unlimited() {
        return UNLIMITED;
    }

    public static ClickLimit ofClicks(int clicks) {
        RANGE.require(clicks, "Click limit");
        return new ClickLimit(clicks);
    }

    public boolean isLimited() {
        return clicks > 0;
    }

    public OptionalLong clicks() {
        return isLimited() ? OptionalLong.of(clicks) : OptionalLong.empty();
    }

    public boolean isReachedBy(long performed) {
        return isLimited() && performed >= clicks;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ClickLimit that && clicks == that.clicks;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(clicks);
    }

    @Override
    public String toString() {
        return isLimited() ? "ClickLimit[" + clicks + "]" : "ClickLimit[unlimited]";
    }
}
