package com.markes96.pcclicker.domain.settings;

import java.util.Locale;
import java.util.Objects;

/**
 * Name of a saved profile, as the user typed it but trimmed. Two names that only differ in case
 * are the same profile, so "Farm" and "farm" cannot coexist.
 */
public record ProfileName(String value) {

    public static final int MAX_LENGTH = 40;

    public ProfileName {
        Objects.requireNonNull(value, "value");
        value = value.strip();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("A profile name cannot be blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "A profile name has at most %d characters but was %d".formatted(MAX_LENGTH, value.length()));
        }
        if (value.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("A profile name cannot contain control characters");
        }
    }

    /** Whether {@code text} would make a valid name. */
    public static boolean isValid(String text) {
        if (text == null) {
            return false;
        }
        try {
            new ProfileName(text);
            return true;
        } catch (IllegalArgumentException invalid) {
            return false;
        }
    }

    /** Case-insensitive identity of the profile. */
    public String key() {
        return value.toLowerCase(Locale.ROOT);
    }

    public boolean sameAs(ProfileName other) {
        return key().equals(other.key());
    }

    @Override
    public String toString() {
        return value;
    }
}
