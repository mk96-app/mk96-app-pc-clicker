package com.markes96.pcclicker.domain.movement;

import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.Objects;

/**
 * Moves at constant speed inside an area, reflecting off its edges like a ball. How the heading
 * changes between bounces is delegated to a {@link SteeringPolicy}.
 *
 * <p>Position and heading are kept in floating point so rounding to pixels never biases the
 * direction of travel.
 */
public final class FreeMotionPath implements MovementPath {

    private final ScreenArea area;
    private final double speed;
    private final SteeringPolicy steering;

    private double x;
    private double y;
    private double heading;

    public FreeMotionPath(ScreenArea area, ScreenPoint start, double speed, double heading, SteeringPolicy steering) {
        this.area = Objects.requireNonNull(area, "area");
        this.steering = Objects.requireNonNull(steering, "steering");
        if (speed <= 0) {
            throw new IllegalArgumentException("Speed must be positive but was " + speed);
        }
        this.speed = speed;
        this.heading = heading;
        this.x = clamp(start.x(), area.minX(), area.maxX());
        this.y = clamp(start.y(), area.minY(), area.maxY());
    }

    @Override
    public ScreenPoint next() {
        x += Math.cos(heading) * speed;
        y += Math.sin(heading) * speed;

        boolean bounced = false;
        if (x < area.minX() || x > area.maxX()) {
            x = clamp(x, area.minX(), area.maxX());
            heading = Math.PI - heading;
            bounced = true;
        }
        if (y < area.minY() || y > area.maxY()) {
            y = clamp(y, area.minY(), area.maxY());
            heading = -heading;
            bounced = true;
        }
        if (bounced) {
            heading = steering.onBounce(heading);
        }
        heading = steering.onStep(heading);

        return new ScreenPoint((int) Math.round(x), (int) Math.round(y));
    }

    private static double clamp(double value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
