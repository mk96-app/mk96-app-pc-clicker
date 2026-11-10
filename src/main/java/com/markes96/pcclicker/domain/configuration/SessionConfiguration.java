package com.markes96.pcclicker.domain.configuration;

import java.util.Objects;

/** Everything a session needs to know before it starts. At least one action must be enabled. */
public record SessionConfiguration(ClickSettings click, MovementSettings movement, TimeLimit timeLimit) {

    public SessionConfiguration {
        Objects.requireNonNull(click, "click");
        Objects.requireNonNull(movement, "movement");
        Objects.requireNonNull(timeLimit, "timeLimit");
        if (!click.enabled() && !movement.enabled()) {
            throw new InvalidSessionConfigurationException(
                    "A session needs at least one action enabled: clicking or moving the mouse");
        }
    }

    public boolean clicks() {
        return click.enabled();
    }

    public boolean moves() {
        return movement.enabled();
    }
}
