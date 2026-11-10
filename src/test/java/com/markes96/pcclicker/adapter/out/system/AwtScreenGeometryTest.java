package com.markes96.pcclicker.adapter.out.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.List;
import org.junit.jupiter.api.Test;

class AwtScreenGeometryTest {

    private static final ScreenArea PRIMARY = new ScreenArea(0, 0, 1920, 1080);
    private static final ScreenArea RIGHT = new ScreenArea(1920, 0, 2560, 1440);
    private static final ScreenArea LEFT = new ScreenArea(-1280, 100, 1280, 1024);

    private final AwtScreenGeometry geometry = new AwtScreenGeometry(() -> List.of(PRIMARY, RIGHT, LEFT));

    @Test
    void picks_the_monitor_under_the_point() {
        assertThat(geometry.areaContaining(new ScreenPoint(100, 100))).isEqualTo(PRIMARY);
        assertThat(geometry.areaContaining(new ScreenPoint(2000, 1300))).isEqualTo(RIGHT);
        assertThat(geometry.areaContaining(new ScreenPoint(-5, 500))).isEqualTo(LEFT);
    }

    @Test
    void falls_back_to_the_primary_monitor_for_points_outside_every_monitor() {
        assertThat(geometry.areaContaining(new ScreenPoint(-5, 0))).isEqualTo(PRIMARY);
        assertThat(geometry.areaContaining(new ScreenPoint(10_000, 10_000))).isEqualTo(PRIMARY);
    }
}
