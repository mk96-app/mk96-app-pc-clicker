package com.markes96.pcclicker.domain.settings;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PresetTest {

    @ParameterizedTest
    @EnumSource(Preset.class)
    void every_preset_can_start_a_session(Preset preset) {
        assertThat(preset.settings().canStart()).isTrue();
        assertThat(preset.settings().toConfiguration()).isNotNull();
    }

    @Test
    void every_preset_does_something_different() {
        assertThat(Arrays.stream(Preset.values()).map(Preset::settings).distinct()).hasSize(Preset.values().length);
    }

    @Test
    void cover_the_main_ways_of_using_the_application() {
        assertThat(Preset.ANTI_AFK.settings().click().enabled()).isFalse();
        assertThat(Preset.ANTI_AFK.settings().movement().enabled()).isTrue();
        assertThat(Preset.HUMAN_CLICK.settings().click().variation().isNone()).isFalse();
        assertThat(Preset.DOUBLE_CLICK.settings().click().type()).isEqualTo(ClickType.DOUBLE);
        assertThat(Preset.RIGHT_CLICK.settings().click().button()).isEqualTo(MouseButton.RIGHT);
        assertThat(Preset.ONE_HOUR_FARM.settings().timeLimit()).isEqualTo(TimeLimit.ofMinutes(60));
    }
}
