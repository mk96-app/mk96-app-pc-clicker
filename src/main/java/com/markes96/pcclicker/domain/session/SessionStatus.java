package com.markes96.pcclicker.domain.session;

/** Lifecycle of a session: COUNTDOWN → RUNNING ⇄ PAUSED → FINISHED (FINISHED is reachable from any state). */
public enum SessionStatus {

    /** The user is placing the pointer before anything happens. */
    COUNTDOWN,
    /** Clicking and/or moving. */
    RUNNING,
    /** Temporarily idle; paused time does not count towards the limit. */
    PAUSED,
    /** Over for good. */
    FINISHED
}
