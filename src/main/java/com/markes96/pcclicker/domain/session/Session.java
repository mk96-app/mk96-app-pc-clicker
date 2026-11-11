package com.markes96.pcclicker.domain.session;

import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate root of an autoclicker run. Enforces the lifecycle described in {@link SessionStatus}
 * and keeps the active time and the click count.
 *
 * <p>Not thread-safe: callers serialise access.
 */
public final class Session {

    private final SessionConfiguration configuration;
    private final Stopwatch stopwatch = new Stopwatch();

    private SessionStatus status = SessionStatus.COUNTDOWN;
    private FinishReason finishReason;
    private long clicks;

    private Session(SessionConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "configuration");
    }

    /** Creates a session waiting for the user to place the pointer. */
    public static Session prepare(SessionConfiguration configuration) {
        return new Session(configuration);
    }

    public void start(Instant now) {
        requireStatus(SessionStatus.COUNTDOWN, "start");
        stopwatch.start(now);
        status = SessionStatus.RUNNING;
    }

    public void togglePause(Instant now) {
        switch (status) {
            case RUNNING -> {
                stopwatch.pause(now);
                status = SessionStatus.PAUSED;
            }
            case PAUSED -> {
                stopwatch.resume(now);
                status = SessionStatus.RUNNING;
            }
            default -> throw new IllegalStateException("Cannot pause or resume a session in " + status);
        }
    }

    public void finish(FinishReason reason, Instant now) {
        Objects.requireNonNull(reason, "reason");
        if (status == SessionStatus.FINISHED) {
            throw new IllegalStateException("The session has already finished");
        }
        stopwatch.stop(now);
        finishReason = reason;
        status = SessionStatus.FINISHED;
    }

    public void recordClick() {
        clicks++;
    }

    /** True once the session has performed as many clicks as its limit allows. */
    public boolean hasReachedClickLimit() {
        return configuration.clicks() && configuration.click().limit().isReachedBy(clicks);
    }

    public boolean hasReachedTimeLimit(Instant now) {
        return status == SessionStatus.RUNNING && configuration.timeLimit().isReachedBy(stopwatch.elapsed(now));
    }

    public boolean isActive() {
        return status != SessionStatus.FINISHED;
    }

    public boolean isRunning() {
        return status == SessionStatus.RUNNING;
    }

    public SessionStatus status() {
        return status;
    }

    public SessionConfiguration configuration() {
        return configuration;
    }

    public Optional<FinishReason> finishReason() {
        return Optional.ofNullable(finishReason);
    }

    public SessionSnapshot snapshot(Instant now) {
        return new SessionSnapshot(status, stopwatch.elapsed(now), clicks, configuration);
    }

    private void requireStatus(SessionStatus expected, String operation) {
        if (status != expected) {
            throw new IllegalStateException("Cannot " + operation + " a session in " + status);
        }
    }
}
