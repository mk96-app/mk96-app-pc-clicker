package com.markes96.pcclicker.adapter.in.ui.viewmodel;

/** What the user interface has to show about the session. */
public enum SessionPhase {
    /** No session: the configuration window is visible. */
    IDLE,
    COUNTDOWN,
    RUNNING,
    PAUSED
}
