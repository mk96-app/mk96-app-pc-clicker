package com.markes96.pcclicker.domain.movement;

import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.List;

/** Walks a fixed list of points over and over again. */
public final class CyclicPath implements MovementPath {

    private final List<ScreenPoint> points;
    private int index;

    public CyclicPath(List<ScreenPoint> points) {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("A cyclic path needs at least one point");
        }
        this.points = List.copyOf(points);
    }

    public static CyclicPath around(Shape shape) {
        return new CyclicPath(shape.outline());
    }

    @Override
    public ScreenPoint next() {
        final ScreenPoint point = points.get(index);
        index = (index + 1) % points.size();
        return point;
    }
}
