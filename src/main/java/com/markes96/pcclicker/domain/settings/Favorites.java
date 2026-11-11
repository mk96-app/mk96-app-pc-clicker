package com.markes96.pcclicker.domain.settings;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Profiles the user marked with a star, offered before the rest. Immutable. */
public final class Favorites {

    private static final Favorites NONE = new Favorites(Set.of());

    private final Set<ProfileRef> refs;

    private Favorites(Set<ProfileRef> refs) {
        this.refs = Set.copyOf(refs);
    }

    public static Favorites none() {
        return NONE;
    }

    public static Favorites of(Set<ProfileRef> refs) {
        return refs.isEmpty() ? NONE : new Favorites(refs);
    }

    public boolean contains(ProfileRef ref) {
        return refs.contains(Objects.requireNonNull(ref, "ref"));
    }

    public Favorites with(ProfileRef ref) {
        if (contains(ref)) {
            return this;
        }
        final Set<ProfileRef> updated = new LinkedHashSet<>(refs);
        updated.add(ref);
        return new Favorites(updated);
    }

    public Favorites without(ProfileRef ref) {
        if (!contains(ref)) {
            return this;
        }
        final Set<ProfileRef> updated = new LinkedHashSet<>(refs);
        updated.remove(ref);
        return of(updated);
    }

    public Favorites set(ProfileRef ref, boolean favorite) {
        return favorite ? with(ref) : without(ref);
    }

    public Set<ProfileRef> refs() {
        return refs;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Favorites that && refs.equals(that.refs);
    }

    @Override
    public int hashCode() {
        return refs.hashCode();
    }

    @Override
    public String toString() {
        return "Favorites" + refs;
    }
}
