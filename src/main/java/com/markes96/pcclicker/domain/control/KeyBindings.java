package com.markes96.pcclicker.domain.control;

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

    public Optional<Hotkey> hotkeyFor(Key key) {
        return keys.entrySet().stream()
                .filter(entry -> entry.getValue() == key)
                .map(Map.Entry::getKey)
                .findFirst();
    }
}
