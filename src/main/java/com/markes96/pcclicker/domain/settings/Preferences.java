package com.markes96.pcclicker.domain.settings;

import com.markes96.pcclicker.domain.control.KeyBindings;
import java.util.Objects;

/**
 * How the application behaves, independently of what a session does.
 *
 * @param keys      key of every global hotkey
 * @param countdown time given to place the pointer before a session acts
 * @param showPanel whether the floating panel is shown during a session
 */
public record Preferences(KeyBindings keys, CountdownSeconds countdown, boolean showPanel) {

    private static final Preferences DEFAULTS = new Preferences(KeyBindings.defaults(), CountdownSeconds.DEFAULT, true);

    public Preferences {
        Objects.requireNonNull(keys, "keys");
        Objects.requireNonNull(countdown, "countdown");
    }

    public static Preferences defaults() {
        return DEFAULTS;
    }

    public Preferences withKeys(KeyBindings newKeys) {
        return new Preferences(newKeys, countdown, showPanel);
    }

    public Preferences withCountdown(CountdownSeconds newCountdown) {
        return new Preferences(keys, newCountdown, showPanel);
    }

    public Preferences withShowPanel(boolean newShowPanel) {
        return new Preferences(keys, countdown, newShowPanel);
    }
}
