package com.markes96.pcclicker.application.port.in;

/** Read side of the session. */
@FunctionalInterface
public interface SessionQuery {

    /** True from the moment a session starts its countdown until it finishes. */
    boolean isSessionActive();
}
