package com.markes96.pcclicker.adapter.in.ui.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DurationFormatTest {

    @ParameterizedTest
    @CsvSource({
            "0, 00:00:00",
            "59, 00:00:59",
            "61, 00:01:01",
            "3600, 01:00:00",
            "86399, 23:59:59",
            "360000, 100:00:00"})
    void formats_as_a_stopwatch(long seconds, String expected) {
        assertThat(DurationFormat.clock(Duration.ofSeconds(seconds))).isEqualTo(expected);
    }

    @Test
    void truncates_fractions_of_a_second() {
        assertThat(DurationFormat.clock(Duration.ofMillis(1_999))).isEqualTo("00:00:01");
    }

    @Test
    void never_shows_negative_times() {
        assertThat(DurationFormat.clock(Duration.ofSeconds(-5))).isEqualTo("00:00:00");
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0 s",
            "12, 12 s",
            "60, 1 min",
            "134, 2 min 14 s",
            "3600, 1 h",
            "3725, 1 h 2 min",
            "90061, 25 h 1 min"})
    void formats_for_humans(long seconds, String expected) {
        assertThat(DurationFormat.human(Duration.ofSeconds(seconds))).isEqualTo(expected);
    }
}
