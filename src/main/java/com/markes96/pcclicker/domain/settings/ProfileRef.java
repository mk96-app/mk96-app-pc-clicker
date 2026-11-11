package com.markes96.pcclicker.domain.settings;

import java.util.Locale;
import java.util.Objects;

/** Identity of a profile, included or saved by the user, independent of how it is displayed. */
public sealed interface ProfileRef {

    static ProfileRef of(Preset preset) {
        return new OfPreset(preset);
    }

    /** Saved profiles are identified regardless of case, like their names. */
    static ProfileRef of(ProfileName name) {
        return new OfSaved(name.key());
    }

    /** A profile included with the application. */
    record OfPreset(Preset preset) implements ProfileRef {

        public OfPreset {
            Objects.requireNonNull(preset, "preset");
        }
    }

    /** A profile saved by the user, by the case-insensitive key of its name. */
    record OfSaved(String key) implements ProfileRef {

        public OfSaved {
            Objects.requireNonNull(key, "key");
            key = key.toLowerCase(Locale.ROOT);
        }
    }
}
