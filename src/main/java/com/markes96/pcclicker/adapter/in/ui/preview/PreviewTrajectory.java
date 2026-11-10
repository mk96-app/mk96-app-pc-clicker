package com.markes96.pcclicker.adapter.in.ui.preview;

import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import com.markes96.pcclicker.domain.movement.CircleShape;
import com.markes96.pcclicker.domain.movement.MovementPath;
import com.markes96.pcclicker.domain.movement.MovementPathFactory;
import com.markes96.pcclicker.domain.movement.PolygonShape;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Samples the very same domain trajectories the session uses, inside a small drawing area, so
 * previews always show the real behaviour of each pattern.
 */
public final class PreviewTrajectory {

    /** Fixed seed: previews must look the same every time they are redrawn. */
    private static final long SEED = 96L;

    private PreviewTrajectory() {
    }

    /**
     * @param settings      movement to preview; its own radius is ignored in favour of {@code displayRadius}
     * @param area          drawing area; shapes are centred in it and free patterns roam inside it
     * @param displayRadius radius, in drawing units, used for closed shapes
     * @param steps         number of positions for free patterns (closed shapes always return one lap)
     */
    public static List<ScreenPoint> sample(MovementSettings settings, ScreenArea area, int displayRadius, int steps) {
        final ScreenPoint center = area.center();
        final Radius radius = new Radius(Radius.RANGE.clamp(displayRadius));
        return switch (settings.pattern()) {
            case CIRCLE -> new CircleShape(center, radius).outline();
            case POLYGON -> new PolygonShape(center, settings.vertices(), radius).outline();
            case RANDOM, REBOUND -> walk(settings, area, center, steps);
        };
    }

    private static List<ScreenPoint> walk(MovementSettings settings, ScreenArea area, ScreenPoint start, int steps) {
        final MovementPath path = new MovementPathFactory(new SplittableRandom(SEED)).create(settings, start, area);
        final List<ScreenPoint> points = new ArrayList<>(steps);
        for (int i = 0; i < steps; i++) {
            points.add(path.next());
        }
        return List.copyOf(points);
    }
}
