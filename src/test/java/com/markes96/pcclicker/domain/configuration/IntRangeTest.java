package com.markes96.pcclicker.domain.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IntRangeTest {

    private final IntRange range = new IntRange(2, 10);

    @Test
    void rejects_a_minimum_greater_than_the_maximum() {
        assertThatIllegalArgumentException().isThrownBy(() -> new IntRange(5, 4));
    }

    @ParameterizedTest
    @CsvSource({"1,false", "2,true", "6,true", "10,true", "11,false"})
    void knows_which_values_it_contains(int value, boolean expected) {
        assertThat(range.contains(value)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"-4,2", "2,2", "7,7", "10,10", "99,10"})
    void clamps_values_into_its_bounds(int value, int expected) {
        assertThat(range.clamp(value)).isEqualTo(expected);
    }

    @Test
    void requires_values_inside_its_bounds() {
        assertThat(range.require(4, "speed")).isEqualTo(4);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> range.require(11, "speed"))
                .withMessage("speed must be between 2 and 10 but was 11");
    }
}
