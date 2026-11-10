package com.markes96.pcclicker.application.port.out.event;

import com.markes96.pcclicker.domain.session.SessionSnapshot;
import java.util.Objects;

/** Periodic update while the session is running. */
public record SessionProgressed(SessionSnapshot snapshot) implements SessionEvent {

    public SessionProgressed {
        Objects.requireNonNull(snapshot, "snapshot");
    }
}
