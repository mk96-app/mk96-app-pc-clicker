package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import com.markes96.pcclicker.domain.settings.Preset;
import com.markes96.pcclicker.domain.settings.ProfileName;
import com.markes96.pcclicker.domain.settings.ProfileRef;
import java.util.Objects;

/** One choice of the profile picker: a preset included with the application or a saved profile. */
public sealed interface ProfileEntry {

    /** Text shown to the user. */
    String label();

    /** Whether the user may delete or overwrite it. */
    boolean editable();

    /** Whether the user marked it with a star. */
    boolean favorite();

    /** Identity of the profile, the same whether it is a favourite or not. */
    ProfileRef ref();

    /** A profile included with the application, named in the user's language. */
    record Included(Preset preset, String label, boolean favorite) implements ProfileEntry {

        public Included {
            Objects.requireNonNull(preset, "preset");
            Objects.requireNonNull(label, "label");
        }

        public Included(Preset preset, String label) {
            this(preset, label, false);
        }

        @Override
        public boolean editable() {
            return false;
        }

        @Override
        public ProfileRef ref() {
            return ProfileRef.of(preset);
        }
    }

    /** A profile the user saved. */
    record Saved(ProfileName name, boolean favorite) implements ProfileEntry {

        public Saved {
            Objects.requireNonNull(name, "name");
        }

        public Saved(ProfileName name) {
            this(name, false);
        }

        @Override
        public String label() {
            return name.value();
        }

        @Override
        public boolean editable() {
            return true;
        }

        @Override
        public ProfileRef ref() {
            return ProfileRef.of(name);
        }
    }
}
