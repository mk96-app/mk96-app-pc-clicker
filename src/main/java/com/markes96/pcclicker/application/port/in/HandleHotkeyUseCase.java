package com.markes96.pcclicker.application.port.in;

import com.markes96.pcclicker.domain.control.Hotkey;

/** Reacts to a global hotkey. Hotkeys only have an effect while a session is active. */
@FunctionalInterface
public interface HandleHotkeyUseCase {

    void handle(Hotkey hotkey);
}
