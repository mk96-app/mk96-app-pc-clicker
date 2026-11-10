package com.markes96.pcclicker.adapter.in.hotkey;

import com.markes96.pcclicker.application.port.in.HandleHotkeyUseCase;
import com.markes96.pcclicker.domain.control.KeyBindings;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lc.kra.system.keyboard.GlobalKeyboardHook;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

/**
 * Driving adapter that listens to the keyboard system-wide (also when other windows have the
 * focus) through a native Windows hook, and forwards bound keys to the application.
 */
public final class GlobalHotkeyListener implements AutoCloseable {

    private final GlobalKeyboardHook hook;
    private final GlobalKeyAdapter listener;
    private final ExecutorService executor;

    private GlobalHotkeyListener(GlobalKeyboardHook hook, HotkeyDispatcher dispatcher, ExecutorService executor) {
        this.hook = hook;
        this.executor = executor;
        this.listener = new GlobalKeyAdapter() {
            @Override
            public void keyReleased(GlobalKeyEvent event) {
                dispatcher.onKeyReleased(event.getVirtualKeyCode());
            }
        };
        hook.addKeyListener(listener);
    }

    /**
     * Installs the hook and starts listening.
     *
     * @throws UnsupportedOperationException if the platform has no native hook (non-Windows)
     */
    public static GlobalHotkeyListener install(KeyBindings bindings, HandleHotkeyUseCase useCase) {
        final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "hotkey-dispatcher");
            thread.setDaemon(true);
            return thread;
        });
        try {
            // Low-level hook (WH_KEYBOARD_LL), not raw input: raw input can stop Windows from sending
            // the usual key messages to this process, and the app's own text fields stop accepting
            // typing. The low-level hook only observes keys and lets them through untouched.
            final GlobalKeyboardHook hook = new GlobalKeyboardHook(false);
            return new GlobalHotkeyListener(hook, new HotkeyDispatcher(bindings, useCase, executor), executor);
        } catch (RuntimeException | UnsatisfiedLinkError failure) {
            executor.shutdownNow();
            throw new UnsupportedOperationException("Global keyboard hook is not available on this system", failure);
        }
    }

    @Override
    public void close() {
        hook.removeKeyListener(listener);
        hook.shutdownHook();
        executor.shutdownNow();
    }
}
