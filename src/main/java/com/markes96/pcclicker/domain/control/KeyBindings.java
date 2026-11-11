package com.markes96.pcclicker.domain.control;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Which key triggers each {@link Hotkey}. Every hotkey is bound, and to a different key. */
public final class KeyBindings {

    private static final KeyBindings DEFAULTS = new KeyBindings(Map.of(
            Hotkey.START, Key.F6,
            Hotkey.EXIT, Key.ESCAPE,
            Hotkey.STOP, Key.X,
            Hotkey.TOGGLE_PAUSE, Key.P));

    private final Map<Hotkey, Key> keys;

    public KeyBindings(Map<Hotkey, Key> keys) {
        Objects.requireNonNull(keys, "keys");
        final Set<Hotkey> unbound = EnumSet.allOf(Hotkey.class);
        unbound.removeAll(keys.keySet());
        if (!unbound.isEmpty()) {
            throw new IllegalArgumentException("Every hotkey needs a key; missing " + unbound);
        }
        if (new HashSet<>(keys.values()).size() != keys.size()) {
            throw new IllegalArgumentException("A key cannot trigger two hotkeys: " + keys);
        }
        this.keys = new EnumMap<>(keys);
    }

    public static KeyBindings defaults() {
        return DEFAULTS;
    }

    public Key keyFor(Hotkey hotkey) {
        return keys.get(hotkey);
    }

    /**
     * Binds {@code hotkey} to {@code key}. If another hotkey was using that key, the two swap keys,
     * so every hotkey stays bound and no key triggers two of them.
     */
    public KeyBindings with(Hotkey hotkey, Key key) {
        Objects.requireNonNull(hotkey, "hotkey");
        Objects.requireNonNull(key, "key");
        final Map<Hotkey, Key> updated = new EnumMap<>(keys);
        hotkeyFor(key).ifPresent(previousOwner -> updated.put(previousOwner, keys.get(hotkey)));
        updated.put(hotkey, key);
        return new KeyBindings(updated);
    }

    /** Every hotkey with its key, in declaration order. */
    public Map<Hotkey, Key> asMap() {
        return Collections.unmodifiableMap(keys);
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof KeyBindings that && keys.equals(that.keys);
    }

    @Override
    public int hashCode() {
        return keys.hashCode();
    }

    @Override
    public String toString() {
        return "KeyBindings" + keys;
    }

    public Optional<Hotkey> hotkeyFor(Key key) {
        return keys.entrySet().stream()
                .filter(entry -> entry.getValue() == key)
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
