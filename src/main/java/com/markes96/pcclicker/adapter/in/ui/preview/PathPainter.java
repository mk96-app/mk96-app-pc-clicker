package com.markes96.pcclicker.adapter.in.ui.preview;

import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/** Canvas drawing primitives shared by the pattern thumbnails and the live preview. */
final class PathPainter {

    private PathPainter() {
    }

    /** Strokes the whole path in one colour. */
    static void stroke(GraphicsContext g, List<ScreenPoint> points, Color color, double width, boolean closed) {
        if (points.isEmpty()) {
            return;
        }
        prepare(g, width);
        g.setStroke(color);
        g.beginPath();
        g.moveTo(points.get(0).x(), points.get(0).y());
        for (int i = 1; i < points.size(); i++) {
            g.lineTo(points.get(i).x(), points.get(i).y());
        }
        if (closed) {
            g.closePath();
        }
        g.stroke();
    }

    /**
     * Strokes the {@code length} points that end at {@code head}, fading from transparent at the
     * tail to {@code color} at the head. Wraps around the path when {@code closed}.
     */
    static void trail(GraphicsContext g, List<ScreenPoint> points, int head, int length, Color color, double width,
                      boolean closed) {
        if (points.size() < 2) {
            return;
        }
        prepare(g, width);
        final int span = Math.min(length, closed ? points.size() - 1 : head);
        for (int i = span; i > 0; i--) {
            final ScreenPoint from = points.get(Math.floorMod(head - i, points.size()));
            final ScreenPoint to = points.get(Math.floorMod(head - i + 1, points.size()));
            final double strength = 1.0 - (double) i / (span + 1);
            g.setStroke(color.deriveColor(0, 1, 1, strength * strength));
            g.strokeLine(from.x(), from.y(), to.x(), to.y());
        }
    }

    /** Glowing dot, used for the pointer. */
    static void dot(GraphicsContext g, double x, double y, Color color, double radius) {
        g.setFill(color.deriveColor(0, 1, 1, 0.18));
        g.fillOval(x - radius * 2.6, y - radius * 2.6, radius * 5.2, radius * 5.2);
        g.setFill(color.deriveColor(0, 1, 1, 0.35));
        g.fillOval(x - radius * 1.6, y - radius * 1.6, radius * 3.2, radius * 3.2);
        g.setFill(color);
        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    /** Expanding ring of a click; {@code age} goes from 0 (just clicked) to 1 (faded out). */
    static void ripple(GraphicsContext g, double x, double y, double age, Color color) {
        final double radius = 4 + 22 * age;
        g.setLineWidth(2.0 * (1 - age) + 0.5);
        g.setStroke(color.deriveColor(0, 1, 1, (1 - age) * 0.9));
        g.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    /** Subtle dotted grid that gives the canvas a sense of space. */
    static void grid(GraphicsContext g, double width, double height, double spacing, Color color) {
        g.setFill(color);
        for (double y = spacing / 2; y < height; y += spacing) {
            for (double x = spacing / 2; x < width; x += spacing) {
                g.fillOval(x - 1, y - 1, 2, 2);
            }
        }
    }

    private static void prepare(GraphicsContext g, double width) {
        g.setLineWidth(width);
        g.setLineCap(StrokeLineCap.ROUND);
        g.setLineJoin(StrokeLineJoin.ROUND);
    }
}
