package com.markes96.pcclicker.adapter.out.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.application.port.out.ScheduledTask;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
class ExecutorTaskSchedulerTest {

    private final ExecutorTaskScheduler scheduler = new ExecutorTaskScheduler();

    @AfterEach
    void tearDown() {
        scheduler.close();
    }

    @Test
    void runs_a_delayed_task_once() throws InterruptedException {
        final CountDownLatch ran = new CountDownLatch(1);

        scheduler.schedule(ran::countDown, Duration.ofMillis(20));

        assertThat(ran.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void runs_periodic_tasks_repeatedly() throws InterruptedException {
        final CountDownLatch fiveRuns = new CountDownLatch(5);

        scheduler.scheduleAtFixedRate(fiveRuns::countDown, Duration.ZERO, Duration.ofMillis(5));

        assertThat(fiveRuns.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void cancelled_tasks_stop_running() throws InterruptedException {
        final AtomicInteger runs = new AtomicInteger();
        final ScheduledTask task = scheduler.scheduleAtFixedRate(runs::incrementAndGet, Duration.ZERO, Duration.ofMillis(5));
        Thread.sleep(50);

        task.cancel();
        final int runsAtCancel = runs.get();
        Thread.sleep(50);

        assertThat(runs.get()).isLessThanOrEqualTo(runsAtCancel + 1);
    }

    @Test
    void a_failing_periodic_task_does_not_kill_the_scheduler() throws InterruptedException {
        scheduler.schedule(() -> {
            throw new IllegalStateException("boom");
        }, Duration.ZERO);
        final CountDownLatch ran = new CountDownLatch(1);

        scheduler.schedule(ran::countDown, Duration.ofMillis(10));

        assertThat(ran.await(2, TimeUnit.SECONDS)).isTrue();
    }

    @Test
    void uses_daemon_threads_so_it_never_keeps_the_process_alive() throws InterruptedException {
        final CountDownLatch ran = new CountDownLatch(1);
        final boolean[] daemon = new boolean[1];

        scheduler.schedule(() -> {
            daemon[0] = Thread.currentThread().isDaemon();
            ran.countDown();
        }, Duration.ZERO);

        assertThat(ran.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(daemon[0]).isTrue();
    }
}
