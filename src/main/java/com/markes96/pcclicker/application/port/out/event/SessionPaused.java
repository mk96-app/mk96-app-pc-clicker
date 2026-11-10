package com.markes96.pcclicker.application.port.out.event;

import com.markes96.pcclicker.domain.session.SessionSnapshot;
import java.util.Objects;

/** The user paused the session. */
public record SessionPaused(SessionSnapshot snapshot) implements SessionEvent {

    public SessionPaused {
        Objects.requireNonNull(snapshot, "snapshot");
    }
}
