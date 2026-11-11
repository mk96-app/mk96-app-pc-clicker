package com.markes96.pcclicker.domain.settings;

import java.util.Objects;

/**
 * Everything kept between launches: the settings last used (the workspace), the saved profiles,
 * the favourite profiles and the preferences.
 */
public record UserSettings(SessionSettings workspace, ProfileLibrary profiles, Preferences preferences,
                           Favorites favorites) {

    private static final UserSettings DEFAULTS = new UserSettings(
            SessionSettings.defaults(), ProfileLibrary.empty(), Preferences.defaults(), Favorites.none());

    public UserSettings {
        Objects.requireNonNull(workspace, "workspace");
        Objects.requireNonNull(profiles, "profiles");
        Objects.requireNonNull(preferences, "preferences");
        Objects.requireNonNull(favorites, "favorites");
    }

    /** Without favourites. */
    public UserSettings(SessionSettings workspace, ProfileLibrary profiles, Preferences preferences) {
        this(workspace, profiles, preferences, Favorites.none());
    }

    /** First launch: nothing saved yet. */
    public static UserSettings defaults() {
        return DEFAULTS;
    }

    public UserSettings withWorkspace(SessionSettings newWorkspace) {
        return new UserSettings(newWorkspace, profiles, preferences, favorites);
    }

    public UserSettings withProfiles(ProfileLibrary newProfiles) {
        return new UserSettings(workspace, newProfiles, preferences, favorites);
    }

    public UserSettings withPreferences(Preferences newPreferences) {
        return new UserSettings(workspace, profiles, newPreferences, favorites);
    }

    public UserSettings withFavorites(Favorites newFavorites) {
        return new UserSettings(workspace, profiles, preferences, newFavorites);
    }

    /** Deletes a saved profile and forgets it as a favourite, so a new one with its name starts plain. */
    public UserSettings deleteProfile(ProfileName name) {
        return new UserSettings(workspace, profiles.delete(name), preferences, favorites.without(ProfileRef.of(name)));
    }
}
