package com.markes96.pcclicker.domain.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class StopwatchTest {

    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    private static Instant at(long seconds) {
        return T0.plusSeconds(seconds);
    }

    @Test
    void measures_nothing_before_being_started() {
        assertThat(new Stopwatch().elapsed(at(30))).isZero();
    }

    @Test
    void measures_the_time_since_it_was_started() {
        final Stopwatch stopwatch = new Stopwatch();
        stopwatch.start(T0);

        assertThat(stopwatch.elapsed(at(42))).isEqualTo(Duration.ofSeconds(42));
    }

    @Test
    void does_not_count_the_time_spent_paused() {
        final Stopwatch stopwatch = new Stopwatch();
        stopwatch.start(T0);
        stopwatch.pause(at(10));

        assertThat(stopwatch.elapsed(at(25))).isEqualTo(Duration.ofSeconds(10));

        stopwatch.resume(at(30));

        assertThat(stopwatch.elapsed(at(35))).isEqualTo(Duration.ofSeconds(15));
    }

    @Test
    void accumulates_several_pauses() {
        final Stopwatch stopwatch = new Stopwatch();
        stopwatch.start(T0);
        stopwatch.pause(at(5));
        stopwatch.resume(at(10));
        stopwatch.pause(at(20));
        stopwatch.resume(at(60));

        assertThat(stopwatch.elapsed(at(70))).isEqualTo(Duration.ofSeconds(25));
    }

    @Test
    void freezes_when_stopped() {
        final Stopwatch stopwatch = new Stopwatch();
        stopwatch.start(T0);
        stopwatch.stop(at(12));

        assertThat(stopwatch.elapsed(at(100))).isEqualTo(Duration.ofSeconds(12));
    }

    @Test
    void stopping_while_paused_keeps_the_paused_reading() {
        final Stopwatch stopwatch = new Stopwatch();
        stopwatch.start(T0);
        stopwatch.pause(at(8));
        stopwatch.stop(at(50));

        assertThat(stopwatch.elapsed(at(100))).isEqualTo(Duration.ofSeconds(8));
    }

    @Test
    void never_goes_backwards_if_the_clock_does() {
        final Stopwatch stopwatch = new Stopwatch();
        stopwatch.start(at(10));

        assertThat(stopwatch.elapsed(T0)).isZero();
    }

    @Test
    void rejects_operations_in_the_wrong_order() {
        final Stopwatch stopwatch = new Stopwatch();

        assertThatIllegalStateException().isThrownBy(() -> stopwatch.pause(T0));
        assertThatIllegalStateException().isThrownBy(() -> stopwatch.resume(T0));

        stopwatch.start(T0);

        assertThatIllegalStateException().isThrownBy(() -> stopwatch.start(T0));
        assertThatIllegalStateException().isThrownBy(() -> stopwatch.resume(T0));
    }
}
