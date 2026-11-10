package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.application.port.out.event.CountdownTicked;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SessionEventPresenterTest {

    @Test
    void hands_events_to_the_view_model_on_the_ui_thread() {
        final List<Runnable> uiQueue = new ArrayList<>();
        final SessionViewModel viewModel = new SessionViewModel();
        final SessionEventPresenter presenter = new SessionEventPresenter(viewModel, uiQueue::add);

        presenter.publish(new CountdownTicked(2));

        assertThat(viewModel.phase().get()).isEqualTo(SessionPhase.IDLE);
        uiQueue.forEach(Runnable::run);
        assertThat(viewModel.phase().get()).isEqualTo(SessionPhase.COUNTDOWN);
        assertThat(viewModel.countdown().get()).isEqualTo(2);
    }
}
