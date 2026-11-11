package com.markes96.pcclicker.domain.session;

/** Why a session ended. */
public enum FinishReason {
    STOPPED_BY_USER,
    TIME_LIMIT_REACHED,
    CLICK_LIMIT_REACHED,
    /** The system refused to move or click the pointer. */
    FAILED
}
