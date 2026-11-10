package com.markes96.pcclicker.domain.movement;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class SteeringPolicyTest {

    private static final double MAX = Math.PI / 4;

    @Test
    void wandering_turns_a_little_on_every_step() {
        final WanderSteering steering = new WanderSteering(new SplittableRandom(1), MAX);

        boolean turned = false;
        for (int i = 0; i < 1_000; i++) {
            final double next = steering.onStep(1.0);
            assertThat(next).isBetween(1.0 - MAX, 1.0 + MAX);
            turned |= next != 1.0;
        }
        assertThat(turned).isTrue();
    }

    @Test
    void wandering_does_not_alter_the_reflected_heading() {
        final WanderSteering steering = new WanderSteering(new SplittableRandom(1), MAX);

        assertThat(steering.onBounce(2.0)).isEqualTo(2.0);
    }

    @Test
    void rebounding_keeps_a_straight_line_between_bounces() {
        final ReboundSteering steering = new ReboundSteering(new SplittableRandom(1), MAX);

        assertThat(steering.onStep(0.7)).isEqualTo(0.7);
    }

    @Test
    void rebounding_adds_a_small_deviation_on_every_bounce() {
        final ReboundSteering steering = new ReboundSteering(new SplittableRandom(3), MAX);

        for (int i = 0; i < 1_000; i++) {
            assertThat(steering.onBounce(0.7)).isBetween(0.7 - MAX, 0.7 + MAX);
        }
    }
}
