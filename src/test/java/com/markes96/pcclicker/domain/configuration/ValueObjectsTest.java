package com.markes96.pcclicker.domain.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Duration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValueObjectsTest {

    @Nested
    class ClickRateTest {

        @Test
        void accepts_from_one_to_one_thousand_clicks_per_second() {
            assertThat(ClickRate.RANGE).isEqualTo(new IntRange(1, 1000));
            assertThat(ClickRate.DEFAULT.clicksPerSecond()).isEqualTo(1);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 1001})
        void rejects_values_out_of_range(int invalid) {
            assertThatIllegalArgumentException().isThrownBy(() -> new ClickRate(invalid));
        }

        @Test
        void derives_the_interval_between_clicks() {
            assertThat(new ClickRate(1).interval()).isEqualTo(Duration.ofSeconds(1));
            assertThat(new ClickRate(4).interval()).isEqualTo(Duration.ofMillis(250));
            assertThat(new ClickRate(1000).interval()).isEqualTo(Duration.ofMillis(1));
            assertThat(new ClickRate(3).interval()).isEqualTo(Duration.ofNanos(333_333_333));
        }
    }

    @Nested
    class RadiusTest {

        @Test
        void accepts_from_one_to_one_hundred_pixels() {
            assertThat(Radius.RANGE).isEqualTo(new IntRange(1, 100));
            assertThat(Radius.DEFAULT.pixels()).isEqualTo(10);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 101})
        void rejects_values_out_of_range(int invalid) {
            assertThatIllegalArgumentException().isThrownBy(() -> new Radius(invalid));
        }
    }

    @Nested
    class VertexCountTest {

        @Test
        void accepts_from_two_to_three_hundred_vertices() {
            assertThat(VertexCount.RANGE).isEqualTo(new IntRange(2, 300));
            assertThat(VertexCount.DEFAULT.value()).isEqualTo(3);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 301})
        void rejects_values_out_of_range(int invalid) {
            assertThatIllegalArgumentException().isThrownBy(() -> new VertexCount(invalid));
        }
    }

    @Nested
    class TimeLimitTest {

        @Test
        void can_be_unlimited() {
            final TimeLimit limit = TimeLimit.unlimited();

            assertThat(limit.isLimited()).isFalse();
            assertThat(limit.duration()).isEmpty();
            assertThat(limit.isReachedBy(Duration.ofDays(365))).isFalse();
        }

        @Test
        void can_be_expressed_in_minutes() {
            final TimeLimit limit = TimeLimit.ofMinutes(2);

            assertThat(limit.isLimited()).isTrue();
            assertThat(limit.duration()).contains(Duration.ofMinutes(2));
        }

        @Test
        void is_reached_once_the_elapsed_time_equals_the_limit() {
            final TimeLimit limit = TimeLimit.ofMinutes(1);

            assertThat(limit.isReachedBy(Duration.ofSeconds(59))).isFalse();
            assertThat(limit.isReachedBy(Duration.ofSeconds(60))).isTrue();
            assertThat(limit.isReachedBy(Duration.ofSeconds(61))).isTrue();
        }

        @Test
        void computes_the_remaining_time_without_going_negative() {
            final TimeLimit limit = TimeLimit.ofMinutes(1);

            assertThat(limit.remainingAfter(Duration.ofSeconds(15))).contains(Duration.ofSeconds(45));
            assertThat(limit.remainingAfter(Duration.ofSeconds(90))).contains(Duration.ZERO);
            assertThat(TimeLimit.unlimited().remainingAfter(Duration.ofSeconds(15))).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -3, 100_000_000})
        void rejects_minutes_out_of_range(int invalid) {
            assertThatIllegalArgumentException().isThrownBy(() -> TimeLimit.ofMinutes(invalid));
        }

        @Test
        void has_value_semantics() {
            assertThat(TimeLimit.ofMinutes(5)).isEqualTo(TimeLimit.ofMinutes(5)).hasSameHashCodeAs(TimeLimit.ofMinutes(5));
            assertThat(TimeLimit.unlimited()).isEqualTo(TimeLimit.unlimited()).isNotEqualTo(TimeLimit.ofMinutes(5));
        }
    }

    @Nested
    class MovementPatternTest {

        @Test
        void tells_which_parameters_each_pattern_needs() {
            assertThat(MovementPattern.RANDOM.usesRadius()).isFalse();
            assertThat(MovementPattern.RANDOM.usesVertices()).isFalse();
            assertThat(MovementPattern.REBOUND.usesRadius()).isFalse();
            assertThat(MovementPattern.REBOUND.usesVertices()).isFalse();
            assertThat(MovementPattern.CIRCLE.usesRadius()).isTrue();
            assertThat(MovementPattern.CIRCLE.usesVertices()).isFalse();
            assertThat(MovementPattern.POLYGON.usesRadius()).isTrue();
            assertThat(MovementPattern.POLYGON.usesVertices()).isTrue();
        }
    }
}
