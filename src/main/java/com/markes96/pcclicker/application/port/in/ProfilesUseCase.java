package com.markes96.pcclicker.application.port.in;

import com.markes96.pcclicker.domain.settings.Favorites;
import com.markes96.pcclicker.domain.settings.Preset;
import com.markes96.pcclicker.domain.settings.Profile;
import com.markes96.pcclicker.domain.settings.ProfileName;
import com.markes96.pcclicker.domain.settings.ProfileRef;
import java.util.List;
import java.util.Optional;

/**
 * Profiles: session settings to reuse in one click. The presets come with the application and
 * cannot change; the user's saved profiles can be created, replaced and deleted, and every change
 * is stored at once.
 *
 * <p>Saving and deleting throw {@link com.markes96.pcclicker.application.port.out.SettingsStorageException}
 * when the change could not be written; it is still applied for the rest of the run.
 */
public interface ProfilesUseCase {

    /** Profiles included with the application, in the order they should be offered. */
    List<Preset> presets();

    /** The user's saved profiles, sorted by name. */
    List<Profile> profiles();

    Optional<Profile> find(ProfileName name);

    /**
     * Adds the profile, or replaces the one with the same name regardless of case.
     *
     * @throws IllegalArgumentException when adding a new profile and the library is full
     */
    void save(Profile profile);

    /** Deletes a saved profile; it also stops being a favourite. */
    void delete(ProfileName name);

    /** Profiles marked with a star, offered first. */
    Favorites favorites();

    /** Marks or unmarks a profile as favourite. Saved profiles that do not exist are ignored. */
    void setFavorite(ProfileRef profile, boolean favorite);
}
