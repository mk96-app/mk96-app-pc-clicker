package com.markes96.pcclicker.domain.configuration;

import java.util.Objects;

/** Whether the session clicks and how fast. */
public record ClickSettings(boolean enabled, ClickRate rate) {

    public ClickSettings {
        Objects.requireNonNull(rate, "rate");
    }

    public static ClickSettings enabled(ClickRate rate) {
        return new ClickSettings(true, rate);
    }

    public static ClickSettings disabled() {
        return new ClickSettings(false, ClickRate.DEFAULT);
    }
}
