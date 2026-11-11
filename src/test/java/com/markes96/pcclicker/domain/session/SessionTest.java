package com.markes96.pcclicker.domain.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.markes96.pcclicker.domain.configuration.ClickLimit;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SessionTest {

    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    private static Instant at(long seconds) {
        return T0.plusSeconds(seconds);
    }

    private static SessionConfiguration configuration(TimeLimit limit) {
        return new SessionConfiguration(ClickSettings.enabled(new ClickRate(5)), MovementSettings.disabled(), limit);
    }

    private static Session runningSession(TimeLimit limit) {
        final Session session = Session.prepare(configuration(limit));
        session.start(T0);
        return session;
    }

    @Test
    void begins_with_a_countdown() {
        final Session session = Session.prepare(configuration(TimeLimit.unlimited()));

        assertThat(session.status()).isEqualTo(SessionStatus.COUNTDOWN);
        assertThat(session.isActive()).isTrue();
        assertThat(session.isRunning()).isFalse();
        assertThat(session.snapshot(at(5)).elapsed()).isZero();
    }

    @Test
    void runs_once_started() {
        final Session session = runningSession(TimeLimit.unlimited());

        assertThat(session.status()).isEqualTo(SessionStatus.RUNNING);
        assertThat(session.isRunning()).isTrue();
        assertThat(session.snapshot(at(3)).elapsed()).isEqualTo(Duration.ofSeconds(3));
    }

    @Test
    void toggles_between_running_and_paused() {
        final Session session = runningSession(TimeLimit.unlimited());

        session.togglePause(at(4));
        assertThat(session.status()).isEqualTo(SessionStatus.PAUSED);
        assertThat(session.isRunning()).isFalse();
        assertThat(session.isActive()).isTrue();

        session.togglePause(at(10));
        assertThat(session.status()).isEqualTo(SessionStatus.RUNNING);
        assertThat(session.snapshot(at(11)).elapsed()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void cannot_be_paused_during_the_countdown() {
        final Session session = Session.prepare(configuration(TimeLimit.unlimited()));

        assertThatIllegalStateException().isThrownBy(() -> session.togglePause(T0));
    }

    @Test
    void counts_the_clicks_performed() {
        final Session session = runningSession(TimeLimit.unlimited());

        session.recordClick();
        session.recordClick();

        assertThat(session.snapshot(at(1)).clicks()).isEqualTo(2);
    }

    @Test
    void reaches_its_time_limit_only_while_running() {
        final Session session = runningSession(TimeLimit.ofMinutes(1));

        assertThat(session.hasReachedTimeLimit(at(59))).isFalse();
        session.togglePause(at(30));
        assertThat(session.hasReachedTimeLimit(at(90))).isFalse();
        session.togglePause(at(90));
        assertThat(session.hasReachedTimeLimit(at(119))).isFalse();
        assertThat(session.hasReachedTimeLimit(at(120))).isTrue();
    }

    @Test
    void an_unlimited_session_never_reaches_its_time_limit() {
        assertThat(runningSession(TimeLimit.unlimited()).hasReachedTimeLimit(at(1_000_000))).isFalse();
    }

    @Test
    void finishing_records_the_reason_and_freezes_the_clock() {
        final Session session = runningSession(TimeLimit.unlimited());

        session.finish(FinishReason.STOPPED_BY_USER, at(20));

        assertThat(session.status()).isEqualTo(SessionStatus.FINISHED);
        assertThat(session.isActive()).isFalse();
        assertThat(session.finishReason()).contains(FinishReason.STOPPED_BY_USER);
        assertThat(session.snapshot(at(500)).elapsed()).isEqualTo(Duration.ofSeconds(20));
    }

    @Test
    void can_be_cancelled_during_the_countdown() {
        final Session session = Session.prepare(configuration(TimeLimit.unlimited()));

        session.finish(FinishReason.STOPPED_BY_USER, T0);

        assertThat(session.status()).isEqualTo(SessionStatus.FINISHED);
        assertThat(session.snapshot(at(5)).elapsed()).isZero();
    }

    @Test
    void cannot_change_once_finished() {
        final Session session = runningSession(TimeLimit.unlimited());
        session.finish(FinishReason.TIME_LIMIT_REACHED, at(1));

        assertThatIllegalStateException().isThrownBy(() -> session.togglePause(at(2)));
        assertThatIllegalStateException().isThrownBy(() -> session.start(at(2)));
        assertThatIllegalStateException().isThrownBy(() -> session.finish(FinishReason.FAILED, at(2)));
    }

    @Test
    void snapshots_expose_remaining_time_and_progress_when_limited() {
        final Session session = runningSession(TimeLimit.ofMinutes(2));

        final SessionSnapshot snapshot = session.snapshot(at(30));

        assertThat(snapshot.status()).isEqualTo(SessionStatus.RUNNING);
        assertThat(snapshot.remaining()).contains(Duration.ofSeconds(90));
        assertThat(snapshot.progress()).hasValue(0.25);
        assertThat(snapshot.configuration()).isEqualTo(configuration(TimeLimit.ofMinutes(2)));
    }

    @Test
    void snapshots_have_no_remaining_time_nor_progress_when_unlimited() {
        final SessionSnapshot snapshot = runningSession(TimeLimit.unlimited()).snapshot(at(30));

        assertThat(snapshot.remaining()).isEmpty();
        assertThat(snapshot.progress()).isEmpty();
        assertThat(snapshot.remainingClicks()).isEmpty();
    }

    @Test
    void reaches_its_click_limit_once_the_clicks_are_done() {
        final Session session = Session.prepare(new SessionConfiguration(
                ClickSettings.enabled(new ClickRate(5)).withLimit(ClickLimit.ofClicks(3)),
                MovementSettings.disabled(), TimeLimit.unlimited()));
        session.start(T0);

        session.recordClick();
        session.recordClick();
        assertThat(session.hasReachedClickLimit()).isFalse();
        assertThat(session.snapshot(at(1)).remainingClicks()).hasValue(1);

        session.recordClick();
        assertThat(session.hasReachedClickLimit()).isTrue();
        assertThat(session.snapshot(at(1)).remainingClicks()).hasValue(0);
    }

    @Test
    void a_session_that_does_not_click_has_no_click_limit() {
        final Session session = Session.prepare(new SessionConfiguration(
                ClickSettings.disabled().withLimit(ClickLimit.ofClicks(3)),
                MovementSettings.enabled(MovementPattern.CIRCLE, Radius.DEFAULT, VertexCount.DEFAULT),
                TimeLimit.unlimited()));
        session.start(T0);

        assertThat(session.hasReachedClickLimit()).isFalse();
        assertThat(session.snapshot(at(1)).remainingClicks()).isEmpty();
    }
}
