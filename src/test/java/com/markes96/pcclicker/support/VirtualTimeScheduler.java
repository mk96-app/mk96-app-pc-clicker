package com.markes96.pcclicker.support;

import com.markes96.pcclicker.application.port.out.ScheduledTask;
import com.markes96.pcclicker.application.port.out.TaskScheduler;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Deterministic scheduler driven by virtual time. Tasks only run when the test calls
 * {@link #advance(Duration)}, in due-time order, exactly as a real scheduler would run them.
 * It is also the {@link InstantSource} of the code under test, so clock and scheduler always agree.
 */
public final class VirtualTimeScheduler implements TaskScheduler, InstantSource {

    private final List<Entry> entries = new ArrayList<>();
    private Instant now = Instant.parse("2026-01-01T09:00:00Z");
    private long sequence;

    @Override
    public Instant instant() {
        return now;
    }

    @Override
    public ScheduledTask schedule(Runnable task, Duration delay) {
        return add(task, delay, null);
    }

    @Override
    public ScheduledTask scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period) {
        return add(task, initialDelay, period);
    }

    public void advance(Duration amount) {
        final Instant target = now.plus(amount);
        Optional<Entry> next;
        while ((next = nextDueBy(target)).isPresent()) {
            final Entry entry = next.get();
            now = entry.due;
            if (entry.period == null) {
                entries.remove(entry);
            } else {
                entry.due = entry.due.plus(entry.period);
            }
            entry.task.run();
        }
        now = target;
    }

    public long activeTasks() {
        return entries.stream().filter(entry -> !entry.cancelled).count();
    }

    private Optional<Entry> nextDueBy(Instant target) {
        entries.removeIf(entry -> entry.cancelled);
        return entries.stream()
                .filter(entry -> !entry.due.isAfter(target))
                .min(Comparator.comparing((Entry entry) -> entry.due).thenComparingLong(entry -> entry.order));
    }

    private ScheduledTask add(Runnable task, Duration delay, Duration period) {
        final Entry entry = new Entry(task, now.plus(delay), period, sequence++);
        entries.add(entry);
        return () -> entry.cancelled = true;
    }

    private static final class Entry {
        private final Runnable task;
        private final Duration period;
        private final long order;
        private Instant due;
        private boolean cancelled;

        private Entry(Runnable task, Instant due, Duration period, long order) {
            this.task = task;
            this.due = due;
            this.period = period;
            this.order = order;
        }
    }
}
