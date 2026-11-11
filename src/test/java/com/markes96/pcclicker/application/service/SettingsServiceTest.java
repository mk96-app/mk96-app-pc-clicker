package com.markes96.pcclicker.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.markes96.pcclicker.application.port.out.SettingsStorageException;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.Key;
import com.markes96.pcclicker.domain.settings.CountdownSeconds;
import com.markes96.pcclicker.domain.settings.Preferences;
import com.markes96.pcclicker.domain.settings.Preset;
import com.markes96.pcclicker.domain.settings.Profile;
import com.markes96.pcclicker.domain.settings.ProfileLibrary;
import com.markes96.pcclicker.domain.settings.ProfileName;
import com.markes96.pcclicker.domain.settings.ProfileRef;
import com.markes96.pcclicker.domain.settings.SessionSettings;
import com.markes96.pcclicker.domain.settings.UserSettings;
import com.markes96.pcclicker.support.InMemorySettingsRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SettingsServiceTest {

    private static SessionSettings clicking(int perSecond) {
        return new SessionSettings(ClickSettings.enabled(new ClickRate(perSecond)), MovementSettings.disabled(),
                TimeLimit.unlimited());
    }

    private final InMemorySettingsRepository repository = new InMemorySettingsRepository();

    @Nested
    class Loading {

        @Test
        void starts_with_the_defaults_on_the_first_launch() {
            final SettingsService service = SettingsService.load(repository);

            assertThat(service.workspace()).isEqualTo(SessionSettings.defaults());
            assertThat(service.profiles()).isEmpty();
            assertThat(service.preferences()).isEqualTo(Preferences.defaults());
        }

        @Test
        void restores_what_was_stored() {
            final UserSettings stored = new UserSettings(
                    clicking(25),
                    ProfileLibrary.empty().save(new Profile(new ProfileName("Farm"), clicking(50))),
                    Preferences.defaults().withCountdown(new CountdownSeconds(5)));

            final SettingsService service = SettingsService.load(new InMemorySettingsRepository(stored));

            assertThat(service.workspace()).isEqualTo(clicking(25));
            assertThat(service.find(new ProfileName("farm"))).map(Profile::settings).contains(clicking(50));
            assertThat(service.preferences().countdown().value()).isEqualTo(5);
        }

        @Test
        void unreadable_storage_falls_back_to_the_defaults() {
            repository.failReads();

            final SettingsService service = SettingsService.load(repository);

            assertThat(service.preferences()).isEqualTo(Preferences.defaults());
        }
    }

    @Nested
    class Workspace {

        @Test
        void is_kept_in_memory_until_flushed() {
            final SettingsService service = SettingsService.load(repository);

            service.updateWorkspace(clicking(10));
            service.updateWorkspace(clicking(20));

            assertThat(service.workspace()).isEqualTo(clicking(20));
            assertThat(repository.saves()).isZero();

            service.flush();
            assertThat(repository.stored()).map(UserSettings::workspace).contains(clicking(20));
        }

        @Test
        void flushing_without_changes_writes_nothing() {
            final SettingsService service = SettingsService.load(repository);

            service.updateWorkspace(SessionSettings.defaults());
            service.flush();

            assertThat(repository.saves()).isZero();
        }

        @Test
        void is_written_along_with_the_next_stored_change() {
            final SettingsService service = SettingsService.load(repository);

            service.updateWorkspace(clicking(10));
            service.updatePreferences(Preferences.defaults().withShowPanel(false));
            service.flush();

            assertThat(repository.saves()).isEqualTo(1);
            assertThat(repository.stored()).map(UserSettings::workspace).contains(clicking(10));
        }

        @Test
        void a_failing_flush_does_not_throw() {
            final SettingsService service = SettingsService.load(repository);
            service.updateWorkspace(clicking(10));
            repository.failWrites(true);

            service.flush();

            assertThat(service.workspace()).isEqualTo(clicking(10));
        }
    }

    @Nested
    class Profiles {

        @Test
        void saving_and_deleting_are_stored_at_once() {
            final SettingsService service = SettingsService.load(repository);

            service.save(new Profile(new ProfileName("Farm"), clicking(50)));
            assertThat(repository.stored().orElseThrow().profiles().profiles()).hasSize(1);

            service.delete(new ProfileName("FARM"));
            assertThat(repository.stored().orElseThrow().profiles().profiles()).isEmpty();
            assertThat(repository.saves()).isEqualTo(2);
        }

        @Test
        void a_change_that_cannot_be_stored_still_applies_until_closing() {
            final SettingsService service = SettingsService.load(repository);
            repository.failWrites(true);

            assertThatThrownBy(() -> service.save(new Profile(new ProfileName("Farm"), clicking(50))))
                    .isInstanceOf(SettingsStorageException.class);

            assertThat(service.profiles()).extracting(Profile::name).containsExactly(new ProfileName("Farm"));
        }

        @Test
        void favourites_are_stored_at_once_for_presets_and_saved_profiles() {
            final SettingsService service = SettingsService.load(repository);
            service.save(new Profile(new ProfileName("Farm"), clicking(50)));

            service.setFavorite(ProfileRef.of(Preset.ANTI_AFK), true);
            service.setFavorite(ProfileRef.of(new ProfileName("farm")), true);

            assertThat(service.favorites().refs()).containsExactlyInAnyOrder(
                    ProfileRef.of(Preset.ANTI_AFK), ProfileRef.of(new ProfileName("Farm")));
            assertThat(repository.stored().orElseThrow().favorites()).isEqualTo(service.favorites());

            service.setFavorite(ProfileRef.of(Preset.ANTI_AFK), false);
            assertThat(service.favorites().refs()).containsExactly(ProfileRef.of(new ProfileName("Farm")));
        }

        @Test
        void a_saved_profile_that_does_not_exist_cannot_be_a_favourite() {
            final SettingsService service = SettingsService.load(repository);

            service.setFavorite(ProfileRef.of(new ProfileName("Ghost")), true);

            assertThat(service.favorites().refs()).isEmpty();
            assertThat(repository.saves()).isZero();
        }

        @Test
        void deleting_a_profile_removes_its_star() {
            final SettingsService service = SettingsService.load(repository);
            service.save(new Profile(new ProfileName("Farm"), clicking(50)));
            service.setFavorite(ProfileRef.of(new ProfileName("Farm")), true);

            service.delete(new ProfileName("Farm"));
            service.save(new Profile(new ProfileName("Farm"), clicking(10)));

            assertThat(service.favorites().refs()).isEmpty();
        }

        @Test
        void offers_every_preset_in_order() {
            assertThat(SettingsService.load(repository).presets()).containsExactly(Preset.values());
        }

        @Test
        void deleting_a_missing_profile_writes_nothing() {
            final SettingsService service = SettingsService.load(repository);

            service.delete(new ProfileName("missing"));

            assertThat(repository.saves()).isZero();
        }
    }

    @Nested
    class PreferencesChanges {

        @Test
        void are_stored_at_once() {
            final SettingsService service = SettingsService.load(repository);
            final Preferences changed = Preferences.defaults()
                    .withKeys(Preferences.defaults().keys().with(Hotkey.START, Key.F8));

            service.updatePreferences(changed);

            assertThat(service.preferences()).isEqualTo(changed);
            assertThat(repository.stored()).map(UserSettings::preferences).contains(changed);
        }

        @Test
        void the_same_preferences_are_not_written_again() {
            final SettingsService service = SettingsService.load(repository);

            service.updatePreferences(Preferences.defaults());

            assertThat(repository.saves()).isZero();
        }
    }
}
