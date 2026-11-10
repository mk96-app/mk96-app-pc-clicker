package com.markes96.pcclicker.domain.movement;

import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** Builds the {@link MovementPath} described by some movement settings. */
public final class MovementPathFactory {

    /** Pixels travelled per step by the free patterns. */
    public static final double FREE_MOTION_SPEED = 5.0;

    private static final double WANDER_MAX_TURN = Math.PI / 4;
    private static final double REBOUND_MAX_DEVIATION = Math.PI / 12;

    private final RandomGenerator random;

    public MovementPathFactory(RandomGenerator random) {
        this.random = Objects.requireNonNull(random, "random");
    }

    /**
     * @param settings what to draw
     * @param origin   where the pointer is when the movement starts; shapes are centred on it
     * @param area     where free patterns are allowed to roam, usually the monitor under the pointer
     */
    public MovementPath create(MovementSettings settings, ScreenPoint origin, ScreenArea area) {
        return switch (settings.pattern()) {
            case CIRCLE -> CyclicPath.around(new CircleShape(origin, settings.radius()));
            case POLYGON -> CyclicPath.around(new PolygonShape(origin, settings.vertices(), settings.radius()));
            case RANDOM -> freeMotion(origin, area, new WanderSteering(random, WANDER_MAX_TURN));
            case REBOUND -> freeMotion(origin, area, new ReboundSteering(random, REBOUND_MAX_DEVIATION));
        };
    }

    private FreeMotionPath freeMotion(ScreenPoint origin, ScreenArea area, SteeringPolicy steering) {
        final double heading = random.nextDouble(Sampling.FULL_TURN);
        return new FreeMotionPath(area, origin, FREE_MOTION_SPEED, heading, steering);
    }
}
