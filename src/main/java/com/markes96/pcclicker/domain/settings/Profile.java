package com.markes96.pcclicker.domain.settings;

import java.util.Objects;

/** Session settings saved under a name, to reuse them in one click. */
public record Profile(ProfileName name, SessionSettings settings) {

    public Profile {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(settings, "settings");
    }
}
