package com.markes96.pcclicker.application.port.out;

import com.markes96.pcclicker.domain.settings.UserSettings;
import java.util.Optional;

/** Keeps the user's settings between launches. */
public interface UserSettingsRepository {

    /**
     * Reads what was saved. Values that are missing or no longer valid are replaced by their
     * defaults instead of failing the whole read.
     *
     * @return empty when nothing has been saved yet
     * @throws SettingsStorageException when the stored settings exist but cannot be read
     */
    Optional<UserSettings> load();

    /**
     * Replaces what was saved. Either the whole new state is stored or the old one is kept.
     *
     * @throws SettingsStorageException when the settings cannot be written
     */
    void save(UserSettings settings);
}
