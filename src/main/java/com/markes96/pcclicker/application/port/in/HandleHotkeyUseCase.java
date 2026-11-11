package com.markes96.pcclicker.application.port.in;

import com.markes96.pcclicker.domain.control.Key;

/**
 * Reacts to a key released anywhere in the system. The key is looked up in the current
 * preferences, so rebinding a hotkey takes effect at once. The start hotkey only works while no
 * session is active; the others only while one is.
 */
@FunctionalInterface
public interface HandleHotkeyUseCase {

    void keyReleased(Key key);
}
