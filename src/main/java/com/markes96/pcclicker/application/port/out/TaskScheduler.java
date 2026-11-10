package com.markes96.pcclicker.application.port.out;

import java.time.Duration;

/**
 * Runs work later or periodically. A periodic task never overlaps with itself; when a run is late,
 * the following ones catch up so the average rate is preserved.
 */
public interface TaskScheduler {

    ScheduledTask schedule(Runnable task, Duration delay);

    ScheduledTask scheduleAtFixedRate(Runnable task, Duration initialDelay, Duration period);
}
