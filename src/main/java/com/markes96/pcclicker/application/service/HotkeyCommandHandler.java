package com.markes96.pcclicker.application.service;

import com.markes96.pcclicker.application.port.in.ExitApplicationUseCase;
import com.markes96.pcclicker.application.port.in.HandleHotkeyUseCase;
import com.markes96.pcclicker.application.port.in.SessionQuery;
import com.markes96.pcclicker.application.port.in.StopSessionUseCase;
import com.markes96.pcclicker.application.port.in.TogglePauseUseCase;
import com.markes96.pcclicker.domain.control.Hotkey;
import java.util.Objects;

/**
 * Translates global hotkeys into use cases. Keys are captured system-wide, so they are ignored
 * unless a session is active: typing in another program must not close this one.
 */
public final class HotkeyCommandHandler implements HandleHotkeyUseCase {

    private final SessionQuery sessions;
    private final StopSessionUseCase stop;
    private final TogglePauseUseCase togglePause;
    private final ExitApplicationUseCase exit;

    public HotkeyCommandHandler(SessionQuery sessions, StopSessionUseCase stop, TogglePauseUseCase togglePause,
                                ExitApplicationUseCase exit) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.stop = Objects.requireNonNull(stop, "stop");
        this.togglePause = Objects.requireNonNull(togglePause, "togglePause");
        this.exit = Objects.requireNonNull(exit, "exit");
    }

    @Override
    public void handle(Hotkey hotkey) {
        Objects.requireNonNull(hotkey, "hotkey");
        if (!sessions.isSessionActive()) {
            return;
        }
        switch (hotkey) {
            case EXIT -> exit.exit();
            case STOP -> stop.stop();
            case TOGGLE_PAUSE -> togglePause.togglePause();
        }
    }
}
