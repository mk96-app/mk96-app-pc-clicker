package com.markes96.pcclicker.adapter.in.hotkey;

import com.markes96.pcclicker.application.port.in.HandleHotkeyUseCase;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Forwards the keys the application understands to it. Which hotkey a key triggers is decided by
 * the application, with the bindings in force at that moment. Work is handed to an executor so the
 * native hook thread is never blocked (or torn down from inside its own callback when exiting).
 */
final class HotkeyDispatcher {

    private static final Logger LOG = System.getLogger(HotkeyDispatcher.class.getName());

    private final HandleHotkeyUseCase useCase;
    private final Executor executor;

    HotkeyDispatcher(HandleHotkeyUseCase useCase, Executor executor) {
        this.useCase = Objects.requireNonNull(useCase, "useCase");
        this.executor = Objects.requireNonNull(executor, "executor");
    }

    void onKeyReleased(int virtualKeyCode) {
        WindowsVirtualKeys.keyOf(virtualKeyCode).ifPresent(key -> executor.execute(() -> {
            try {
                useCase.keyReleased(key);
            } catch (RuntimeException failure) {
                LOG.log(Level.ERROR, "Handling key " + key + " failed", failure);
            }
        }));
    }
}
