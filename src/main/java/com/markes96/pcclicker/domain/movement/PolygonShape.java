package com.markes96.pcclicker.domain.movement;

import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Regular polygon inscribed in a circle around a center. It is oriented so it looks "upright":
 * squares have horizontal sides, odd polygons point up and the rest have a vertex on the right.
 */
public final class PolygonShape implements Shape {

    private static final double SQUARE_OFFSET = -Math.PI / 4;
    private static final double ODD_OFFSET = -Math.PI / 2;
    private static final double EVEN_OFFSET = 0.0;
    private static final int MIN_POINTS_PER_SIDE = 1;

    private final ScreenPoint center;
    private final VertexCount vertexCount;
    private final Radius radius;

    public PolygonShape(ScreenPoint center, VertexCount vertexCount, Radius radius) {
        this.center = Objects.requireNonNull(center, "center");
        this.vertexCount = Objects.requireNonNull(vertexCount, "vertexCount");
        this.radius = Objects.requireNonNull(radius, "radius");
    }

    public List<ScreenPoint> vertices() {
        final int count = vertexCount.value();
        final double step = Sampling.FULL_TURN / count;
        final double offset = orientationOffset(count);

        final List<ScreenPoint> vertices = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final double angle = step * i + offset;
            vertices.add(center.translate(
                    (int) Math.round(Math.cos(angle) * radius.pixels()),
                    (int) Math.round(Math.sin(angle) * radius.pixels())));
        }
        return List.copyOf(vertices);
    }

    @Override
    public List<ScreenPoint> outline() {
        final List<ScreenPoint> vertices = vertices();
        final List<ScreenPoint> points = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            addSide(points, vertices.get(i), vertices.get((i + 1) % vertices.size()));
        }
        return List.copyOf(points);
    }

    /** Adds the points of one side, including its start and excluding its end (the next side's start). */
    private static void addSide(List<ScreenPoint> points, ScreenPoint from, ScreenPoint to) {
        final int dx = to.x() - from.x();
        final int dy = to.y() - from.y();
        final int count = Sampling.pointsFor(Math.hypot(dx, dy), MIN_POINTS_PER_SIDE);
        for (int i = 0; i < count; i++) {
            final double fraction = (double) i / count;
            points.add(from.translate((int) Math.round(dx * fraction), (int) Math.round(dy * fraction)));
        }
    }

    private static double orientationOffset(int vertexCount) {
        if (vertexCount == 4) {
            return SQUARE_OFFSET;
        }
        return vertexCount % 2 == 0 ? EVEN_OFFSET : ODD_OFFSET;
    }
}
