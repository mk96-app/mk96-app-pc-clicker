package com.markes96.pcclicker.domain.timing;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.RateVariation;
import java.time.Duration;
import java.util.SplittableRandom;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

class ClickCadenceTest {

    private static final long TEN_PER_SECOND = Duration.ofMillis(100).toNanos();

    @Test
    void without_variation_every_wait_is_the_exact_interval() {
        final ClickCadence cadence = new ClickCadence(new ClickRate(10), RateVariation.NONE, new SplittableRandom(1));

        assertThat(cadence.isSteady()).isTrue();
        assertThat(LongStream.range(0, 50).mapToObj(i -> cadence.next())).containsOnly(Duration.ofMillis(100));
    }

    @Test
    void with_variation_every_wait_stays_within_the_allowed_deviation() {
        final ClickCadence cadence = new ClickCadence(new ClickRate(10), new RateVariation(20), new SplittableRandom(7));

        assertThat(cadence.isSteady()).isFalse();
        for (int i = 0; i < 10_000; i++) {
            assertThat(cadence.next().toNanos()).isBetween(80_000_000L, 120_000_000L);
        }
    }

    @Test
    void with_variation_the_waits_are_irregular_but_keep_the_average_rate() {
        final ClickCadence cadence = new ClickCadence(new ClickRate(10), new RateVariation(50), new SplittableRandom(3));

        final long[] waits = LongStream.range(0, 20_000).map(i -> cadence.next().toNanos()).toArray();

        assertThat(LongStream.of(waits).distinct().count()).isGreaterThan(1_000);
        assertThat(LongStream.of(waits).average().orElseThrow())
                .isCloseTo(TEN_PER_SECOND, org.assertj.core.data.Percentage.withPercentage(1));
    }

    @Test
    void the_wait_is_never_zero_even_at_the_fastest_rate() {
        final ClickCadence cadence = new ClickCadence(new ClickRate(1000), new RateVariation(50), new SplittableRandom(5));

        for (int i = 0; i < 10_000; i++) {
            assertThat(cadence.next()).isPositive();
        }
    }
}
