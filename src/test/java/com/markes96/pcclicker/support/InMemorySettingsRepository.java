package com.markes96.pcclicker.support;

import com.markes96.pcclicker.application.port.out.SettingsStorageException;
import com.markes96.pcclicker.application.port.out.UserSettingsRepository;
import com.markes96.pcclicker.domain.settings.UserSettings;
import java.util.Optional;

/** Keeps the settings in memory, counts writes and can be told to fail like a full disk. */
public final class InMemorySettingsRepository implements UserSettingsRepository {

    private UserSettings stored;
    private int saves;
    private boolean failReads;
    private boolean failWrites;

    public InMemorySettingsRepository() {
    }

    public InMemorySettingsRepository(UserSettings stored) {
        this.stored = stored;
    }

    @Override
    public Optional<UserSettings> load() {
        if (failReads) {
            throw new SettingsStorageException("Unreadable", new IllegalStateException("corrupt"));
        }
        return Optional.ofNullable(stored);
    }

    @Override
    public void save(UserSettings settings) {
        if (failWrites) {
            throw new SettingsStorageException("Disk full", new IllegalStateException("no space"));
        }
        stored = settings;
        saves++;
    }

    public Optional<UserSettings> stored() {
        return Optional.ofNullable(stored);
    }

    public int saves() {
        return saves;
    }

    public void failReads() {
        failReads = true;
    }

    public void failWrites(boolean fail) {
        failWrites = fail;
    }
}
