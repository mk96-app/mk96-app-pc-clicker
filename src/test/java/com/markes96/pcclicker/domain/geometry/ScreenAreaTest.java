package com.markes96.pcclicker.domain.geometry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScreenAreaTest {

    private final ScreenArea area = new ScreenArea(100, 50, 200, 100);

    @Test
    void exposes_its_inclusive_bounds() {
        assertThat(area.minX()).isEqualTo(100);
        assertThat(area.maxX()).isEqualTo(299);
        assertThat(area.minY()).isEqualTo(50);
        assertThat(area.maxY()).isEqualTo(149);
    }

    @Test
    void rejects_empty_areas() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ScreenArea(0, 0, 0, 10));
        assertThatIllegalArgumentException().isThrownBy(() -> new ScreenArea(0, 0, 10, -1));
    }

    @ParameterizedTest
    @CsvSource({"100,50,true", "299,149,true", "300,100,false", "99,100,false", "150,49,false", "150,150,false"})
    void knows_which_points_it_contains(int x, int y, boolean expected) {
        assertThat(area.contains(new ScreenPoint(x, y))).isEqualTo(expected);
    }

    @Test
    void finds_its_center() {
        assertThat(area.center()).isEqualTo(new ScreenPoint(200, 100));
    }

    @Test
    void translates_points() {
        assertThat(new ScreenPoint(3, 4).translate(-1, 6)).isEqualTo(new ScreenPoint(2, 10));
    }
}
