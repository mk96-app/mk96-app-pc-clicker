package com.markes96.pcclicker.domain.control;

/**
 * Commands available from the keyboard anywhere in the system. {@link #START} only works while no
 * session is active; the others only while one is. Which key triggers each one is defined by
 * {@link KeyBindings}.
 */
public enum Hotkey {
    START,
    EXIT,
    STOP,
    TOGGLE_PAUSE;

    /** Whether the hotkey acts on a session in progress, as opposed to starting a new one. */
    public boolean controlsActiveSession() {
        return this != START;
    }
}
