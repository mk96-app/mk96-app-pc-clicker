package com.markes96.pcclicker.domain.movement;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Goes straight and deviates up to {@code maxDeviation} radians on every bounce, so the pointer
 * does not get trapped repeating the same trajectory.
 */
public final class ReboundSteering implements SteeringPolicy {

    private final RandomGenerator random;
    private final double maxDeviation;

    public ReboundSteering(RandomGenerator random, double maxDeviation) {
        this.random = Objects.requireNonNull(random, "random");
        this.maxDeviation = maxDeviation;
    }

    @Override
    public double onStep(double heading) {
        return heading;
    }

    @Override
    public double onBounce(double reflectedHeading) {
        return reflectedHeading + random.nextDouble(-maxDeviation, maxDeviation);
    }
}
