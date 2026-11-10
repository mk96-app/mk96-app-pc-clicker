package com.markes96.pcclicker.bootstrap;

import javafx.application.Application;

/**
 * Process entry point. It is deliberately not a JavaFX {@link Application}, so the fat jar can run
 * with a plain {@code java -jar} without the JavaFX modules on the module path.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(PcClickerApplication.class, args);
        // The native keyboard hook may leave non-daemon threads behind; make sure the process ends.
        System.exit(0);
    }
}
