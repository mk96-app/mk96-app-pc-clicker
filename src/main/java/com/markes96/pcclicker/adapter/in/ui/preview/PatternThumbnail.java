package com.markes96.pcclicker.adapter.in.ui.preview;

import com.markes96.pcclicker.adapter.in.ui.theme.Theme;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Small static drawing of a movement pattern, used as the face of its selector tile. */
public final class PatternThumbnail extends Canvas {

    private static final int WIDTH = 92;
    private static final int HEIGHT = 54;
    private static final int THUMBNAIL_VERTICES = 5;
    private static final int FREE_STEPS = 220;

    public PatternThumbnail(MovementPattern pattern, Color color) {
        super(WIDTH, HEIGHT);
        getStyleClass().add("pattern-thumbnail");
        final MovementSettings settings =
                MovementSettings.enabled(pattern, Radius.DEFAULT, new VertexCount(THUMBNAIL_VERTICES));
        final ScreenArea area = new ScreenArea(2, 2, WIDTH - 4, HEIGHT - 4);
        final List<ScreenPoint> points = PreviewTrajectory.sample(settings, area, HEIGHT / 2 - 5, FREE_STEPS);

        final GraphicsContext g = getGraphicsContext2D();
        PathPainter.grid(g, WIDTH, HEIGHT, 9, Theme.GRID.deriveColor(0, 1, 1, 0.7));
        final boolean closed = pattern.usesRadius();
        PathPainter.stroke(g, points, color.deriveColor(0, 1, 1, closed ? 0.95 : 0.75), closed ? 2 : 1.4, closed);
        final ScreenPoint last = points.get(points.size() - 1);
        PathPainter.dot(g, closed ? points.get(0).x() : last.x(), closed ? points.get(0).y() : last.y(),
                Theme.GREEN, 2.4);
    }
}
