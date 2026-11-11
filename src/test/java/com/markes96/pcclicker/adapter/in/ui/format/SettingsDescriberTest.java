package com.markes96.pcclicker.adapter.in.ui.format;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
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
import com.markes96.pcclicker.domain.settings.Preset;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class SettingsDescriberTest {

    private final SettingsDescriber describer = new SettingsDescriber(Messages.forLocale(Locale.forLanguageTag("es-ES")));

    @Test
    void describes_clicking() {
        assertThat(describer.click(ClickSettings.enabled(new ClickRate(25)))).isEqualTo(nbsp("25_clics/s"));
        assertThat(describer.click(ClickSettings.disabled())).isEqualTo("Desactivado");
    }

    @Test
    void describes_only_the_click_options_that_differ_from_a_plain_steady_left_click() {
        final ClickSettings everything = ClickSettings.enabled(new ClickRate(25))
                .withButton(MouseButton.RIGHT)
                .withType(ClickType.DOUBLE)
                .withVariation(new RateVariation(20))
                .withLimit(ClickLimit.ofClicks(1_500));

        assertThat(describer.click(everything))
                .isEqualTo(nbsp("25_clics/s · botón_derecho · doble_clic · ±20_% · hasta_1.500_clics"));
        assertThat(describer.click(ClickSettings.enabled(new ClickRate(25)).withButton(MouseButton.MIDDLE)))
                .isEqualTo(nbsp("25_clics/s · botón_central"));
    }

    @Test
    void names_buttons_click_types_and_hotkeys() {
        assertThat(describer.buttonName(MouseButton.LEFT)).isEqualTo("Izquierdo");
        assertThat(describer.clickTypeName(ClickType.DOUBLE)).isEqualTo("Doble");
        assertThat(describer.hotkeyName(Hotkey.START)).isEqualTo("Iniciar");
        assertThat(describer.presetName(Preset.ANTI_AFK)).isEqualTo("Anti-AFK (círculos)");
    }

    @Test
    void describes_each_movement_pattern_with_only_its_relevant_parameters() {
        final Radius radius = new Radius(30);
        final VertexCount vertices = new VertexCount(6);

        assertThat(describer.movement(MovementSettings.enabled(MovementPattern.RANDOM, radius, vertices)))
                .isEqualTo("Aleatorio");
        assertThat(describer.movement(MovementSettings.enabled(MovementPattern.REBOUND, radius, vertices)))
                .isEqualTo("Rebote");
        assertThat(describer.movement(MovementSettings.enabled(MovementPattern.CIRCLE, radius, vertices)))
                .isEqualTo(nbsp("Círculo · radio_30_px"));
        assertThat(describer.movement(MovementSettings.enabled(MovementPattern.POLYGON, radius, vertices)))
                .isEqualTo(nbsp("Polígono · 6_vértices · radio_30_px"));
        assertThat(describer.movement(MovementSettings.disabled())).isEqualTo("Desactivado");
    }

    @Test
    void long_descriptions_can_only_wrap_between_parts() {
        final String description = describer.movement(
                MovementSettings.enabled(MovementPattern.POLYGON, new Radius(60), new VertexCount(5)));

        assertThat(description.split(SettingsDescriber.SEPARATOR)).allSatisfy(part -> assertThat(part).doesNotContain(" "));
    }

    /** Writes non-breaking spaces as underscores to keep expectations readable. */
    private static String nbsp(String text) {
        return text.replace('_', '\u00A0');
    }

    @Test
    void describes_the_time_limit() {
        assertThat(describer.duration(TimeLimit.unlimited())).isEqualTo("Sin límite");
        assertThat(describer.duration(TimeLimit.ofMinutes(90))).isEqualTo("1 h 30 min");
    }

    @Test
    void names_patterns() {
        assertThat(describer.patternName(MovementPattern.CIRCLE)).isEqualTo("Círculo");
        assertThat(describer.patternDescription(MovementPattern.REBOUND)).isNotBlank();
    }
}
