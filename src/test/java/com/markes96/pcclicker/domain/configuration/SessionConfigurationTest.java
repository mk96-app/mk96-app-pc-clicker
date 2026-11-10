package com.markes96.pcclicker.domain.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SessionConfigurationTest {

    private static final ClickSettings CLICKING = ClickSettings.enabled(new ClickRate(10));
    private static final MovementSettings MOVING =
            MovementSettings.enabled(MovementPattern.CIRCLE, Radius.DEFAULT, VertexCount.DEFAULT);

    @Test
    void requires_at_least_one_enabled_action() {
        assertThatThrownBy(() -> new SessionConfiguration(
                        ClickSettings.disabled(), MovementSettings.disabled(), TimeLimit.unlimited()))
                .isInstanceOf(InvalidSessionConfigurationException.class)
                .hasMessageContaining("at least one");
    }

    @Test
    void can_only_click() {
        final SessionConfiguration configuration =
                new SessionConfiguration(CLICKING, MovementSettings.disabled(), TimeLimit.unlimited());

        assertThat(configuration.clicks()).isTrue();
        assertThat(configuration.moves()).isFalse();
    }

    @Test
    void can_only_move() {
        final SessionConfiguration configuration =
                new SessionConfiguration(ClickSettings.disabled(), MOVING, TimeLimit.ofMinutes(3));

        assertThat(configuration.clicks()).isFalse();
        assertThat(configuration.moves()).isTrue();
    }

    @Test
    void rejects_missing_parts() {
        assertThatNullPointerException().isThrownBy(() -> new SessionConfiguration(null, MOVING, TimeLimit.unlimited()));
        assertThatNullPointerException().isThrownBy(() -> new SessionConfiguration(CLICKING, null, TimeLimit.unlimited()));
        assertThatNullPointerException().isThrownBy(() -> new SessionConfiguration(CLICKING, MOVING, null));
    }

    @Test
    void disabled_settings_keep_sensible_defaults() {
        assertThat(ClickSettings.disabled().rate()).isEqualTo(ClickRate.DEFAULT);
        assertThat(MovementSettings.disabled().pattern()).isEqualTo(MovementPattern.RANDOM);
        assertThat(MovementSettings.disabled().radius()).isEqualTo(Radius.DEFAULT);
        assertThat(MovementSettings.disabled().vertices()).isEqualTo(VertexCount.DEFAULT);
    }
}
