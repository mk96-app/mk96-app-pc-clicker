package com.markes96.pcclicker.application.port.out;

/** Handle of a scheduled task. */
@FunctionalInterface
public interface ScheduledTask {

    /** Prevents future runs. Idempotent. A run already in progress is allowed to finish. */
    void cancel();
}
