package com.markes96.pcclicker.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class SessionTimingsTest {

    @Test
    void defaults_match_the_expected_behaviour() {
        final SessionTimings defaults = SessionTimings.defaults();

        assertThat(defaults.countdownStep()).isEqualTo(Duration.ofSeconds(1));
        assertThat(defaults.movementInterval()).isEqualTo(Duration.ofMillis(20));
        assertThat(defaults.progressInterval()).isEqualTo(Duration.ofMillis(200));
    }

    @Test
    void rejects_non_positive_values() {
        final Duration ok = Duration.ofMillis(10);

        assertThatIllegalArgumentException().isThrownBy(() -> new SessionTimings(Duration.ZERO, ok, ok));
        assertThatIllegalArgumentException().isThrownBy(() -> new SessionTimings(ok, Duration.ofMillis(-1), ok));
        assertThatIllegalArgumentException().isThrownBy(() -> new SessionTimings(ok, ok, Duration.ZERO));
    }
}
