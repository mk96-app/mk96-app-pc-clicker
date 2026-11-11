package com.markes96.pcclicker.domain.settings;

import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import java.util.Objects;

/**
 * What the user has set up for a session, as it is edited, saved in a profile or remembered
 * between launches. Unlike {@link SessionConfiguration} it may have every action off: that is a
 * valid thing to store, just not something that can start.
 */
public record SessionSettings(ClickSettings click, MovementSettings movement, TimeLimit timeLimit) {

    private static final SessionSettings DEFAULTS =
            new SessionSettings(ClickSettings.disabled(), MovementSettings.disabled(), TimeLimit.unlimited());

    public SessionSettings {
        Objects.requireNonNull(click, "click");
        Objects.requireNonNull(movement, "movement");
        Objects.requireNonNull(timeLimit, "timeLimit");
    }

    /** Everything off, as on the very first launch. */
    public static SessionSettings defaults() {
        return DEFAULTS;
    }

    /** True when at least one action is on. */
    public boolean canStart() {
        return click.enabled() || movement.enabled();
    }

    /**
     * @throws com.markes96.pcclicker.domain.configuration.InvalidSessionConfigurationException when
     *         no action is on; check {@link #canStart()} first
     */
    public SessionConfiguration toConfiguration() {
        return new SessionConfiguration(click, movement, timeLimit);
    }
}
