package com.markes96.pcclicker.adapter.in.ui.component;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

/**
 * Makes a button repeat its step while it is held down, accelerating after a while, the way native
 * spinners behave. Keyboard activation still triggers a single step.
 */
final class PressAndHold {

    private static final Duration INITIAL_DELAY = Duration.millis(380);
    private static final Duration REPEAT_PERIOD = Duration.millis(55);
    private static final int REPEATS_BEFORE_FAST = 18;

    /** Receives the multiplier to apply: 1 at first, then larger once the button is held long enough. */
    @FunctionalInterface
    interface Step {
        void apply(int multiplier);
    }

    private final Timeline repeater = new Timeline();
    private int repeats;
    private boolean mouseStepped;

    private PressAndHold(ButtonBase button, Step step, int fastMultiplier) {
        repeater.getKeyFrames().add(new KeyFrame(REPEAT_PERIOD, event -> {
            repeats++;
            step.apply(repeats > REPEATS_BEFORE_FAST ? fastMultiplier : 1);
        }));
        repeater.setCycleCount(Timeline.INDEFINITE);
        repeater.setDelay(INITIAL_DELAY);

        button.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                step.apply(1);
                mouseStepped = true;
                repeats = 0;
                repeater.playFromStart();
            }
        });
        button.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            repeater.stop();
            if (!button.isHover()) {
                mouseStepped = false; // releasing outside does not fire the action
            }
        });
        button.disabledProperty().addListener((observable, wasDisabled, disabled) -> {
            if (disabled) {
                repeater.stop();
            }
        });
        button.setOnAction(event -> {
            if (mouseStepped) {
                mouseStepped = false; // already handled on press
            } else {
                step.apply(1);
            }
        });
    }

    static void install(ButtonBase button, Step step, int fastMultiplier) {
        new PressAndHold(button, step, fastMultiplier);
    }
}
