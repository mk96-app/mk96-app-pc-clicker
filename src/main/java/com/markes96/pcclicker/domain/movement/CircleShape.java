package com.markes96.pcclicker.domain.movement;

import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Circle around a center, starting on its right and turning clockwise on screen. */
public final class CircleShape implements Shape {

    private static final int MIN_POINTS = 3;

    private final ScreenPoint center;
    private final Radius radius;

    public CircleShape(ScreenPoint center, Radius radius) {
        this.center = Objects.requireNonNull(center, "center");
        this.radius = Objects.requireNonNull(radius, "radius");
    }

    @Override
    public List<ScreenPoint> outline() {
        final int r = radius.pixels();
        final int count = Sampling.pointsFor(Sampling.FULL_TURN * r, MIN_POINTS);
        final double step = Sampling.FULL_TURN / count;

        final List<ScreenPoint> points = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final double angle = step * i;
            points.add(center.translate(
                    (int) Math.round(Math.cos(angle) * r),
                    (int) Math.round(Math.sin(angle) * r)));
        }
        return List.copyOf(points);
    }
}
