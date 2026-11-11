package com.markes96.pcclicker.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.markes96.pcclicker.application.port.in.SessionAlreadyActiveException;
import com.markes96.pcclicker.application.port.out.event.CountdownTicked;
import com.markes96.pcclicker.application.port.out.event.SessionFinished;
import com.markes96.pcclicker.application.port.out.event.SessionPaused;
import com.markes96.pcclicker.application.port.out.event.SessionProgressed;
import com.markes96.pcclicker.application.port.out.event.SessionResumed;
import com.markes96.pcclicker.application.port.out.event.SessionStarted;
import com.markes96.pcclicker.domain.configuration.ClickLimit;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.ClickType;
import com.markes96.pcclicker.domain.configuration.MouseButton;
import com.markes96.pcclicker.domain.configuration.MovementPattern;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.Radius;
import com.markes96.pcclicker.domain.configuration.RateVariation;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.configuration.VertexCount;
import com.markes96.pcclicker.domain.geometry.ScreenArea;
import com.markes96.pcclicker.domain.geometry.ScreenPoint;
import com.markes96.pcclicker.domain.movement.MovementPathFactory;
import com.markes96.pcclicker.domain.session.FinishReason;
import com.markes96.pcclicker.domain.session.SessionStatus;
import com.markes96.pcclicker.domain.settings.CountdownSeconds;
import com.markes96.pcclicker.support.FakeMouseDriver;
import com.markes96.pcclicker.support.RecordingEventPublisher;
import com.markes96.pcclicker.support.VirtualTimeScheduler;
import java.time.Duration;
import java.util.SplittableRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SessionServiceTest {

    private static final ScreenPoint ORIGIN = new ScreenPoint(400, 300);
    private static final ScreenArea SCREEN = new ScreenArea(0, 0, 1920, 1080);
    private static final Duration COUNTDOWN = Duration.ofSeconds(3);

    private CountdownSeconds countdown = CountdownSeconds.DEFAULT;
    private VirtualTimeScheduler time;
    private FakeMouseDriver mouse;
    private RecordingEventPublisher events;
    private SessionService service;

    @BeforeEach
    void setUp() {
        time = new VirtualTimeScheduler();
        mouse = new FakeMouseDriver(ORIGIN);
        events = new RecordingEventPublisher();
        service = new SessionService(
                mouse,
                point -> SCREEN,
                time,
                time,
                events,
                new MovementPathFactory(new SplittableRandom(11)),
                new SplittableRandom(13),
                SessionTimings.defaults(),
                () -> countdown);
    }

    private static SessionConfiguration clicking(int perSecond, TimeLimit limit) {
        return new SessionConfiguration(
                ClickSettings.enabled(new ClickRate(perSecond)), MovementSettings.disabled(), limit);
    }

    private static SessionConfiguration movingInCircles() {
        return new SessionConfiguration(
                ClickSettings.disabled(),
                MovementSettings.enabled(MovementPattern.CIRCLE, new Radius(10), VertexCount.DEFAULT),
                TimeLimit.unlimited());
    }

    private void startAndSkipCountdown(SessionConfiguration configuration) {
        service.start(configuration);
        time.advance(COUNTDOWN);
    }

    @Nested
    class Countdown {

        @Test
        void announces_three_two_one_before_starting() {
            service.start(clicking(10, TimeLimit.unlimited()));
            time.advance(Duration.ofMillis(2_999));

            assertThat(events.eventsOfType(CountdownTicked.class))
                    .extracting(CountdownTicked::secondsLeft)
                    .containsExactly(3, 2, 1);
            assertThat(events.eventsOfType(SessionStarted.class)).isEmpty();
            assertThat(mouse.clicks()).isZero();
        }

        @Test
        void starts_the_session_when_the_countdown_ends() {
            startAndSkipCountdown(clicking(10, TimeLimit.unlimited()));

            assertThat(events.eventsOfType(SessionStarted.class)).hasSize(1);
            assertThat(service.isSessionActive()).isTrue();
        }

        @Test
        void can_be_cancelled_before_anything_happens() {
            service.start(clicking(10, TimeLimit.unlimited()));
            time.advance(Duration.ofSeconds(1));

            service.stop();
            time.advance(Duration.ofSeconds(10));

            assertThat(mouse.clicks()).isZero();
            assertThat(events.eventsOfType(SessionStarted.class)).isEmpty();
            assertThat(events.last()).isInstanceOfSatisfying(SessionFinished.class,
                    finished -> assertThat(finished.reason()).isEqualTo(FinishReason.STOPPED_BY_USER));
            assertThat(time.activeTasks()).isZero();
        }

        @Test
        void ignores_pause_requests() {
            service.start(clicking(10, TimeLimit.unlimited()));

            service.togglePause();

            assertThat(events.eventsOfType(SessionPaused.class)).isEmpty();
        }
    }

    @Nested
    class Clicking {

        @Test
        void clicks_at_the_configured_rate() {
            startAndSkipCountdown(clicking(10, TimeLimit.unlimited()));
            time.advance(Duration.ofMillis(950));

            assertThat(mouse.clicks()).isEqualTo(10);
        }

        @Test
        void counts_the_clicks_in_the_progress_events() {
            startAndSkipCountdown(clicking(10, TimeLimit.unlimited()));
            time.advance(Duration.ofSeconds(2));

            final SessionProgressed last = events.eventsOfType(SessionProgressed.class).getLast();
            assertThat(last.snapshot().clicks()).isEqualTo(mouse.clicks());
            assertThat(last.snapshot().elapsed()).isEqualTo(Duration.ofSeconds(2));
        }

        @Test
        void does_not_move_the_pointer_when_only_clicking() {
            startAndSkipCountdown(clicking(10, TimeLimit.unlimited()));
            time.advance(Duration.ofSeconds(1));

            assertThat(mouse.moves()).isEmpty();
        }
    }

    @Nested
    class ClickOptions {

        @Test
        void clicks_with_the_configured_button_and_type() {
            startAndSkipCountdown(new SessionConfiguration(
                    ClickSettings.enabled(new ClickRate(10)).withButton(MouseButton.RIGHT).withType(ClickType.DOUBLE),
                    MovementSettings.disabled(), TimeLimit.unlimited()));
            time.advance(Duration.ofMillis(250));

            assertThat(mouse.clickLog())
                    .hasSize(3)
                    .containsOnly(new FakeMouseDriver.Click(MouseButton.RIGHT, ClickType.DOUBLE));
        }

        @Test
        void a_double_click_counts_as_one_click() {
            startAndSkipCountdown(new SessionConfiguration(
                    ClickSettings.enabled(new ClickRate(10)).withType(ClickType.DOUBLE),
                    MovementSettings.disabled(), TimeLimit.unlimited()));
            time.advance(Duration.ofMillis(950));
            service.stop();

            assertThat(mouse.clicks()).isEqualTo(10);
            assertThat(events.eventsOfType(SessionFinished.class).getLast().snapshot().clicks()).isEqualTo(10);
        }

        @Test
        void with_variation_keeps_the_average_rate_with_a_single_pending_click() {
            startAndSkipCountdown(new SessionConfiguration(
                    ClickSettings.enabled(new ClickRate(10)).withVariation(new RateVariation(40)),
                    MovementSettings.disabled(), TimeLimit.unlimited()));

            for (int second = 0; second < 30; second++) {
                time.advance(Duration.ofSeconds(1));
                assertThat(time.activeTasks()).as("click task + progress task").isEqualTo(2);
            }
            assertThat(mouse.clicks()).isBetween(285, 315);
        }

        @Test
        void with_variation_stops_clicking_while_paused_and_when_stopped() {
            startAndSkipCountdown(new SessionConfiguration(
                    ClickSettings.enabled(new ClickRate(20)).withVariation(new RateVariation(30)),
                    MovementSettings.disabled(), TimeLimit.unlimited()));
            time.advance(Duration.ofSeconds(1));

            service.togglePause();
            final int beforePause = mouse.clicks();
            time.advance(Duration.ofSeconds(2));
            assertThat(mouse.clicks()).isEqualTo(beforePause);

            service.stop();
            time.advance(Duration.ofSeconds(2));
            assertThat(time.activeTasks()).isZero();
        }
    }

    @Nested
    class ClickLimits {

        private SessionConfiguration clickingUpTo(int limit, int perSecond) {
            return new SessionConfiguration(
                    ClickSettings.enabled(new ClickRate(perSecond))
                            .withLimit(ClickLimit.ofClicks(limit)),
                    MovementSettings.disabled(), TimeLimit.unlimited());
        }

        @Test
        void finishes_right_after_the_last_click() {
            startAndSkipCountdown(clickingUpTo(5, 10));
            time.advance(Duration.ofSeconds(3));

            assertThat(mouse.clicks()).isEqualTo(5);
            assertThat(service.isSessionActive()).isFalse();
            assertThat(time.activeTasks()).isZero();
            assertThat(events.last()).isInstanceOfSatisfying(SessionFinished.class, finished -> {
                assertThat(finished.reason()).isEqualTo(FinishReason.CLICK_LIMIT_REACHED);
                assertThat(finished.snapshot().clicks()).isEqualTo(5);
                assertThat(finished.snapshot().elapsed()).isEqualTo(Duration.ofMillis(400));
            });
        }

        @Test
        void also_applies_with_an_irregular_pace() {
            startAndSkipCountdown(new SessionConfiguration(
                    ClickSettings.enabled(new ClickRate(50))
                            .withVariation(new RateVariation(50))
                            .withLimit(ClickLimit.ofClicks(30)),
                    MovementSettings.disabled(), TimeLimit.unlimited()));
            time.advance(Duration.ofSeconds(5));

            assertThat(mouse.clicks()).isEqualTo(30);
            assertThat(time.activeTasks()).isZero();
        }

        @Test
        void the_time_limit_can_still_end_the_session_first() {
            startAndSkipCountdown(new SessionConfiguration(
                    ClickSettings.enabled(new ClickRate(1))
                            .withLimit(ClickLimit.ofClicks(1_000)),
                    MovementSettings.disabled(), TimeLimit.ofMinutes(1)));
            time.advance(Duration.ofMinutes(2));

            assertThat(events.last()).isInstanceOfSatisfying(SessionFinished.class,
                    finished -> assertThat(finished.reason()).isEqualTo(FinishReason.TIME_LIMIT_REACHED));
        }
    }

    @Nested
    class ConfigurableCountdown {

        @Test
        void counts_down_the_seconds_chosen_in_the_preferences() {
            countdown = new CountdownSeconds(5);

            service.start(clicking(10, TimeLimit.unlimited()));
            time.advance(Duration.ofSeconds(5));

            assertThat(events.eventsOfType(CountdownTicked.class))
                    .extracting(CountdownTicked::secondsLeft)
                    .containsExactly(5, 4, 3, 2, 1);
            assertThat(events.eventsOfType(SessionStarted.class)).hasSize(1);
        }

        @Test
        void zero_seconds_starts_acting_immediately() {
            countdown = new CountdownSeconds(0);

            service.start(clicking(10, TimeLimit.unlimited()));

            assertThat(events.eventsOfType(CountdownTicked.class)).isEmpty();
            assertThat(events.events()).first().isInstanceOf(SessionStarted.class);
            time.advance(Duration.ZERO);
            assertThat(mouse.clicks()).isEqualTo(1);
        }

        @Test
        void an_immediate_start_that_cannot_read_the_pointer_fails_the_session() {
            countdown = new CountdownSeconds(0);
            mouse.failWith(new IllegalStateException("No pointer"));

            service.start(movingInCircles());

            assertThat(service.isSessionActive()).isFalse();
            assertThat(events.last()).isInstanceOfSatisfying(SessionFinished.class,
                    finished -> assertThat(finished.reason()).isEqualTo(FinishReason.FAILED));
        }

        @Test
        void a_change_applies_to_the_next_session() {
            startAndSkipCountdown(clicking(10, TimeLimit.unlimited()));
            service.stop();
            events.clear();

            countdown = new CountdownSeconds(1);
            service.start(clicking(10, TimeLimit.unlimited()));

            assertThat(events.events()).first().isEqualTo(new CountdownTicked(1));
        }
    }

    @Nested
    class Moving {

        @Test
        void follows_the_pattern_around_the_pointer_position_at_start() {
            startAndSkipCountdown(movingInCircles());
            time.advance(Duration.ofMillis(10));

            assertThat(mouse.moves()).first().isEqualTo(new ScreenPoint(410, 300));
        }

        @Test
        void moves_fifty_times_per_second() {
            startAndSkipCountdown(movingInCircles());
            time.advance(Duration.ofMillis(990));

            assertThat(mouse.moves()).hasSize(50);
        }

        @Test
        void does_not_click_when_only_moving() {
            startAndSkipCountdown(movingInCircles());
            time.advance(Duration.ofSeconds(1));

            assertThat(mouse.clicks()).isZero();
        }

        @Test
        void takes_the_starting_position_after_the_countdown() {
            service.start(movingInCircles());
            time.advance(Duration.ofSeconds(2));
            mouse.moveTo(new ScreenPoint(1000, 800));

            time.advance(Duration.ofSeconds(1));

            assertThat(mouse.moves()).last().isEqualTo(new ScreenPoint(1010, 800));
        }
    }

    @Nested
    class Pausing {

        @Test
        void stops_acting_while_paused_and_resumes_afterwards() {
            startAndSkipCountdown(clicking(10, TimeLimit.unlimited()));
            time.advance(Duration.ofMillis(450));
            final int clicksBeforePause = mouse.clicks();

            service.togglePause();
            time.advance(Duration.ofSeconds(5));
            assertThat(mouse.clicks()).isEqualTo(clicksBeforePause);

            service.togglePause();
            time.advance(Duration.ofSeconds(1));
            assertThat(mouse.clicks()).isGreaterThan(clicksBeforePause);
        }

        @Test
        void publishes_pause_and_resume_events() {
            startAndSkipCountdown(clicking(10, TimeLimit.unlimited()));

            service.togglePause();
            service.togglePause();

            assertThat(events.eventsOfType(SessionPaused.class)).singleElement()
                    .satisfies(paused -> assertThat(paused.snapshot().status()).isEqualTo(SessionStatus.PAUSED));
            assertThat(events.eventsOfType(SessionResumed.class)).hasSize(1);
        }

        @Test
        void paused_time_does_not_count_towards_the_limit() {
            startAndSkipCountdown(clicking(10, TimeLimit.ofMinutes(1)));
            time.advance(Duration.ofSeconds(30));
            service.togglePause();
            time.advance(Duration.ofMinutes(10));
            service.togglePause();

            time.advance(Duration.ofSeconds(29));
            assertThat(service.isSessionActive()).isTrue();

            time.advance(Duration.ofSeconds(1));
            assertThat(service.isSessionActive()).isFalse();
        }

        @Test
        void does_nothing_without_a_session() {
            service.togglePause();

            assertThat(events.events()).isEmpty();
        }
    }

    @Nested
    class Finishing {

        @Test
        void finishes_by_itself_when_the_time_limit_is_reached() {
            startAndSkipCountdown(clicking(1, TimeLimit.ofMinutes(1)));
            time.advance(Duration.ofSeconds(60));

            assertThat(service.isSessionActive()).isFalse();
            assertThat(events.last()).isInstanceOfSatisfying(SessionFinished.class, finished -> {
                assertThat(finished.reason()).isEqualTo(FinishReason.TIME_LIMIT_REACHED);
                assertThat(finished.snapshot().elapsed()).isEqualTo(Duration.ofSeconds(60));
            });
        }

        @Test
        void stopping_cancels_every_scheduled_task() {
            startAndSkipCountdown(new SessionConfiguration(
                    ClickSettings.enabled(new ClickRate(20)),
                    MovementSettings.enabled(MovementPattern.REBOUND, Radius.DEFAULT, VertexCount.DEFAULT),
                    TimeLimit.unlimited()));
            time.advance(Duration.ofSeconds(1));

            service.stop();
            final int clicks = mouse.clicks();
            final int moves = mouse.moves().size();
            time.advance(Duration.ofSeconds(5));

            assertThat(time.activeTasks()).isZero();
            assertThat(mouse.clicks()).isEqualTo(clicks);
            assertThat(mouse.moves()).hasSize(moves);
            assertThat(service.isSessionActive()).isFalse();
        }

        @Test
        void stopping_without_a_session_does_nothing() {
            service.stop();

            assertThat(events.events()).isEmpty();
        }

        @Test
        void a_failing_mouse_finishes_the_session_as_failed() {
            startAndSkipCountdown(clicking(10, TimeLimit.unlimited()));

            mouse.failWith(new IllegalStateException("Input blocked by the system"));
            time.advance(Duration.ofMillis(200));

            assertThat(service.isSessionActive()).isFalse();
            assertThat(events.last()).isInstanceOfSatisfying(SessionFinished.class,
                    finished -> assertThat(finished.reason()).isEqualTo(FinishReason.FAILED));
            assertThat(time.activeTasks()).isZero();
        }

        @Test
        void failing_to_read_the_pointer_at_start_finishes_the_session_as_failed() {
            service.start(movingInCircles());
            mouse.failWith(new IllegalStateException("No pointer"));

            time.advance(COUNTDOWN);

            assertThat(events.last()).isInstanceOfSatisfying(SessionFinished.class,
                    finished -> assertThat(finished.reason()).isEqualTo(FinishReason.FAILED));
        }

        @Test
        void a_new_session_can_start_after_the_previous_one_finished() {
            startAndSkipCountdown(clicking(10, TimeLimit.unlimited()));
            service.stop();
            events.clear();

            service.start(clicking(10, TimeLimit.unlimited()));

            assertThat(events.events()).first().isEqualTo(new CountdownTicked(3));
        }
    }

    @Test
    void refuses_to_start_a_second_session_while_one_is_active() {
        service.start(clicking(10, TimeLimit.unlimited()));

        assertThatThrownBy(() -> service.start(clicking(10, TimeLimit.unlimited())))
                .isInstanceOf(SessionAlreadyActiveException.class);
    }

    @Test
    void reports_no_active_session_initially() {
        assertThat(service.isSessionActive()).isFalse();
    }
}
