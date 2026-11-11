package com.markes96.pcclicker.application.port.in;

import com.markes96.pcclicker.domain.settings.Preferences;

/**
 * Application preferences: hotkeys, countdown and floating panel. Changes apply immediately, also
 * to the global hotkeys, and are stored at once.
 */
public interface PreferencesUseCase {

    Preferences preferences();

    /**
     * @throws com.markes96.pcclicker.application.port.out.SettingsStorageException when the change
     *         could not be written; it is still applied for the rest of the run
     */
    void updatePreferences(Preferences preferences);
}
