package com.markes96.pcclicker.adapter.in.ui.hud;

import com.markes96.pcclicker.adapter.in.ui.UiContext;
import com.markes96.pcclicker.adapter.in.ui.component.KeyCap;
import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.format.DurationFormat;
import com.markes96.pcclicker.adapter.in.ui.format.SettingsDescriber;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.theme.Animations;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import com.markes96.pcclicker.adapter.in.ui.theme.Theme;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.SessionPhase;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.SessionViewModel;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.control.Hotkey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

/**
 * Small always-on-top panel shown while a session is active: first the countdown to place the
 * pointer, then the live status. It sits in a corner, can be dragged anywhere and offers a Stop
 * button as a mouse alternative to the hotkeys.
 */
public final class SessionHud {

    private static final PseudoClass PAUSED = PseudoClass.getPseudoClass("paused");
    private static final double MARGIN = 20;
    private static final double RING_RADIUS = 46;
    private static final double RING_STROKE = 6;

    private final SessionViewModel session;
    private final UiContext context;
    private final Messages messages;
    private final SettingsDescriber describer;
    private final Stage stage = new Stage(StageStyle.TRANSPARENT);
    private final Arc ring = new Arc(0, 0, RING_RADIUS, RING_RADIUS, 90, 360);
    private final Label go;
    private final Label countdownNumber;
    private Timeline ringDrain;

    private double dragOffsetX;
    private double dragOffsetY;

    public SessionHud(UiContext context, SettingsDescriber describer) {
        this.context = Objects.requireNonNull(context, "context");
        this.session = context.session();
        this.messages = context.messages();
        this.describer = Objects.requireNonNull(describer, "describer");
        this.go = Nodes.label(messages.get("hud.go"), "hud-go");
        this.countdownNumber = Nodes.label("", "hud-countdown-number");

        final Node countdown = countdownView();
        final Node running = runningView();
        Nodes.showWhen(countdown, session.phase().isEqualTo(SessionPhase.COUNTDOWN));
        Nodes.showWhen(running, session.phase().isEqualTo(SessionPhase.RUNNING)
                .or(session.phase().isEqualTo(SessionPhase.PAUSED)));
        go.setOpacity(0);
        go.setMouseTransparent(true);

        final StackPane card = new StackPane(new VBox(countdown, running), go);
        card.getStyleClass().add("hud");
        final StackPane frame = new StackPane(card);
        frame.getStyleClass().add("hud-frame");

        final Scene scene = new Scene(frame);
        Theme.style(scene);
        stage.setScene(scene);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setTitle(context.info().name());
        stage.getIcons().add(Theme.logo());
        stage.setOnCloseRequest(event -> {
            event.consume(); // Alt+F4 on the panel stops the session instead of leaving it headless
            context.stopSession().stop();
        });
        makeDraggable(frame);

        session.phase().addListener((observable, previous, current) -> {
            if (previous == SessionPhase.COUNTDOWN && current == SessionPhase.RUNNING) {
                playGo();
            }
            stage.sizeToScene();
        });
        session.countdown().addListener((observable, previous, current) -> drainRing(current.intValue()));
    }

    public void show() {
        final Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMinX() + MARGIN);
        stage.setY(bounds.getMinY() + MARGIN);
        stage.sizeToScene();
        stage.show();
        drainRing(session.countdown().get());
    }

    public void hide() {
        stage.hide();
    }

    // --- Countdown ---

    private Node countdownView() {
        final Circle track = Nodes.styled(new Circle(RING_RADIUS), "hud-ring-track");
        ring.setType(ArcType.OPEN);
        ring.getStyleClass().add("hud-ring");
        countdownNumber.textProperty().bind(session.countdown().asString());
        // Track and ring sit on a fixed-size canvas at fixed coordinates. Any auto-centring container
        // (StackPane, Group) follows the arc's bounds, which change while it closes, and the ring
        // would drift and jitter on every frame.
        final double centre = RING_RADIUS + RING_STROKE;
        for (final Node shape : List.of(track, ring)) {
            shape.setLayoutX(centre);
            shape.setLayoutY(centre);
        }
        final Pane ringLayer = new Pane(track, ring);
        ringLayer.setMinSize(2 * centre, 2 * centre);
        ringLayer.setPrefSize(2 * centre, 2 * centre);
        ringLayer.setMaxSize(2 * centre, 2 * centre);
        final StackPane dial = new StackPane(ringLayer, countdownNumber);
        dial.getStyleClass().add("hud-dial");

        final VBox view = new VBox(12,
                header(Nodes.label(messages.get("hud.placePointer"), "hud-title")),
                Nodes.label(messages.get("hud.startsIn"), "hud-caption"),
                dial,
                hotkeyHints(Hotkey.EXIT, Hotkey.STOP));
        view.setAlignment(Pos.TOP_CENTER);
        view.getStyleClass().add("hud-view");
        return view;
    }

    /** Each second the ring closes once, so "3, 2, 1" reads as three full sweeps. */
    private void drainRing(int secondsLeft) {
        if (ringDrain != null) {
            ringDrain.stop();
        }
        if (secondsLeft <= 0) {
            ring.setLength(0);
            return;
        }
        ringDrain = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(ring.lengthProperty(), 360)),
                new KeyFrame(Duration.seconds(1), new KeyValue(ring.lengthProperty(), 0, Interpolator.EASE_BOTH)));
        ringDrain.play();
    }

    // --- Running / paused ---

    private Node runningView() {
        final Label status = Nodes.label("", "hud-status");
        status.setGraphic(Nodes.styled(new Region(), "hud-status-dot"));
        status.textProperty().bind(Bindings.when(session.phase().isEqualTo(SessionPhase.PAUSED))
                .then(messages.get("hud.paused"))
                .otherwise(messages.get("hud.running")));
        session.phase().addListener((observable, previous, current) ->
                status.pseudoClassStateChanged(PAUSED, current == SessionPhase.PAUSED));

        final Button stop = new Button(messages.get("hud.stop"), Icons.of(MaterialDesignS.STOP, 16));
        stop.getStyleClass().add("hud-stop");
        stop.setFocusTraversable(false);
        stop.setOnAction(event -> context.stopSession().stop());

        final Label timer = Nodes.label("", "hud-timer");
        timer.textProperty().bind(Bindings.createStringBinding(
                () -> DurationFormat.clock(session.elapsed().get()), session.elapsed()));
        timer.pseudoClassStateChanged(PAUSED, false);
        session.phase().addListener((observable, previous, current) ->
                timer.pseudoClassStateChanged(PAUSED, current == SessionPhase.PAUSED));

        final ProgressBar progress = new ProgressBar();
        progress.getStyleClass().add("hud-progress");
        progress.setMaxWidth(Double.MAX_VALUE);
        progress.progressProperty().bind(session.progress());
        Nodes.showWhen(progress, session.limited());

        final Label remaining = Nodes.label("", "hud-caption");
        remaining.textProperty().bind(Bindings.createStringBinding(() -> session.remaining().get() == null
                        ? messages.get("hud.unlimited")
                        : messages.format("hud.remaining", DurationFormat.clock(session.remaining().get())),
                session.remaining()));

        final Label clicks = Nodes.label("", "hud-clicks");
        clicks.textProperty().bind(Bindings.createStringBinding(() -> {
            final String done = messages.number(session.clicks().get());
            final SessionConfiguration configuration = session.configuration().get();
            return configuration == null || !configuration.click().limit().isLimited()
                    ? done
                    : done + " / " + messages.number(configuration.click().limit().clicks().getAsLong());
        }, session.clicks(), session.configuration()));
        clicks.setMinWidth(Label.USE_PREF_SIZE);
        final Label clicksCaption = Nodes.label(messages.get("hud.clicks"), "hud-caption");
        clicksCaption.setMinWidth(Label.USE_PREF_SIZE);
        final HBox clickStat = new HBox(6, clicks, clicksCaption);
        clickStat.setAlignment(Pos.BASELINE_LEFT);
        final Label configuration = Nodes.label("", "hud-caption", "hud-config");
        configuration.textProperty().bind(Bindings.createStringBinding(
                () -> describe(session.configuration().get()), session.configuration()));
        Nodes.showWhen(clickStat, Bindings.createBooleanBinding(
                () -> session.configuration().get() != null && session.configuration().get().clicks(),
                session.configuration()));

        final VBox view = new VBox(10,
                header(status, Nodes.spacer(), stop),
                timer,
                progress,
                remaining,
                clickStat,
                configuration,
                Nodes.styled(new Region(), "hud-divider"),
                hotkeyHints(Hotkey.EXIT, Hotkey.STOP, Hotkey.TOGGLE_PAUSE));
        view.getStyleClass().add("hud-view");
        return view;
    }

    private String describe(SessionConfiguration configuration) {
        if (configuration == null) {
            return "";
        }
        final List<String> parts = new ArrayList<>(2);
        if (configuration.clicks()) {
            // Only the rate: the click limit is already shown next to the counter.
            parts.add(messages.format("summary.clickRate", configuration.click().rate().clicksPerSecond()));
        }
        if (configuration.moves()) {
            parts.add(describer.patternName(configuration.movement().pattern()));
        }
        return SettingsDescriber.join(parts);
    }

    private void playGo() {
        go.setScaleX(0.6);
        go.setScaleY(0.6);
        final FadeTransition fadeIn = new FadeTransition(Animations.FAST, go);
        fadeIn.setToValue(1);
        final ScaleTransition grow = new ScaleTransition(Duration.millis(420), go);
        grow.setToX(1.15);
        grow.setToY(1.15);
        grow.setInterpolator(Animations.EASE_OUT);
        final FadeTransition fadeOut = new FadeTransition(Duration.millis(300), go);
        fadeOut.setToValue(0);
        new SequentialTransition(new ParallelTransition(fadeIn, grow), new PauseTransition(Duration.millis(250)), fadeOut)
                .play();
    }

    // --- Shared pieces ---

    private static HBox header(Node... children) {
        final ImageView logo = new ImageView(Theme.logo());
        logo.setFitWidth(18);
        logo.setFitHeight(18);
        logo.setClip(new Circle(9, 9, 9));
        final HBox header = new HBox(8, logo);
        header.getChildren().addAll(children);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("hud-header");
        return header;
    }

    private Node hotkeyHints(Hotkey... hotkeys) {
        final FlowPane hints = new FlowPane(12, 6);
        hints.getStyleClass().add("hud-hints");
        hints.setAlignment(Pos.CENTER_LEFT);
        for (Hotkey hotkey : hotkeys) {
            hints.getChildren().add(KeyCap.withCaption(context.preferences().keyOf(hotkey), describer.hotkeyName(hotkey)));
        }
        return hints;
    }

    private void makeDraggable(Node handle) {
        handle.setOnMousePressed(event -> {
            dragOffsetX = stage.getX() - event.getScreenX();
            dragOffsetY = stage.getY() - event.getScreenY();
        });
        handle.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + dragOffsetX);
            stage.setY(event.getScreenY() + dragOffsetY);
        });
    }
}
