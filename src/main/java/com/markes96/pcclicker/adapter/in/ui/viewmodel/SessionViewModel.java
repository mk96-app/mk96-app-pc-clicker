package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import com.markes96.pcclicker.application.port.out.event.CountdownTicked;
import com.markes96.pcclicker.application.port.out.event.SessionEvent;
import com.markes96.pcclicker.application.port.out.event.SessionFinished;
import com.markes96.pcclicker.application.port.out.event.SessionPaused;
import com.markes96.pcclicker.application.port.out.event.SessionProgressed;
import com.markes96.pcclicker.application.port.out.event.SessionResumed;
import com.markes96.pcclicker.application.port.out.event.SessionStarted;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.session.SessionSnapshot;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * Observable state of the active session, fed with application events. Must only be used from the
 * UI thread; {@link SessionEventPresenter} takes care of that.
 */
public final class SessionViewModel {

    private final ReadOnlyObjectWrapper<SessionPhase> phase = new ReadOnlyObjectWrapper<>(this, "phase", SessionPhase.IDLE);
    private final ReadOnlyIntegerWrapper countdown = new ReadOnlyIntegerWrapper(this, "countdown");
    private final ReadOnlyObjectWrapper<Duration> elapsed = new ReadOnlyObjectWrapper<>(this, "elapsed", Duration.ZERO);
    private final ReadOnlyObjectWrapper<Duration> remaining = new ReadOnlyObjectWrapper<>(this, "remaining");
    private final ReadOnlyDoubleWrapper progress = new ReadOnlyDoubleWrapper(this, "progress");
    private final ReadOnlyLongWrapper clicks = new ReadOnlyLongWrapper(this, "clicks");
    private final ReadOnlyBooleanWrapper limited = new ReadOnlyBooleanWrapper(this, "limited");
    private final ReadOnlyObjectWrapper<SessionConfiguration> configuration =
            new ReadOnlyObjectWrapper<>(this, "configuration");
    private final BooleanBinding active = phase.isNotEqualTo(SessionPhase.IDLE);

    private final List<Consumer<SessionOutcome>> finishListeners = new ArrayList<>();

    public void apply(SessionEvent event) {
        switch (event) {
            case CountdownTicked ticked -> {
                if (phase.get() == SessionPhase.IDLE) {
                    reset();
                }
                countdown.set(ticked.secondsLeft());
                phase.set(SessionPhase.COUNTDOWN);
            }
            case SessionStarted started -> show(started.snapshot(), SessionPhase.RUNNING);
            case SessionProgressed progressed -> show(progressed.snapshot(), SessionPhase.RUNNING);
            case SessionPaused paused -> show(paused.snapshot(), SessionPhase.PAUSED);
            case SessionResumed resumed -> show(resumed.snapshot(), SessionPhase.RUNNING);
            case SessionFinished finished -> {
                show(finished.snapshot(), SessionPhase.IDLE);
                final SessionOutcome outcome = new SessionOutcome(
                        finished.reason(), finished.snapshot().elapsed(), finished.snapshot().clicks());
                finishListeners.forEach(listener -> listener.accept(outcome));
            }
        }
    }

    /** Called on the UI thread every time a session ends, whatever the reason. */
    public void onFinished(Consumer<SessionOutcome> listener) {
        finishListeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private void show(SessionSnapshot snapshot, SessionPhase newPhase) {
        configuration.set(snapshot.configuration());
        elapsed.set(snapshot.elapsed());
        clicks.set(snapshot.clicks());
        limited.set(snapshot.configuration().timeLimit().isLimited());
        remaining.set(snapshot.remaining().orElse(null));
        progress.set(snapshot.progress().orElse(0));
        phase.set(newPhase);
    }

    private void reset() {
        elapsed.set(Duration.ZERO);
        remaining.set(null);
        progress.set(0);
        clicks.set(0);
    }

    public ReadOnlyObjectProperty<SessionPhase> phase() {
        return phase.getReadOnlyProperty();
    }

    /** True from the countdown until the session finishes. */
    public BooleanBinding active() {
        return active;
    }

    public ReadOnlyIntegerProperty countdown() {
        return countdown.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<Duration> elapsed() {
        return elapsed.getReadOnlyProperty();
    }

    /** Time left until the limit, or {@code null} for unlimited sessions. */
    public ReadOnlyObjectProperty<Duration> remaining() {
        return remaining.getReadOnlyProperty();
    }

    /** Fraction of the time limit used, from 0 to 1; always 0 for unlimited sessions. */
    public ReadOnlyDoubleProperty progress() {
        return progress.getReadOnlyProperty();
    }

    public ReadOnlyLongProperty clicks() {
        return clicks.getReadOnlyProperty();
    }

    public ReadOnlyBooleanProperty limited() {
        return limited.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<SessionConfiguration> configuration() {
        return configuration.getReadOnlyProperty();
    }
}
