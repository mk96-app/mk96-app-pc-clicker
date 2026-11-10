package com.markes96.pcclicker.application.port.out.event;

import com.markes96.pcclicker.domain.session.SessionSnapshot;
import java.util.Objects;

/** The countdown is over and the session is acting. */
public record SessionStarted(SessionSnapshot snapshot) implements SessionEvent {

    public SessionStarted {
        Objects.requireNonNull(snapshot, "snapshot");
    }
}
