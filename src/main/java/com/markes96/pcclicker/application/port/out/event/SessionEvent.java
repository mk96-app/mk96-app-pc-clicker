package com.markes96.pcclicker.application.port.out.event;

/** Something that happened to the session. Closed hierarchy so consumers can switch exhaustively. */
public sealed interface SessionEvent
        permits CountdownTicked, SessionStarted, SessionProgressed, SessionPaused, SessionResumed, SessionFinished {
}
