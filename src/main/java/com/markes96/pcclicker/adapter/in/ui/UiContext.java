package com.markes96.pcclicker.adapter.in.ui;

import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.SessionViewModel;
import com.markes96.pcclicker.application.port.in.ExitApplicationUseCase;
import com.markes96.pcclicker.application.port.in.StartSessionUseCase;
import com.markes96.pcclicker.application.port.in.StopSessionUseCase;
import com.markes96.pcclicker.domain.control.KeyBindings;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Everything the desktop UI needs from the outside, handed over by the composition root. The UI
 * only talks to the application through these use cases.
 *
 * @param countdownSeconds  seconds given to place the pointer, shown in hints
 * @param hotkeysAvailable  false when the global keyboard hook could not be installed
 * @param browser           opens a URL in the user's browser
 */
public record UiContext(
        StartSessionUseCase startSession,
        StopSessionUseCase stopSession,
        ExitApplicationUseCase exitApplication,
        SessionViewModel session,
        KeyBindings keyBindings,
        int countdownSeconds,
        boolean hotkeysAvailable,
        ApplicationInfo info,
        Messages messages,
        Consumer<String> browser) {

    public UiContext {
        Objects.requireNonNull(startSession, "startSession");
        Objects.requireNonNull(stopSession, "stopSession");
        Objects.requireNonNull(exitApplication, "exitApplication");
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(keyBindings, "keyBindings");
        Objects.requireNonNull(info, "info");
        Objects.requireNonNull(messages, "messages");
        Objects.requireNonNull(browser, "browser");
    }
}
