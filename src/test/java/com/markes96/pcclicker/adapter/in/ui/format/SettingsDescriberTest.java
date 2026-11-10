package com.markes96.pcclicker.adapter.in.ui.format;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class SettingsDescriberTest {

    private final SettingsDescriber describer = new SettingsDescriber(Messages.forLocale(Locale.forLanguageTag("es-ES")));

    @Test
    void describes_clicking() {
        assertThat(describer.click(ClickSettings.enabled(new ClickRate(25)))).isEqualTo("25 clics/s");
        assertThat(describer.click(ClickSettings.disabled())).isEqualTo("Desactivado");
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
