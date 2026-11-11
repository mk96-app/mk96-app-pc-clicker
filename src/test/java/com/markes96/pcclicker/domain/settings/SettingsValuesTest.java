package com.markes96.pcclicker.domain.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.IntRange;
import com.markes96.pcclicker.domain.configuration.InvalidSessionConfigurationException;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.Key;
import com.markes96.pcclicker.domain.control.KeyBindings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SettingsValuesTest {

    @Nested
    class ProfileNameTest {

        @Test
        void is_trimmed() {
            assertThat(new ProfileName("  Farm 25 cps ").value()).isEqualTo("Farm 25 cps");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "tab\there", "line\nbreak"})
        void rejects_blank_names_and_control_characters(String invalid) {
            assertThatIllegalArgumentException().isThrownBy(() -> new ProfileName(invalid));
            assertThat(ProfileName.isValid(invalid)).isFalse();
        }

        @Test
        void has_a_maximum_length() {
            assertThat(ProfileName.isValid("x".repeat(ProfileName.MAX_LENGTH))).isTrue();
            assertThat(ProfileName.isValid("x".repeat(ProfileName.MAX_LENGTH + 1))).isFalse();
            assertThat(ProfileName.isValid(null)).isFalse();
        }

        @Test
        void names_differing_only_in_case_are_the_same_profile() {
            assertThat(new ProfileName("Farm").sameAs(new ProfileName("fARM"))).isTrue();
            assertThat(new ProfileName("Farm").sameAs(new ProfileName("Farm 2"))).isFalse();
        }
    }

    @Nested
    class CountdownSecondsTest {

        @Test
        void goes_from_immediate_to_ten_seconds_and_defaults_to_three() {
            assertThat(CountdownSeconds.RANGE).isEqualTo(new IntRange(0, 10));
            assertThat(CountdownSeconds.DEFAULT.value()).isEqualTo(3);
            assertThat(new CountdownSeconds(0).isImmediate()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 11})
        void rejects_values_out_of_range(int invalid) {
            assertThatIllegalArgumentException().isThrownBy(() -> new CountdownSeconds(invalid));
        }
    }

    @Nested
    class SessionSettingsTest {

        @Test
        void may_have_every_action_off_but_then_cannot_start() {
            final SessionSettings settings = SessionSettings.defaults();

            assertThat(settings.canStart()).isFalse();
            assertThatThrownBy(settings::toConfiguration).isInstanceOf(InvalidSessionConfigurationException.class);
        }

        @Test
        void becomes_a_configuration_when_an_action_is_on() {
            final SessionSettings settings = new SessionSettings(
                    ClickSettings.enabled(new ClickRate(5)), MovementSettings.disabled(), TimeLimit.ofMinutes(3));

            assertThat(settings.canStart()).isTrue();
            assertThat(settings.toConfiguration().click()).isEqualTo(settings.click());
            assertThat(settings.toConfiguration().timeLimit()).isEqualTo(TimeLimit.ofMinutes(3));
        }
    }

    @Nested
    class PreferencesTest {

        @Test
        void defaults_to_the_standard_keys_a_three_second_countdown_and_the_panel_visible() {
            final Preferences preferences = Preferences.defaults();

            assertThat(preferences.keys()).isEqualTo(KeyBindings.defaults());
            assertThat(preferences.countdown()).isEqualTo(CountdownSeconds.DEFAULT);
            assertThat(preferences.showPanel()).isTrue();
        }

        @Test
        void each_preference_changes_independently() {
            final Preferences changed = Preferences.defaults()
                    .withKeys(KeyBindings.defaults().with(Hotkey.STOP, Key.S))
                    .withCountdown(new CountdownSeconds(0))
                    .withShowPanel(false);

            assertThat(changed.keys().keyFor(Hotkey.STOP)).isEqualTo(Key.S);
            assertThat(changed.countdown().isImmediate()).isTrue();
            assertThat(changed.showPanel()).isFalse();
        }
    }

    @Nested
    class UserSettingsTest {

        @Test
        void starts_with_nothing_saved() {
            final UserSettings settings = UserSettings.defaults();

            assertThat(settings.workspace()).isEqualTo(SessionSettings.defaults());
            assertThat(settings.profiles()).isEqualTo(ProfileLibrary.empty());
            assertThat(settings.preferences()).isEqualTo(Preferences.defaults());
        }
    }
}
