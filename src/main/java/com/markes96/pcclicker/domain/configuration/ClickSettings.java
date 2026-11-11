package com.markes96.pcclicker.domain.configuration;

import java.util.Objects;

/**
 * Whether the session clicks and how: rate, button, single or double click, random variation of
 * the pace and an optional number of clicks after which it stops.
 */
public record ClickSettings(boolean enabled, ClickRate rate, MouseButton button, ClickType type,
                            RateVariation variation, ClickLimit limit) {

    public ClickSettings {
        Objects.requireNonNull(rate, "rate");
        Objects.requireNonNull(button, "button");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(variation, "variation");
        Objects.requireNonNull(limit, "limit");
    }

    /** Left single clicks at a steady rate, without limit. */
    public static ClickSettings enabled(ClickRate rate) {
        return new ClickSettings(true, rate, MouseButton.LEFT, ClickType.SINGLE, RateVariation.NONE,
                ClickLimit.unlimited());
    }

    public static ClickSettings disabled() {
        return new ClickSettings(false, ClickRate.DEFAULT, MouseButton.LEFT, ClickType.SINGLE, RateVariation.NONE,
                ClickLimit.unlimited());
    }

    public ClickSettings withButton(MouseButton newButton) {
        return new ClickSettings(enabled, rate, newButton, type, variation, limit);
    }

    public ClickSettings withType(ClickType newType) {
        return new ClickSettings(enabled, rate, button, newType, variation, limit);
    }

    public ClickSettings withVariation(RateVariation newVariation) {
        return new ClickSettings(enabled, rate, button, type, newVariation, limit);
    }

    public ClickSettings withLimit(ClickLimit newLimit) {
        return new ClickSettings(enabled, rate, button, type, variation, newLimit);
    }
}
