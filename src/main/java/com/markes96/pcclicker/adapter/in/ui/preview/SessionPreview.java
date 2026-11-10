package com.markes96.pcclicker.adapter.in.ui.preview;

import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.theme.Theme;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ConfigurationViewModel;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import javafx.animation.AnimationTimer;
import javafx.beans.Observable;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Live, animated rehearsal of the configured session: the pointer follows the real domain
 * trajectory at the real pace and every click leaves a ripple. It only animates while visible
 * and allowed to, so it costs nothing during a session.
 */
public final class SessionPreview extends StackPane {

    /** The session moves the pointer 50 times per second. */
    private static final double MOVES_PER_SECOND = 50;
    /** Ripples are drawn at most this often, so high click rates stay readable. */
    private static final double MAX_VISIBLE_CLICKS_PER_SECOND = 8;
    private static final double RIPPLE_SECONDS = 0.65;
    private static final int FREE_STEPS = 3_000;
    private static final int TRAIL_LENGTH = 70;
    private static final double NANOS_PER_SECOND = 1e9;

    private final ConfigurationViewModel configuration;
    private final Canvas canvas = new Canvas();
    private final Deque<Ripple> ripples = new ArrayDeque<>();
    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            tick(now);
        }
    };

    private List<ScreenPoint> trajectory = List.of();
    private boolean closedTrajectory;
    private boolean dirty = true;
    private double cursor;
    private double clickClock;
    private long lastFrame;

    public SessionPreview(ConfigurationViewModel configuration, String emptyText, ObservableBooleanValue animate) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
        getStyleClass().add("session-preview");

        final Pane canvasHolder = new Pane(canvas);
        canvas.widthProperty().bind(canvasHolder.widthProperty());
        canvas.heightProperty().bind(canvasHolder.heightProperty());
        canvas.widthProperty().addListener(observable -> dirty = true);
        canvas.heightProperty().addListener(observable -> dirty = true);

        final Label empty = Nodes.paragraph(emptyText, "preview-empty");
        Nodes.showWhen(empty, configuration.canStart().not());

        getChildren().addAll(canvasHolder, empty);

        // Change listeners (not invalidation listeners) so every edit is seen, even while not animating.
        for (Observable setting : configuration.settings()) {
            ((ObservableValue<?>) setting).addListener((observable, previous, current) -> dirty = true);
        }
        animate.addListener((observable, was, now) -> toggle(now));
        toggle(animate.get());
    }

    private void toggle(boolean animate) {
        if (animate) {
            lastFrame = 0;
            timer.start();
        } else {
            timer.stop();
        }
    }

    private void tick(long now) {
        final double seconds = lastFrame == 0 ? 0 : (now - lastFrame) / NANOS_PER_SECOND;
        lastFrame = now;
        if (canvas.getWidth() < 10 || canvas.getHeight() < 10) {
            return;
        }
        if (dirty) {
            rebuild();
        }
        advance(seconds);
        draw();
    }

    private void rebuild() {
        dirty = false;
        cursor = 0;
        ripples.clear();
        final MovementSettings movement = configuration.movementSettings();
        if (!movement.enabled()) {
            trajectory = List.of();
            return;
        }
        final ScreenArea area = new ScreenArea(12, 12, (int) canvas.getWidth() - 24, (int) canvas.getHeight() - 24);
        final int maxRadius = Math.max(1, Math.min(area.width(), area.height()) / 2 - 4);
        // Small radii stay visible and the largest one fills the canvas.
        final int displayRadius = Math.min(maxRadius, (int) Math.round(6 + 0.74 * movement.radius().pixels()));
        trajectory = PreviewTrajectory.sample(movement, area, displayRadius, FREE_STEPS);
        closedTrajectory = movement.pattern().usesRadius();
    }

    private void advance(double seconds) {
        if (!trajectory.isEmpty()) {
            cursor = (cursor + seconds * MOVES_PER_SECOND) % trajectory.size();
        }
        ripples.forEach(ripple -> ripple.age += seconds / RIPPLE_SECONDS);
        ripples.removeIf(ripple -> ripple.age >= 1);

        final ClickSettings click = configuration.clickSettings();
        if (click.enabled()) {
            final double interval = 1.0 / Math.min(click.rate().clicksPerSecond(), MAX_VISIBLE_CLICKS_PER_SECOND);
            clickClock += seconds;
            if (clickClock >= interval) {
                clickClock %= interval;
                ripples.add(new Ripple(pointer()));
            }
        }
    }

    private Point2D pointer() {
        if (trajectory.isEmpty()) {
            return new Point2D(canvas.getWidth() / 2, canvas.getHeight() / 2);
        }
        final ScreenPoint point = trajectory.get((int) cursor);
        return new Point2D(point.x(), point.y());
    }

    private void draw() {
        final GraphicsContext g = canvas.getGraphicsContext2D();
        final double width = canvas.getWidth();
        final double height = canvas.getHeight();
        g.clearRect(0, 0, width, height);
        PathPainter.grid(g, width, height, 18, Theme.GRID.deriveColor(0, 1, 1, 0.65));

        if (!configuration.canStart().get()) {
            return;
        }
        if (!trajectory.isEmpty()) {
            if (closedTrajectory) {
                PathPainter.stroke(g, trajectory, Theme.VIOLET.deriveColor(0, 1, 1, 0.22), 2, true);
            }
            PathPainter.trail(g, trajectory, (int) cursor, TRAIL_LENGTH, Theme.VIOLET_BRIGHT, 2.4, closedTrajectory);
        }
        for (Ripple ripple : ripples) {
            PathPainter.ripple(g, ripple.center.getX(), ripple.center.getY(), ripple.age, Theme.VIOLET_BRIGHT);
        }
        final Point2D pointer = pointer();
        PathPainter.dot(g, pointer.getX(), pointer.getY(), Theme.GREEN, 4.2);
    }

    /** A click being drawn; {@code age} grows from 0 to 1 while it fades out. */
    private static final class Ripple {
        private final Point2D center;
        private double age;

        private Ripple(Point2D center) {
            this.center = center;
        }
    }
}
