package com.markes96.pcclicker.application.port.in;

/** Ends the active session, if any. Safe to call at any moment. */
@FunctionalInterface
public interface StopSessionUseCase {

    void stop();
}
