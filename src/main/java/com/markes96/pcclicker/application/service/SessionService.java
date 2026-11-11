package com.markes96.pcclicker.application.service;

import com.markes96.pcclicker.application.port.in.SessionAlreadyActiveException;
import com.markes96.pcclicker.application.port.in.SessionQuery;
import com.markes96.pcclicker.application.port.in.StartSessionUseCase;
import com.markes96.pcclicker.application.port.in.StopSessionUseCase;
import com.markes96.pcclicker.application.port.in.TogglePauseUseCase;
import com.markes96.pcclicker.application.port.out.MouseDriver;
import com.markes96.pcclicker.application.port.out.ScheduledTask;
import com.markes96.pcclicker.application.port.out.ScreenGeometry;
import com.markes96.pcclicker.application.port.out.SessionEventPublisher;
import com.markes96.pcclicker.application.port.out.TaskScheduler;
import com.markes96.pcclicker.application.port.out.event.CountdownTicked;
import com.markes96.pcclicker.application.port.out.event.SessionFinished;
import com.markes96.pcclicker.application.port.out.event.SessionPaused;
import com.markes96.pcclicker.application.port.out.event.SessionProgressed;
import com.markes96.pcclicker.application.port.out.event.SessionResumed;
import com.markes96.pcclicker.application.port.out.event.SessionStarted;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import com.markes96.pcclicker.domain.movement.MovementPath;
import com.markes96.pcclicker.domain.movement.MovementPathFactory;
import com.markes96.pcclicker.domain.session.FinishReason;
import com.markes96.pcclicker.domain.session.Session;
import com.markes96.pcclicker.domain.session.SessionStatus;
import com.markes96.pcclicker.domain.settings.CountdownSeconds;
import com.markes96.pcclicker.domain.timing.ClickCadence;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.Duration;
import java.time.InstantSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * Orchestrates the life of a {@link Session}: countdown, periodic clicks and moves, pause, time
 * and click limits, and termination.
 *
 * <p>Threading: commands arrive from the UI thread, the hotkey thread and the scheduler threads.
 * Every access to the session goes through a single lock, and events are published while holding
 * it, so they are always delivered in a consistent order. Each scheduled task is bound to the
 * session that created it and becomes a no-op once that session is over, even if it was already
 * waiting for the lock.
 */
public final class SessionService implements StartSessionUseCase, StopSessionUseCase, TogglePauseUseCase, SessionQuery {

    private static final Logger LOG = System.getLogger(SessionService.class.getName());

    private final MouseDriver mouse;
    private final ScreenGeometry screens;
    private final TaskScheduler scheduler;
    private final InstantSource clock;
    private final SessionEventPublisher events;
    private final MovementPathFactory paths;
    private final RandomGenerator random;
    private final SessionTimings timings;
    private final Supplier<CountdownSeconds> countdown;

    private final ReentrantLock lock = new ReentrantLock();
    private ActiveRun run;

    /**
     * @param random    source of the irregular pace of clicks with variation
     * @param countdown read at every start, so a change of preference applies to the next session
     */
    public SessionService(MouseDriver mouse, ScreenGeometry screens, TaskScheduler scheduler, InstantSource clock,
                          SessionEventPublisher events, MovementPathFactory paths, RandomGenerator random,
                          SessionTimings timings, Supplier<CountdownSeconds> countdown) {
        this.mouse = Objects.requireNonNull(mouse, "mouse");
        this.screens = Objects.requireNonNull(screens, "screens");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.events = Objects.requireNonNull(events, "events");
        this.paths = Objects.requireNonNull(paths, "paths");
        this.random = Objects.requireNonNull(random, "random");
        this.timings = Objects.requireNonNull(timings, "timings");
        this.countdown = Objects.requireNonNull(countdown, "countdown");
    }

    @Override
    public void start(SessionConfiguration configuration) {
        Objects.requireNonNull(configuration, "configuration");
        lock.lock();
        try {
            if (run != null) {
                throw new SessionAlreadyActiveException();
            }
            final CountdownSeconds seconds = Objects.requireNonNull(countdown.get(), "countdown seconds");
            final ActiveRun newRun = new ActiveRun(Session.prepare(configuration), seconds.value());
            run = newRun;
            if (seconds.isImmediate()) {
                boundTo(newRun, this::beginRunning).run();
                return;
            }
            events.publish(new CountdownTicked(newRun.countdownLeft));
            newRun.track(scheduler.scheduleAtFixedRate(
                    boundTo(newRun, this::countdownTick), timings.countdownStep(), timings.countdownStep()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void stop() {
        lock.lock();
        try {
            if (run != null) {
                finish(FinishReason.STOPPED_BY_USER);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void togglePause() {
        lock.lock();
        try {
            if (run == null) {
                return;
            }
            final Session session = run.session;
            final SessionStatus status = session.status();
            if (status != SessionStatus.RUNNING && status != SessionStatus.PAUSED) {
                return;
            }
            session.togglePause(clock.instant());
            final var snapshot = session.snapshot(clock.instant());
            events.publish(session.isRunning() ? new SessionResumed(snapshot) : new SessionPaused(snapshot));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isSessionActive() {
        lock.lock();
        try {
            return run != null;
        } finally {
            lock.unlock();
        }
    }

    // --- Scheduled work. All the methods below run with the lock held. ---

    private void countdownTick() {
        run.countdownLeft--;
        if (run.countdownLeft > 0) {
            events.publish(new CountdownTicked(run.countdownLeft));
            return;
        }
        run.cancelTasks();
        beginRunning();
    }

    private void beginRunning() {
        final ActiveRun current = run;
        final Session session = current.session;
        final SessionConfiguration configuration = session.configuration();
        session.start(clock.instant());

        if (configuration.moves()) {
            final ScreenPoint origin = mouse.position();
            final MovementPath path = paths.create(configuration.movement(), origin, screens.areaContaining(origin));
            current.track(scheduler.scheduleAtFixedRate(
                    boundTo(current, () -> move(path)), Duration.ZERO, timings.movementInterval()));
        }
        if (configuration.clicks()) {
            final ClickSettings click = configuration.click();
            final ClickCadence cadence = new ClickCadence(click.rate(), click.variation(), random);
            if (cadence.isSteady()) {
                current.track(scheduler.scheduleAtFixedRate(boundTo(current, this::click), Duration.ZERO, cadence.interval()));
            } else {
                scheduleNextClick(current, cadence, Duration.ZERO);
            }
        }
        current.track(scheduler.scheduleAtFixedRate(
                boundTo(current, this::reportProgress), timings.progressInterval(), timings.progressInterval()));

        events.publish(new SessionStarted(session.snapshot(clock.instant())));
    }

    private void move(MovementPath path) {
        if (run.session.isRunning()) {
            mouse.moveTo(path.next());
        }
    }

    private void click() {
        final Session session = run.session;
        if (!session.isRunning()) {
            return;
        }
        final ClickSettings settings = session.configuration().click();
        mouse.click(settings.button(), settings.type());
        session.recordClick();
        if (session.hasReachedClickLimit()) {
            finish(FinishReason.CLICK_LIMIT_REACHED);
        }
    }

    /**
     * Irregular pace: every click schedules the next one after a fresh wait. Only one click task
     * exists at a time, so a long session does not pile up handles.
     */
    private void scheduleNextClick(ActiveRun owner, ClickCadence cadence, Duration delay) {
        owner.replaceClickTask(scheduler.schedule(boundTo(owner, () -> {
            click();
            if (run == owner) {
                scheduleNextClick(owner, cadence, cadence.next());
            }
        }), delay));
    }

    private void reportProgress() {
        final Session session = run.session;
        if (!session.isRunning()) {
            return;
        }
        if (session.hasReachedTimeLimit(clock.instant())) {
            finish(FinishReason.TIME_LIMIT_REACHED);
        } else {
            events.publish(new SessionProgressed(session.snapshot(clock.instant())));
        }
    }

    private void finish(FinishReason reason) {
        final ActiveRun finished = run;
        run = null;
        finished.cancelTasks();
        finished.session.finish(reason, clock.instant());
        events.publish(new SessionFinished(finished.session.snapshot(clock.instant()), reason));
    }

    /**
     * Wraps scheduled work so it runs under the lock, only while {@code owner} is still the active
     * run, and turns any failure of the system into a failed session instead of a dead thread.
     */
    private Runnable boundTo(ActiveRun owner, Runnable work) {
        return () -> {
            lock.lock();
            try {
                if (run != owner) {
                    return;
                }
                work.run();
            } catch (RuntimeException failure) {
                LOG.log(Level.ERROR, "Session failed; stopping it", failure);
                if (run == owner) {
                    finish(FinishReason.FAILED);
                }
            } finally {
                lock.unlock();
            }
        };
    }

    /** Mutable bookkeeping of the session currently in progress. Guarded by the service lock. */
    private static final class ActiveRun {
        private final Session session;
        private final List<ScheduledTask> tasks = new ArrayList<>();
        private ScheduledTask clickTask;
        private int countdownLeft;

        private ActiveRun(Session session, int countdownLeft) {
            this.session = session;
            this.countdownLeft = countdownLeft;
        }

        private void track(ScheduledTask task) {
            tasks.add(task);
        }

        private void replaceClickTask(ScheduledTask task) {
            clickTask = task;
        }

        private void cancelTasks() {
            tasks.forEach(ScheduledTask::cancel);
            tasks.clear();
            if (clickTask != null) {
                clickTask.cancel();
                clickTask = null;
            }
        }
    }
}
