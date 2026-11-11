package com.markes96.pcclicker.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.markes96.pcclicker.application.port.out.SettingsStorageException;
import com.markes96.pcclicker.domain.configuration.ClickLimit;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.RateVariation;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.Key;
import com.markes96.pcclicker.domain.control.KeyBindings;
import com.markes96.pcclicker.domain.settings.CountdownSeconds;
import com.markes96.pcclicker.domain.settings.Favorites;
import com.markes96.pcclicker.domain.settings.Preferences;
import com.markes96.pcclicker.domain.settings.Preset;
import com.markes96.pcclicker.domain.settings.Profile;
import com.markes96.pcclicker.domain.settings.ProfileLibrary;
import com.markes96.pcclicker.domain.settings.ProfileName;
import com.markes96.pcclicker.domain.settings.ProfileRef;
import com.markes96.pcclicker.domain.settings.SessionSettings;
import com.markes96.pcclicker.domain.settings.UserSettings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PropertiesSettingsRepositoryTest {

    @TempDir
    Path directory;

    private Path file() {
        return directory.resolve("nested").resolve("settings.properties");
    }

    private PropertiesSettingsRepository repository() {
        return new PropertiesSettingsRepository(file());
    }

    private static UserSettings everythingCustomised() {
        final SessionSettings workspace = new SessionSettings(
                new ClickSettings(true, new ClickRate(250), MouseButton.MIDDLE, ClickType.DOUBLE, new RateVariation(35),
                        ClickLimit.ofClicks(12_345)),
                MovementSettings.enabled(MovementPattern.POLYGON, new Radius(77), new VertexCount(9)),
                TimeLimit.ofMinutes(90));
        final SessionSettings idle = new SessionSettings(ClickSettings.disabled(),
                MovementSettings.enabled(MovementPattern.REBOUND, Radius.DEFAULT, VertexCount.DEFAULT),
                TimeLimit.unlimited());
        return new UserSettings(
                workspace,
                ProfileLibrary.of(List.of(
                        new Profile(new ProfileName("Farmeo 25 cps"), workspace),
                        new Profile(new ProfileName("Anti-AFK «círculo» = ok"), idle))),
                new Preferences(KeyBindings.defaults().with(Hotkey.START, Key.F9).with(Hotkey.STOP, Key.S),
                        new CountdownSeconds(0), false),
                Favorites.none()
                        .with(ProfileRef.of(Preset.HUMAN_CLICK))
                        .with(ProfileRef.of(new ProfileName("Farmeo 25 cps"))));
    }

    @Test
    void nothing_is_loaded_before_the_first_save() {
        assertThat(repository().load()).isEmpty();
    }

    @Test
    void every_setting_survives_a_round_trip() {
        final UserSettings settings = everythingCustomised();

        repository().save(settings);

        assertThat(repository().load()).contains(settings);
    }

    @Test
    void the_defaults_survive_a_round_trip() {
        repository().save(UserSettings.defaults());

        assertThat(repository().load()).contains(UserSettings.defaults());
    }

    @Test
    void saving_again_replaces_the_previous_content_and_leaves_no_temporary_files() throws IOException {
        repository().save(everythingCustomised());
        repository().save(UserSettings.defaults());

        assertThat(repository().load()).contains(UserSettings.defaults());
        try (var files = Files.list(file().getParent())) {
            assertThat(files).containsExactly(file());
        }
    }

    @Test
    void the_file_is_utf8_and_readable_by_a_person() throws IOException {
        repository().save(everythingCustomised());

        final String content = Files.readString(file(), StandardCharsets.UTF_8);

        assertThat(content)
                .contains("format=1")
                .contains("workspace.click.button=MIDDLE")
                .contains("preferences.key.START=F9")
                .contains("profile.0.name=Anti-AFK «círculo» \\= ok");
    }

    @Test
    void invalid_values_fall_back_to_their_defaults_without_losing_the_rest() throws IOException {
        write("""
                workspace.click.enabled=true
                workspace.click.rate=5000
                workspace.click.button=THUMB
                workspace.click.limit=abc
                workspace.movement.pattern=CIRCLE
                workspace.timeLimit.minutes=15
                preferences.countdownSeconds=7
                preferences.showPanel=maybe
                """);

        final UserSettings loaded = repository().load().orElseThrow();

        assertThat(loaded.workspace().click().enabled()).isTrue();
        assertThat(loaded.workspace().click().rate()).isEqualTo(ClickRate.DEFAULT);
        assertThat(loaded.workspace().click().button()).isEqualTo(MouseButton.LEFT);
        assertThat(loaded.workspace().click().limit()).isEqualTo(ClickLimit.unlimited());
        assertThat(loaded.workspace().movement().pattern()).isEqualTo(MovementPattern.CIRCLE);
        assertThat(loaded.workspace().timeLimit()).isEqualTo(TimeLimit.ofMinutes(15));
        assertThat(loaded.preferences().countdown().value()).isEqualTo(7);
        assertThat(loaded.preferences().showPanel()).isTrue();
    }

    @Test
    void favourites_of_unknown_presets_or_missing_profiles_are_dropped() throws IOException {
        write("""
                profiles.count=1
                profile.0.name=Farm
                favorites.count=4
                favorite.0=preset:ANTI_AFK
                favorite.1=preset:NOT_A_PRESET
                favorite.2=profile:farm
                favorite.3=profile:ghost
                """);

        assertThat(repository().load().orElseThrow().favorites().refs()).containsExactlyInAnyOrder(
                ProfileRef.of(Preset.ANTI_AFK), ProfileRef.of(new ProfileName("Farm")));
    }

    @Test
    void conflicting_hotkeys_restore_every_default_key() throws IOException {
        write("""
                preferences.key.START=X
                preferences.key.STOP=X
                """);

        assertThat(repository().load().orElseThrow().preferences().keys()).isEqualTo(KeyBindings.defaults());
    }

    @Test
    void profiles_with_an_invalid_name_are_skipped() throws IOException {
        write("""
                profiles.count=3
                profile.0.name=Good
                profile.0.click.enabled=true
                profile.1.name=\\u0020\\u0020
                profile.2.name=Also good
                """);

        assertThat(repository().load().orElseThrow().profiles().profiles())
                .extracting(profile -> profile.name().value())
                .containsExactly("Also good", "Good");
    }

    @Test
    void an_unreadable_file_is_reported() throws IOException {
        Files.createDirectories(file());

        assertThatThrownBy(() -> repository().load()).isInstanceOf(SettingsStorageException.class);
    }

    @Test
    void a_write_that_cannot_happen_is_reported() throws IOException {
        Files.writeString(directory.resolve("nested"), "a file where the directory should be");

        assertThatThrownBy(() -> repository().save(UserSettings.defaults()))
                .isInstanceOf(SettingsStorageException.class);
    }

    private void write(String content) throws IOException {
        Files.createDirectories(file().getParent());
        Files.writeString(file(), content, StandardCharsets.UTF_8);
    }
}
