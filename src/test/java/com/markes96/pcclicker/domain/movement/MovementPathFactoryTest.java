package com.markes96.pcclicker.domain.movement;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MovementPathFactoryTest {

    private static final ScreenArea SCREEN = new ScreenArea(0, 0, 1920, 1080);
    private static final ScreenPoint ORIGIN = new ScreenPoint(960, 540);

    private final MovementPathFactory factory = new MovementPathFactory(new SplittableRandom(5));

    private static MovementSettings settings(MovementPattern pattern) {
        return MovementSettings.enabled(pattern, new Radius(20), new VertexCount(5));
    }

    @Test
    void draws_a_circle_around_the_origin() {
        final MovementPath path = factory.create(settings(MovementPattern.CIRCLE), ORIGIN, SCREEN);

        assertThat(path.next()).isEqualTo(new ScreenPoint(980, 540));
    }

    @Test
    void draws_a_polygon_around_the_origin() {
        final MovementPath path = factory.create(settings(MovementPattern.POLYGON), ORIGIN, SCREEN);

        assertThat(path.next()).isEqualTo(new ScreenPoint(960, 520));
    }

    @ParameterizedTest
    @EnumSource(value = MovementPattern.class, names = {"RANDOM", "REBOUND"})
    void free_patterns_move_at_five_pixels_per_step_from_the_origin(MovementPattern pattern) {
        final MovementPath path = factory.create(settings(pattern), ORIGIN, SCREEN);

        final ScreenPoint first = path.next();

        assertThat(Math.hypot(first.x() - ORIGIN.x(), first.y() - ORIGIN.y())).isBetween(4.0, 6.0);
    }

    @ParameterizedTest
    @EnumSource(MovementPattern.class)
    void every_pattern_is_supported(MovementPattern pattern) {
        assertThat(factory.create(settings(pattern), ORIGIN, SCREEN).next()).isNotNull();
    }
}
