package com.markes96.pcclicker.adapter.in.hotkey;

import com.markes96.pcclicker.application.port.in.HandleHotkeyUseCase;
import com.markes96.pcclicker.domain.control.KeyBindings;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Turns raw key presses into hotkey commands. Commands are handed to an executor so the native
 * hook thread is never blocked (or torn down from inside its own callback when exiting).
 */
final class HotkeyDispatcher {

    private static final Logger LOG = System.getLogger(HotkeyDispatcher.class.getName());

    private final KeyBindings bindings;
    private final HandleHotkeyUseCase useCase;
    private final Executor executor;

    HotkeyDispatcher(KeyBindings bindings, HandleHotkeyUseCase useCase, Executor executor) {
        this.bindings = Objects.requireNonNull(bindings, "bindings");
        this.useCase = Objects.requireNonNull(useCase, "useCase");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    void onKeyReleased(int virtualKeyCode) {
        WindowsVirtualKeys.keyOf(virtualKeyCode)
                .flatMap(bindings::hotkeyFor)
                .ifPresent(hotkey -> executor.execute(() -> {
                    try {
                        useCase.handle(hotkey);
                    } catch (RuntimeException failure) {
                        LOG.log(Level.ERROR, "Hotkey " + hotkey + " failed", failure);
                    }
                }));
    }
}
