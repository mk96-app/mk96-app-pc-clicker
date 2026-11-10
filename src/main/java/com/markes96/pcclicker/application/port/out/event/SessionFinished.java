package com.markes96.pcclicker.application.port.out.event;

import com.markes96.pcclicker.domain.session.FinishReason;
import com.markes96.pcclicker.domain.session.SessionSnapshot;
import java.util.Objects;

/** The session is over; {@code snapshot} holds its final figures. */
public record SessionFinished(SessionSnapshot snapshot, FinishReason reason) implements SessionEvent {

    public SessionFinished {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(reason, "reason");
    }
}
