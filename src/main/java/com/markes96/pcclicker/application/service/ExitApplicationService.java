package com.markes96.pcclicker.application.service;

import com.markes96.pcclicker.application.port.in.ExitApplicationUseCase;
import com.markes96.pcclicker.application.port.in.StopSessionUseCase;
import com.markes96.pcclicker.application.port.out.ApplicationLifecycle;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Objects;

/** Leaves the pointer alone before closing: stops the session, then shuts the application down. */
public final class ExitApplicationService implements ExitApplicationUseCase {

    private static final Logger LOG = System.getLogger(ExitApplicationService.class.getName());

    private final StopSessionUseCase stop;
    private final ApplicationLifecycle lifecycle;

    public ExitApplicationService(StopSessionUseCase stop, ApplicationLifecycle lifecycle) {
        this.stop = Objects.requireNonNull(stop, "stop");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
    }

    @Override
    public void exit() {
        try {
            stop.stop();
        } catch (RuntimeException failure) {
            LOG.log(Level.WARNING, "Could not stop the session cleanly before exiting", failure);
        } finally {
            lifecycle.shutdown();
        }
    }
}
