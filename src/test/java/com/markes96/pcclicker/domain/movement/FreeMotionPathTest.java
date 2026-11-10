package com.markes96.pcclicker.domain.movement;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.random.RandomGenerator;
import org.junit.jupiter.api.Test;

class FreeMotionPathTest {

    private static final ScreenArea AREA = new ScreenArea(0, 0, 100, 80);

    /** Steering that never changes the heading, so motion is fully predictable. */
    private static final SteeringPolicy STRAIGHT = new SteeringPolicy() {
        @Override
        public double onStep(double heading) {
            return heading;
        }

        @Override
        public double onBounce(double heading) {
            return heading;
        }
    };

    @Test
    void advances_in_a_straight_line_at_the_given_speed() {
        final FreeMotionPath path = new FreeMotionPath(AREA, new ScreenPoint(50, 40), 5, 0, STRAIGHT);

        assertThat(path.next()).isEqualTo(new ScreenPoint(55, 40));
        assertThat(path.next()).isEqualTo(new ScreenPoint(60, 40));
    }

    @Test
    void bounces_off_a_vertical_edge() {
        final FreeMotionPath path = new FreeMotionPath(AREA, new ScreenPoint(93, 40), 5, 0, STRAIGHT);

        assertThat(path.next()).isEqualTo(new ScreenPoint(98, 40));
        assertThat(path.next()).isEqualTo(new ScreenPoint(99, 40));
        assertThat(path.next()).isEqualTo(new ScreenPoint(94, 40));
    }

    @Test
    void bounces_off_a_horizontal_edge() {
        final FreeMotionPath path = new FreeMotionPath(AREA, new ScreenPoint(50, 3), 5, -Math.PI / 2, STRAIGHT);

        assertThat(path.next()).isEqualTo(new ScreenPoint(50, 0));
        assertThat(path.next()).isEqualTo(new ScreenPoint(50, 5));
    }

    @Test
    void starts_inside_the_area_even_if_the_origin_is_outside() {
        final FreeMotionPath path = new FreeMotionPath(AREA, new ScreenPoint(-40, 500), 5, 0, STRAIGHT);

        assertThat(AREA.contains(path.next())).isTrue();
    }

    @Test
    void asks_the_steering_policy_on_every_step_and_on_every_bounce() {
        final CountingSteering steering = new CountingSteering();
        final FreeMotionPath path = new FreeMotionPath(AREA, new ScreenPoint(97, 40), 5, 0, steering);

        path.next();
        path.next();

        assertThat(steering.steps).isEqualTo(2);
        assertThat(steering.bounces).isEqualTo(1);
    }

    @Test
    void wandering_stays_inside_the_area_forever() {
        final RandomGenerator random = new java.util.SplittableRandom(42);
        final FreeMotionPath path =
                new FreeMotionPath(AREA, new ScreenPoint(10, 10), 5, 1.0, new WanderSteering(random, Math.PI / 4));

        for (int step = 0; step < 20_000; step++) {
            assertThat(AREA.contains(path.next())).isTrue();
        }
    }

    @Test
    void rebounding_stays_inside_the_area_forever() {
        final RandomGenerator random = new java.util.SplittableRandom(7);
        final FreeMotionPath path =
                new FreeMotionPath(AREA, new ScreenPoint(10, 10), 5, 1.0, new ReboundSteering(random, Math.PI / 12));

        for (int step = 0; step < 20_000; step++) {
            assertThat(AREA.contains(path.next())).isTrue();
        }
    }

    private static final class CountingSteering implements SteeringPolicy {
        private int steps;
        private int bounces;

        @Override
        public double onStep(double heading) {
            steps++;
            return heading;
        }

        @Override
        public double onBounce(double heading) {
            bounces++;
            return heading;
        }
    }
}
