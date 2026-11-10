package com.markes96.pcclicker.adapter.out.scheduling;

import com.markes96.pcclicker.application.port.out.ScheduledTask;
import com.markes96.pcclicker.application.port.out.TaskScheduler;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@link TaskScheduler} backed by a {@link ScheduledThreadPoolExecutor} with nanosecond periods.
 * Tasks that throw are logged instead of being silently dropped, and threads are daemons so they
 * never keep the process alive.
 */
public final class ExecutorTaskScheduler implements TaskScheduler, AutoCloseable {

    private static final Logger LOG = System.getLogger(ExecutorTaskScheduler.class.getName());
    private static final int THREADS = 2;

    private final ScheduledThreadPoolExecutor executor;

    public ExecutorTaskScheduler() {
        final AtomicInteger counter = new AtomicInteger();
        executor = new ScheduledThreadPoolExecutor(THREADS, runnable -> {
            final Thread thread = new Thread(runnable, "session-scheduler-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
        executor.setRemoveOnCancelPolicy(true);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
    }

    @Override
    public ScheduledTask schedule(Runnable task, Duration delay) {
        return handleOf(executor.schedule(logging(task), delay.toNanos(), TimeUnit.NANOSECONDS));
    }

    @Override
    public ScheduledTask scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period) {
        return handleOf(executor.scheduleAtFixedRate(
                logging(task), initialDelay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS));
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

    private static ScheduledTask handleOf(ScheduledFuture<?> future) {
        return () -> future.cancel(false);
    }

    /**
     * A periodic task that throws is cancelled by the executor without a trace; logging keeps the
     * failure visible while still honouring that contract.
     */
    private static Runnable logging(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (RuntimeException failure) {
                LOG.log(Level.ERROR, "Scheduled task failed", failure);
                throw failure;
            }
        };
    }
}
