package com.markes96.pcclicker.application.service;

import com.markes96.pcclicker.application.port.in.PreferencesUseCase;
import com.markes96.pcclicker.application.port.in.ProfilesUseCase;
import com.markes96.pcclicker.application.port.in.WorkspaceUseCase;
import com.markes96.pcclicker.application.port.out.SettingsStorageException;
import com.markes96.pcclicker.application.port.out.UserSettingsRepository;
import com.markes96.pcclicker.domain.settings.Favorites;
import com.markes96.pcclicker.domain.settings.Preferences;
import com.markes96.pcclicker.domain.settings.Preset;
import com.markes96.pcclicker.domain.settings.Profile;
import com.markes96.pcclicker.domain.settings.ProfileName;
import com.markes96.pcclicker.domain.settings.ProfileRef;
import com.markes96.pcclicker.domain.settings.SessionSettings;
import com.markes96.pcclicker.domain.settings.UserSettings;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.UnaryOperator;

/**
 * Owns the user's settings while the application runs and keeps them in storage.
 *
 * <p>Profiles and preferences are written on every change, because losing them would be noticed.
 * The workspace changes with every keystroke in the form, so it is only written together with the
 * next stored change or on {@link #flush()} when the application closes.
 *
 * <p>Thread-safe: the UI edits settings while the hotkey thread reads them.
 */
public final class SettingsService implements WorkspaceUseCase, ProfilesUseCase, PreferencesUseCase {

    private static final Logger LOG = System.getLogger(SettingsService.class.getName());

    private final UserSettingsRepository repository;
    private final ReentrantLock lock = new ReentrantLock();

    private UserSettings settings;
    private boolean workspaceUnsaved;

    private SettingsService(UserSettingsRepository repository, UserSettings initial) {
        this.repository = repository;
        this.settings = initial;
    }

    /**
     * Loads the stored settings. Unreadable storage is logged and replaced by the defaults, so a
     * damaged file never prevents the application from starting.
     */
    public static SettingsService load(UserSettingsRepository repository) {
        Objects.requireNonNull(repository, "repository");
        UserSettings initial;
        try {
            initial = repository.load().orElseGet(UserSettings::defaults);
        } catch (SettingsStorageException unreadable) {
            LOG.log(Level.WARNING, "Stored settings could not be read; starting with the defaults", unreadable);
            initial = UserSettings.defaults();
        }
        return new SettingsService(repository, initial);
    }

    // --- Workspace ---

    @Override
    public SessionSettings workspace() {
        return read().workspace();
    }

    @Override
    public void updateWorkspace(SessionSettings workspace) {
        Objects.requireNonNull(workspace, "workspace");
        lock.lock();
        try {
            if (!workspace.equals(settings.workspace())) {
                settings = settings.withWorkspace(workspace);
                workspaceUnsaved = true;
            }
        } finally {
            lock.unlock();
        }
    }

    /** Writes the workspace if it changed since the last write. Never throws: called on shutdown. */
    public void flush() {
        lock.lock();
        try {
            if (workspaceUnsaved) {
                store();
            }
        } catch (SettingsStorageException failure) {
            LOG.log(Level.WARNING, "The last used settings could not be saved", failure);
        } finally {
            lock.unlock();
        }
    }

    // --- Profiles ---

    @Override
    public List<Preset> presets() {
        return List.of(Preset.values());
    }

    @Override
    public List<Profile> profiles() {
        return read().profiles().profiles();
    }

    @Override
    public Optional<Profile> find(ProfileName name) {
        return read().profiles().find(name);
    }

    @Override
    public void save(Profile profile) {
        Objects.requireNonNull(profile, "profile");
        change(current -> current.withProfiles(current.profiles().save(profile)));
    }

    @Override
    public void delete(ProfileName name) {
        Objects.requireNonNull(name, "name");
        change(current -> current.deleteProfile(name));
    }

    @Override
    public Favorites favorites() {
        return read().favorites();
    }

    @Override
    public void setFavorite(ProfileRef profile, boolean favorite) {
        Objects.requireNonNull(profile, "profile");
        change(current -> exists(current, profile)
                ? current.withFavorites(current.favorites().set(profile, favorite))
                : current);
    }

    private static boolean exists(UserSettings settings, ProfileRef profile) {
        return switch (profile) {
            case ProfileRef.OfPreset preset -> true;
            case ProfileRef.OfSaved saved -> settings.profiles().profiles().stream()
                    .anyMatch(stored -> stored.name().key().equals(saved.key()));
        };
    }

    // --- Preferences ---

    @Override
    public Preferences preferences() {
        return read().preferences();
    }

    @Override
    public void updatePreferences(Preferences preferences) {
        Objects.requireNonNull(preferences, "preferences");
        change(current -> current.withPreferences(preferences));
    }

    // --- Internals ---

    private UserSettings read() {
        lock.lock();
        try {
            return settings;
        } finally {
            lock.unlock();
        }
    }

    /** Applies the change in memory first, so it holds for this run even if it cannot be stored. */
    private void change(UnaryOperator<UserSettings> change) {
        lock.lock();
        try {
            final UserSettings updated = change.apply(settings);
            if (updated.equals(settings)) {
                return;
            }
            settings = updated;
            store();
        } finally {
            lock.unlock();
        }
    }

    private void store() {
        repository.save(settings);
        workspaceUnsaved = false;
    }
}
