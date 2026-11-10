package com.markes96.pcclicker.domain.control;

/**
 * Physical keys that can be bound to a {@link Hotkey}, independent of any platform key code.
 * Limited to keys that exist on every keyboard layout.
 */
public enum Key {

    ESCAPE("Esc"),
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
    F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12;

    private final String label;

    Key() {
        this.label = name();
    }

    Key(String label) {
        this.label = label;
    }

    /** Short text to print on a key cap. */
    public String label() {
        return label;
    }
}
