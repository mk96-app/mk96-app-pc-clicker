package com.markes96.pcclicker.domain.settings;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FavoritesTest {

    private static final ProfileRef FARM = ProfileRef.of(new ProfileName("Farm"));
    private static final ProfileRef ANTI_AFK = ProfileRef.of(Preset.ANTI_AFK);

    @Test
    void starts_empty() {
        assertThat(Favorites.none().refs()).isEmpty();
        assertThat(Favorites.none().contains(FARM)).isFalse();
    }

    @Test
    void marks_and_unmarks_presets_and_saved_profiles() {
        final Favorites favorites = Favorites.none().with(FARM).set(ANTI_AFK, true);

        assertThat(favorites.contains(FARM)).isTrue();
        assertThat(favorites.contains(ANTI_AFK)).isTrue();
        assertThat(favorites.without(FARM).set(ANTI_AFK, false)).isEqualTo(Favorites.none());
    }

    @Test
    void marking_twice_changes_nothing_and_it_is_immutable() {
        final Favorites once = Favorites.none().with(FARM);

        assertThat(once.with(FARM)).isSameAs(once);
        assertThat(Favorites.none().without(FARM)).isSameAs(Favorites.none());
        assertThat(Favorites.none().refs()).isEmpty();
    }

    @Test
    void saved_profiles_are_identified_regardless_of_case() {
        assertThat(ProfileRef.of(new ProfileName("FARM"))).isEqualTo(FARM);
        assertThat(Favorites.of(Set.of(FARM)).contains(ProfileRef.of(new ProfileName("farm")))).isTrue();
    }

    @Test
    void deleting_a_saved_profile_also_forgets_it_as_a_favourite() {
        final SessionSettings clicking = new SessionSettings(
                ClickSettings.enabled(new ClickRate(5)), MovementSettings.disabled(), TimeLimit.unlimited());
        final UserSettings settings = UserSettings.defaults()
                .withProfiles(ProfileLibrary.empty().save(new Profile(new ProfileName("Farm"), clicking)))
                .withFavorites(Favorites.none().with(FARM).with(ANTI_AFK));

        final UserSettings updated = settings.deleteProfile(new ProfileName("farm"));

        assertThat(updated.profiles().profiles()).isEmpty();
        assertThat(updated.favorites()).isEqualTo(Favorites.none().with(ANTI_AFK));
    }
}
