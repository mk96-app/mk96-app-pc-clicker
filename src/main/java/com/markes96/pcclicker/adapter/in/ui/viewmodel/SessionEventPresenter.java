package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import com.markes96.pcclicker.application.port.out.SessionEventPublisher;
import com.markes96.pcclicker.application.port.out.event.SessionEvent;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Driven adapter: receives session events on any thread and hands them, in order, to the
 * {@link SessionViewModel} on the UI thread. Never blocks the publisher.
 */
public final class SessionEventPresenter implements SessionEventPublisher {

    private final SessionViewModel viewModel;
    private final Executor uiThread;

    /** @param uiThread runs work on the UI thread in submission order, e.g. {@code Platform::runLater} */
    public SessionEventPresenter(SessionViewModel viewModel, Executor uiThread) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
        this.uiThread = Objects.requireNonNull(uiThread, "uiThread");
    }

    @Override
    public void publish(SessionEvent event) {
        Objects.requireNonNull(event, "event");
        uiThread.execute(() -> viewModel.apply(event));
    }
}
