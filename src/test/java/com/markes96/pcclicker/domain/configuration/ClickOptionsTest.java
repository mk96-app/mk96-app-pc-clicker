package com.markes96.pcclicker.domain.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ClickOptionsTest {

    @Nested
    class RateVariationTest {

        @Test
        void goes_from_none_to_half_the_interval() {
            assertThat(RateVariation.RANGE).isEqualTo(new IntRange(0, 50));
            assertThat(RateVariation.NONE.isNone()).isTrue();
            assertThat(new RateVariation(20).fraction()).isEqualTo(0.2);
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 51})
        void rejects_values_out_of_range(int invalid) {
            assertThatIllegalArgumentException().isThrownBy(() -> new RateVariation(invalid));
        }
    }

    @Nested
    class ClickLimitTest {

        @Test
        void an_unlimited_session_never_reaches_it() {
            final ClickLimit limit = ClickLimit.unlimited();

            assertThat(limit.isLimited()).isFalse();
            assertThat(limit.clicks()).isEmpty();
            assertThat(limit.isReachedBy(Long.MAX_VALUE)).isFalse();
        }

        @Test
        void is_reached_once_the_clicks_are_done() {
            final ClickLimit limit = ClickLimit.ofClicks(100);

            assertThat(limit.clicks()).hasValue(100);
            assertThat(limit.isReachedBy(99)).isFalse();
            assertThat(limit.isReachedBy(100)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -5, 100_000_000})
        void rejects_values_out_of_range(int invalid) {
            assertThatIllegalArgumentException().isThrownBy(() -> ClickLimit.ofClicks(invalid));
        }

        @Test
        void compares_by_value() {
            assertThat(ClickLimit.ofClicks(5)).isEqualTo(ClickLimit.ofClicks(5)).isNotEqualTo(ClickLimit.unlimited());
        }
    }

    @Nested
    class ClickTypeTest {

        @Test
        void a_double_click_presses_the_button_twice() {
            assertThat(ClickType.SINGLE.presses()).isEqualTo(1);
            assertThat(ClickType.DOUBLE.presses()).isEqualTo(2);
        }
    }

    @Nested
    class ClickSettingsTest {

        @Test
        void enabling_clicks_with_the_left_button_steadily_and_without_limit() {
            final ClickSettings settings = ClickSettings.enabled(new ClickRate(10));

            assertThat(settings.enabled()).isTrue();
            assertThat(settings.button()).isEqualTo(MouseButton.LEFT);
            assertThat(settings.type()).isEqualTo(ClickType.SINGLE);
            assertThat(settings.variation()).isEqualTo(RateVariation.NONE);
            assertThat(settings.limit()).isEqualTo(ClickLimit.unlimited());
        }

        @Test
        void each_option_can_be_changed_independently() {
            final ClickSettings settings = ClickSettings.enabled(new ClickRate(10))
                    .withButton(MouseButton.RIGHT)
                    .withType(ClickType.DOUBLE)
                    .withVariation(new RateVariation(15))
                    .withLimit(ClickLimit.ofClicks(500));

            assertThat(settings).isEqualTo(new ClickSettings(true, new ClickRate(10), MouseButton.RIGHT,
                    ClickType.DOUBLE, new RateVariation(15), ClickLimit.ofClicks(500)));
        }
    }
}
