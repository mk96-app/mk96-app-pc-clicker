package com.markes96.pcclicker.adapter.in.ui;

import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.PreferencesViewModel;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.SessionViewModel;
import com.markes96.pcclicker.application.port.in.ExitApplicationUseCase;
import com.markes96.pcclicker.application.port.in.ProfilesUseCase;
import com.markes96.pcclicker.application.port.in.StartSessionUseCase;
import com.markes96.pcclicker.application.port.in.StopSessionUseCase;
import com.markes96.pcclicker.application.port.in.SuspendHotkeysUseCase;
import com.markes96.pcclicker.application.port.in.WorkspaceUseCase;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Everything the desktop UI needs from the outside, handed over by the composition root. The UI
 * only talks to the application through these use cases.
 *
 * @param preferences      observable preferences, shared by every screen
 * @param hotkeysAvailable false when the global keyboard hook could not be installed
 * @param settingsLocation where the settings are stored, shown to the user
 * @param browser          opens a URL in the user's browser
 */
public record UiContext(
        StartSessionUseCase startSession,
        StopSessionUseCase stopSession,
        ExitApplicationUseCase exitApplication,
        WorkspaceUseCase workspace,
        ProfilesUseCase profiles,
        SuspendHotkeysUseCase hotkeys,
        SessionViewModel session,
        PreferencesViewModel preferences,
        boolean hotkeysAvailable,
        String settingsLocation,
        ApplicationInfo info,
        Messages messages,
        Consumer<String> browser) {

    public UiContext {
        Objects.requireNonNull(startSession, "startSession");
        Objects.requireNonNull(stopSession, "stopSession");
        Objects.requireNonNull(exitApplication, "exitApplication");
        Objects.requireNonNull(workspace, "workspace");
        Objects.requireNonNull(profiles, "profiles");
        Objects.requireNonNull(hotkeys, "hotkeys");
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(preferences, "preferences");
        Objects.requireNonNull(settingsLocation, "settingsLocation");
        Objects.requireNonNull(info, "info");
        Objects.requireNonNull(messages, "messages");
        Objects.requireNonNull(browser, "browser");
    }

    /** Whether the floating panel must be shown: always when there are no hotkeys to stop with. */
    public boolean showsPanel() {
        return preferences.current().showPanel() || !hotkeysAvailable;
    }
}
