package com.markes96.pcclicker.application.port.in;

import com.markes96.pcclicker.domain.configuration.SessionConfiguration;

/** Starts a new session: a short countdown to place the pointer, then clicking and/or moving. */
@FunctionalInterface
public interface StartSessionUseCase {

    /** @throws SessionAlreadyActiveException if another session has not finished yet */
    void start(SessionConfiguration configuration);
}
