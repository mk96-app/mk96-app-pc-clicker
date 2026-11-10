package com.markes96.pcclicker.adapter.in.ui.theme;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/** Small, consistent motion vocabulary: everything enters with the same easing and timing. */
public final class Animations {

    public static final Duration FAST = Duration.millis(160);
    public static final Duration NORMAL = Duration.millis(240);
    public static final Interpolator EASE_OUT = Interpolator.SPLINE(0.2, 0.8, 0.2, 1.0);

    private Animations() {
    }

    /** Fades a node in while it slides up a few pixels. */
    public static void enter(Node node) {
        final FadeTransition fade = new FadeTransition(NORMAL, node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(EASE_OUT);
        final TranslateTransition slide = new TranslateTransition(NORMAL, node);
        slide.setFromY(8);
        slide.setToY(0);
        slide.setInterpolator(EASE_OUT);
        new ParallelTransition(fade, slide).play();
    }
}
