package com.markes96.pcclicker.domain.settings;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * The user's saved profiles, always sorted by name. Names are unique regardless of case; saving
 * under an existing name replaces that profile. Immutable: every change returns a new library.
 */
public final class ProfileLibrary {

    public static final int MAX_PROFILES = 100;

    private static final ProfileLibrary EMPTY = new ProfileLibrary(List.of());

    private final List<Profile> profiles;

    private ProfileLibrary(List<Profile> sorted) {
        this.profiles = List.copyOf(sorted);
    }

    public static ProfileLibrary empty() {
        return EMPTY;
    }

    /**
     * Builds a library from stored profiles. When two share a name, the last one wins.
     *
     * @throws IllegalArgumentException with more than {@link #MAX_PROFILES} distinct profiles
     */
    public static ProfileLibrary of(List<Profile> profiles) {
        ProfileLibrary library = EMPTY;
        for (Profile profile : profiles) {
            library = library.save(profile);
        }
        return library;
    }

    /**
     * Adds the profile, or replaces the one with the same name (keeping the new spelling).
     *
     * @throws IllegalArgumentException when adding a new profile to a full library
     */
    public ProfileLibrary save(Profile profile) {
        Objects.requireNonNull(profile, "profile");
        final List<Profile> updated = new ArrayList<>(profiles);
        final boolean replaced = updated.removeIf(existing -> existing.name().sameAs(profile.name()));
        if (!replaced && profiles.size() >= MAX_PROFILES) {
            throw new IllegalArgumentException("A library holds at most " + MAX_PROFILES + " profiles");
        }
        updated.add(profile);
        updated.sort(BY_NAME);
        return new ProfileLibrary(updated);
    }

    /** Removes the profile with that name, if any. */
    public ProfileLibrary delete(ProfileName name) {
        Objects.requireNonNull(name, "name");
        final List<Profile> updated = new ArrayList<>(profiles);
        return updated.removeIf(existing -> existing.name().sameAs(name)) ? new ProfileLibrary(updated) : this;
    }

    public Optional<Profile> find(ProfileName name) {
        Objects.requireNonNull(name, "name");
        return profiles.stream().filter(profile -> profile.name().sameAs(name)).findFirst();
    }

    public boolean contains(ProfileName name) {
        return find(name).isPresent();
    }

    public boolean isFull() {
        return profiles.size() >= MAX_PROFILES;
    }

    /** Profiles sorted by name, ignoring case and accents. */
    public List<Profile> profiles() {
        return profiles;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof ProfileLibrary that && profiles.equals(that.profiles);
    }

    @Override
    public int hashCode() {
        return profiles.hashCode();
    }

    @Override
    public String toString() {
        return "ProfileLibrary" + profiles.stream().map(Profile::name).toList();
    }

    private static final Comparator<Profile> BY_NAME = byName();

    private static Comparator<Profile> byName() {
        final Collator collator = Collator.getInstance(Locale.ROOT);
        collator.setStrength(Collator.PRIMARY);
        final Comparator<String> ignoringCaseAndAccents = collator::compare;
        return Comparator.comparing((Profile profile) -> profile.name().value(), ignoringCaseAndAccents)
                .thenComparing(profile -> profile.name().value());
    }
}
