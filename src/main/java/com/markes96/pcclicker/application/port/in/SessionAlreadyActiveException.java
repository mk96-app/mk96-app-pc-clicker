package com.markes96.pcclicker.application.port.in;

/** Thrown when asked to start a session while another one is still active. */
public final class SessionAlreadyActiveException extends IllegalStateException {

    public SessionAlreadyActiveException() {
        super("A session is already active; stop it before starting a new one");
    }
}
