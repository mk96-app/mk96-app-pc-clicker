package com.markes96.pcclicker.adapter.in.ui.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.application.port.out.event.CountdownTicked;
import com.markes96.pcclicker.application.port.out.event.SessionFinished;
import com.markes96.pcclicker.application.port.out.event.SessionPaused;
import com.markes96.pcclicker.application.port.out.event.SessionProgressed;
import com.markes96.pcclicker.application.port.out.event.SessionResumed;
import com.markes96.pcclicker.application.port.out.event.SessionStarted;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.session.FinishReason;
import com.markes96.pcclicker.domain.session.SessionSnapshot;
import com.markes96.pcclicker.domain.session.SessionStatus;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SessionViewModelTest {

    private static final SessionConfiguration LIMITED = new SessionConfiguration(
            ClickSettings.enabled(new ClickRate(10)), MovementSettings.disabled(), TimeLimit.ofMinutes(2));
    private static final SessionConfiguration UNLIMITED = new SessionConfiguration(
            ClickSettings.enabled(new ClickRate(10)), MovementSettings.disabled(), TimeLimit.unlimited());

    private final SessionViewModel viewModel = new SessionViewModel();

    private static SessionSnapshot snapshot(SessionStatus status, long seconds, long clicks, SessionConfiguration c) {
        return new SessionSnapshot(status, Duration.ofSeconds(seconds), clicks, c);
    }

    @Test
    void is_idle_until_a_session_begins() {
        assertThat(viewModel.phase().get()).isEqualTo(SessionPhase.IDLE);
        assertThat(viewModel.active().get()).isFalse();
    }

    @Test
    void follows_the_countdown() {
        viewModel.apply(new CountdownTicked(3));
        assertThat(viewModel.phase().get()).isEqualTo(SessionPhase.COUNTDOWN);
        assertThat(viewModel.countdown().get()).isEqualTo(3);
        assertThat(viewModel.active().get()).isTrue();

        viewModel.apply(new CountdownTicked(1));
        assertThat(viewModel.countdown().get()).isEqualTo(1);
    }

    @Test
    void shows_the_figures_of_a_running_session() {
        viewModel.apply(new CountdownTicked(3));
        viewModel.apply(new SessionStarted(snapshot(SessionStatus.RUNNING, 0, 0, LIMITED)));
        viewModel.apply(new SessionProgressed(snapshot(SessionStatus.RUNNING, 30, 300, LIMITED)));

        assertThat(viewModel.phase().get()).isEqualTo(SessionPhase.RUNNING);
        assertThat(viewModel.elapsed().get()).isEqualTo(Duration.ofSeconds(30));
        assertThat(viewModel.clicks().get()).isEqualTo(300);
        assertThat(viewModel.limited().get()).isTrue();
        assertThat(viewModel.remaining().get()).isEqualTo(Duration.ofSeconds(90));
        assertThat(viewModel.progress().get()).isEqualTo(0.25);
        assertThat(viewModel.configuration().get()).isEqualTo(LIMITED);
    }

    @Test
    void has_no_remaining_time_when_unlimited() {
        viewModel.apply(new SessionStarted(snapshot(SessionStatus.RUNNING, 0, 0, UNLIMITED)));

        assertThat(viewModel.limited().get()).isFalse();
        assertThat(viewModel.remaining().get()).isNull();
        assertThat(viewModel.progress().get()).isZero();
    }

    @Test
    void reflects_pauses() {
        viewModel.apply(new SessionStarted(snapshot(SessionStatus.RUNNING, 0, 0, LIMITED)));

        viewModel.apply(new SessionPaused(snapshot(SessionStatus.PAUSED, 10, 100, LIMITED)));
        assertThat(viewModel.phase().get()).isEqualTo(SessionPhase.PAUSED);

        viewModel.apply(new SessionResumed(snapshot(SessionStatus.RUNNING, 10, 100, LIMITED)));
        assertThat(viewModel.phase().get()).isEqualTo(SessionPhase.RUNNING);
    }

    @Test
    void goes_back_to_idle_and_reports_the_outcome_when_the_session_finishes() {
        final List<SessionOutcome> outcomes = new ArrayList<>();
        viewModel.onFinished(outcomes::add);
        viewModel.apply(new SessionStarted(snapshot(SessionStatus.RUNNING, 0, 0, LIMITED)));

        viewModel.apply(new SessionFinished(snapshot(SessionStatus.FINISHED, 120, 1200, LIMITED),
                FinishReason.TIME_LIMIT_REACHED));

        assertThat(viewModel.phase().get()).isEqualTo(SessionPhase.IDLE);
        assertThat(viewModel.active().get()).isFalse();
        assertThat(outcomes).containsExactly(
                new SessionOutcome(FinishReason.TIME_LIMIT_REACHED, Duration.ofSeconds(120), 1200));
    }

    @Test
    void a_session_cancelled_during_the_countdown_also_reports_its_outcome() {
        final List<SessionOutcome> outcomes = new ArrayList<>();
        viewModel.onFinished(outcomes::add);
        viewModel.apply(new CountdownTicked(3));

        viewModel.apply(new SessionFinished(snapshot(SessionStatus.FINISHED, 0, 0, LIMITED), FinishReason.STOPPED_BY_USER));

        assertThat(outcomes).singleElement().extracting(SessionOutcome::reason).isEqualTo(FinishReason.STOPPED_BY_USER);
    }

    @Test
    void resets_its_figures_for_the_next_session() {
        viewModel.apply(new SessionStarted(snapshot(SessionStatus.RUNNING, 0, 0, LIMITED)));
        viewModel.apply(new SessionProgressed(snapshot(SessionStatus.RUNNING, 30, 300, LIMITED)));
        viewModel.apply(new SessionFinished(snapshot(SessionStatus.FINISHED, 30, 300, LIMITED), FinishReason.STOPPED_BY_USER));

        viewModel.apply(new CountdownTicked(3));

        assertThat(viewModel.elapsed().get()).isEqualTo(Duration.ZERO);
        assertThat(viewModel.clicks().get()).isZero();
        assertThat(viewModel.progress().get()).isZero();
    }
}
