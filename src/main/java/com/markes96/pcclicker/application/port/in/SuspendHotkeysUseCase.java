package com.markes96.pcclicker.application.port.in;

/**
 * Temporarily ignores every global hotkey, for example while the user is choosing a new key: the
 * key they press must not also start or stop a session.
 */
public interface SuspendHotkeysUseCase {

    void suspendHotkeys();

    void resumeHotkeys();
}
