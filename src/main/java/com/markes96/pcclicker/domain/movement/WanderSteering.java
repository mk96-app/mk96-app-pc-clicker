package com.markes96.pcclicker.domain.movement;

import java.util.Objects;
import java.util.random.RandomGenerator;

/** Turns a random amount, up to {@code maxTurn} radians either way, on every step. */
public final class WanderSteering implements SteeringPolicy {

    private final RandomGenerator random;
    private final double maxTurn;

    public WanderSteering(RandomGenerator random, double maxTurn) {
        this.random = Objects.requireNonNull(random, "random");
        this.maxTurn = maxTurn;
    }

    @Override
    public double onStep(double heading) {
        // The product of two uniforms favours small turns, which makes the walk look natural.
        return heading + maxTurn * random.nextDouble() * random.nextDouble(-1.0, 1.0);
    }

    @Override
    public double onBounce(double reflectedHeading) {
        return reflectedHeading;
    }
}
