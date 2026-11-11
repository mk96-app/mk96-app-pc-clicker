package com.markes96.pcclicker.application.port.in;

import com.markes96.pcclicker.domain.settings.SessionSettings;

/**
 * The session settings currently being edited. They are remembered between launches and are what
 * the start hotkey launches.
 */
public interface WorkspaceUseCase {

    SessionSettings workspace();

    /**
     * Replaces the current settings. Cheap enough to call on every edit: they are written to
     * storage later, together with any other change or when the application closes.
     */
    void updateWorkspace(SessionSettings settings);
}
