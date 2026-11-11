package com.markes96.pcclicker.adapter.in.ui.format;

import com.markes96.pcclicker.domain.control.Key;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.scene.input.KeyCode;

/** Translation from JavaFX key codes to the keys a hotkey can use. */
public final class KeyCodes {

    private static final Set<String> BINDABLE = Arrays.stream(Key.values()).map(Key::name).collect(Collectors.toSet());

    private KeyCodes() {
    }

    /** The domain key for {@code code}, or empty when that key cannot trigger a hotkey. */
    public static Optional<Key> keyOf(KeyCode code) {
        // Domain keys are named like their JavaFX counterparts: A..Z, F1..F12, ESCAPE.
        return code != null && BINDABLE.contains(code.name()) ? Optional.of(Key.valueOf(code.name())) : Optional.empty();
    }
}
