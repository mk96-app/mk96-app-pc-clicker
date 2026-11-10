package com.markes96.pcclicker.application.port.in;

/** Pauses a running session or resumes a paused one. Does nothing in any other situation. */
@FunctionalInterface
public interface TogglePauseUseCase {

    void togglePause();
}
