package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import com.markes96.pcclicker.domain.session.FinishReason;
import java.time.Duration;
import java.util.Objects;

/** How a session ended, to tell the user once the main window is back. */
public record SessionOutcome(FinishReason reason, Duration activeTime, long clicks) {

    public SessionOutcome {
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(activeTime, "activeTime");
    }
}
