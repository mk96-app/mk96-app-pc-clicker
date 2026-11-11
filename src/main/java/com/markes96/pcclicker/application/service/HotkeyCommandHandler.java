package com.markes96.pcclicker.application.service;

import com.markes96.pcclicker.application.port.in.ExitApplicationUseCase;
import com.markes96.pcclicker.application.port.in.HandleHotkeyUseCase;
import com.markes96.pcclicker.application.port.in.PreferencesUseCase;
import com.markes96.pcclicker.application.port.in.SessionAlreadyActiveException;
import com.markes96.pcclicker.application.port.in.SessionQuery;
import com.markes96.pcclicker.application.port.in.StartSessionUseCase;
import com.markes96.pcclicker.application.port.in.StopSessionUseCase;
import com.markes96.pcclicker.application.port.in.SuspendHotkeysUseCase;
import com.markes96.pcclicker.application.port.in.TogglePauseUseCase;
import com.markes96.pcclicker.application.port.in.WorkspaceUseCase;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.Key;
import com.markes96.pcclicker.domain.settings.SessionSettings;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Translates global hotkeys into use cases. Keys are captured system-wide, so each hotkey only acts
 * when it makes sense: the start key while idle, the others while a session is active. Typing in
 * another program must never close or start anything by accident.
 */
public final class HotkeyCommandHandler implements HandleHotkeyUseCase, SuspendHotkeysUseCase {

    private static final Logger LOG = System.getLogger(HotkeyCommandHandler.class.getName());

    private final PreferencesUseCase preferences;
    private final WorkspaceUseCase workspace;
    private final SessionQuery sessions;
    private final StartSessionUseCase start;
    private final StopSessionUseCase stop;
    private final TogglePauseUseCase togglePause;
    private final ExitApplicationUseCase exit;
    private final AtomicBoolean suspended = new AtomicBoolean();

    public HotkeyCommandHandler(PreferencesUseCase preferences, WorkspaceUseCase workspace, SessionQuery sessions,
                                StartSessionUseCase start, StopSessionUseCase stop, TogglePauseUseCase togglePause,
                                ExitApplicationUseCase exit) {
        this.preferences = Objects.requireNonNull(preferences, "preferences");
        this.workspace = Objects.requireNonNull(workspace, "workspace");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.start = Objects.requireNonNull(start, "start");
        this.stop = Objects.requireNonNull(stop, "stop");
        this.togglePause = Objects.requireNonNull(togglePause, "togglePause");
        this.exit = Objects.requireNonNull(exit, "exit");
    }

    @Override
    public void keyReleased(Key key) {
        Objects.requireNonNull(key, "key");
        if (suspended.get()) {
            return;
        }
        preferences.preferences().keys().hotkeyFor(key).ifPresent(this::handle);
    }

    @Override
    public void suspendHotkeys() {
        suspended.set(true);
    }

    @Override
    public void resumeHotkeys() {
        suspended.set(false);
    }

    private void handle(Hotkey hotkey) {
        if (hotkey.controlsActiveSession() != sessions.isSessionActive()) {
            return;
        }
        switch (hotkey) {
            case START -> startWithWorkspace();
            case EXIT -> exit.exit();
            case STOP -> stop.stop();
            case TOGGLE_PAUSE -> togglePause.togglePause();
        }
    }

    private void startWithWorkspace() {
        final SessionSettings settings = workspace.workspace();
        if (!settings.canStart()) {
            LOG.log(Level.INFO, "Start hotkey ignored: no action is enabled");
            return;
        }
        try {
            start.start(settings.toConfiguration());
        } catch (SessionAlreadyActiveException alreadyStarted) {
            // Started from the window a moment ago: nothing left to do.
        }
    }
}
