package com.markes96.pcclicker.adapter.in.ui.preview;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PreviewTrajectoryTest {

    private static final ScreenArea CANVAS = new ScreenArea(0, 0, 300, 200);

    private static MovementSettings settings(MovementPattern pattern) {
        return MovementSettings.enabled(pattern, new Radius(40), new VertexCount(5));
    }

    @Test
    void closed_shapes_are_one_full_lap_around_the_center() {
        final List<ScreenPoint> circle = PreviewTrajectory.sample(settings(MovementPattern.CIRCLE), CANVAS, 40, 500);

        assertThat(circle).hasSize((int) Math.round(Math.PI * 40));
        assertThat(circle).allSatisfy(point ->
                assertThat(Math.hypot(point.x() - 150, point.y() - 100)).isBetween(39.0, 41.0));
    }

    @Test
    void polygons_use_the_configured_vertices() {
        final List<ScreenPoint> polygon = PreviewTrajectory.sample(settings(MovementPattern.POLYGON), CANVAS, 40, 500);

        assertThat(polygon).first().isEqualTo(new ScreenPoint(150, 60));
    }

    @ParameterizedTest
    @EnumSource(value = MovementPattern.class, names = {"RANDOM", "REBOUND"})
    void free_patterns_produce_the_requested_steps_inside_the_canvas(MovementPattern pattern) {
        final List<ScreenPoint> walk = PreviewTrajectory.sample(settings(pattern), CANVAS, 40, 500);

        assertThat(walk).hasSize(500).allSatisfy(point -> assertThat(CANVAS.contains(point)).isTrue());
    }

    @ParameterizedTest
    @EnumSource(MovementPattern.class)
    void previews_are_stable_so_they_do_not_flicker_between_redraws(MovementPattern pattern) {
        assertThat(PreviewTrajectory.sample(settings(pattern), CANVAS, 40, 300))
                .isEqualTo(PreviewTrajectory.sample(settings(pattern), CANVAS, 40, 300));
    }
}
