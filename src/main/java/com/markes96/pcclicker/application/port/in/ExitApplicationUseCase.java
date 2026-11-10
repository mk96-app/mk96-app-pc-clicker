package com.markes96.pcclicker.application.port.in;

/** Closes the application, stopping any active session first. */
@FunctionalInterface
public interface ExitApplicationUseCase {

    void exit();
}
