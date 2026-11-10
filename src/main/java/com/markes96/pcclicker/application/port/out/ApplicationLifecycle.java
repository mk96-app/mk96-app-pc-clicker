package com.markes96.pcclicker.application.port.out;

/** Releases every resource and terminates the process. */
@FunctionalInterface
public interface ApplicationLifecycle {

    void shutdown();
}
