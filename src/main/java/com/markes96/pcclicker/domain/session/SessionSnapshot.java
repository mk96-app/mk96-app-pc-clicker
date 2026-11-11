package com.markes96.pcclicker.domain.session;

import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalLong;

/** Immutable picture of a session at a given instant, safe to hand over to other threads. */
public record SessionSnapshot(SessionStatus status, Duration elapsed, long clicks, SessionConfiguration configuration) {

    public SessionSnapshot {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(elapsed, "elapsed");
        Objects.requireNonNull(configuration, "configuration");
    }

    /** Time left until the limit; empty when the session is unlimited. */
    public Optional<Duration> remaining() {
        return configuration.timeLimit().remainingAfter(elapsed);
    }

    /** Clicks left until the click limit; empty when clicking without limit or not clicking. */
    public OptionalLong remainingClicks() {
        if (!configuration.clicks()) {
            return OptionalLong.empty();
        }
        final OptionalLong limit = configuration.click().limit().clicks();
        return limit.isPresent() ? OptionalLong.of(Math.max(0, limit.getAsLong() - clicks)) : OptionalLong.empty();
    }

    /** Fraction of the time limit already used, from 0 to 1; empty when the session is unlimited. */
    public OptionalDouble progress() {
        return configuration.timeLimit().duration()
                .map(limit -> OptionalDouble.of(Math.min(1.0, (double) elapsed.toMillis() / limit.toMillis())))
                .orElse(OptionalDouble.empty());
    }
}
