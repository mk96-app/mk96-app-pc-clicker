package com.markes96.pcclicker.domain.movement;

/** Shared rules about how densely figures are sampled into pointer positions. */
final class Sampling {

    /** One position every two pixels of length gives a smooth movement at 50 steps per second. */
    static final double POINTS_PER_PIXEL = 0.5;

    static final double FULL_TURN = 2 * Math.PI;

    private Sampling() {
    }

    static int pointsFor(double length, int minimum) {
        return Math.max(minimum, (int) Math.round(length * POINTS_PER_PIXEL));
    }
}
