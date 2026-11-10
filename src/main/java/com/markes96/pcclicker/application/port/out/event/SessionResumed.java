package com.markes96.pcclicker.application.port.out.event;

import com.markes96.pcclicker.domain.session.SessionSnapshot;
import java.util.Objects;

/** The user resumed the session. */
public record SessionResumed(SessionSnapshot snapshot) implements SessionEvent {

    public SessionResumed {
        Objects.requireNonNull(snapshot, "snapshot");
    }
}
