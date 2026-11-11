package com.markes96.pcclicker.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.application.port.in.PreferencesUseCase;
import com.markes96.pcclicker.application.port.in.SessionAlreadyActiveException;
import com.markes96.pcclicker.application.port.in.WorkspaceUseCase;
import com.markes96.pcclicker.domain.configuration.ClickRate;
import com.markes96.pcclicker.domain.configuration.ClickSettings;
import com.markes96.pcclicker.domain.configuration.MovementSettings;
import com.markes96.pcclicker.domain.configuration.SessionConfiguration;
import com.markes96.pcclicker.domain.configuration.TimeLimit;
import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.Key;
import com.markes96.pcclicker.domain.settings.Preferences;
import com.markes96.pcclicker.domain.settings.SessionSettings;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HotkeyCommandHandlerTest {

    private static final SessionSettings CLICKING = new SessionSettings(
            ClickSettings.enabled(new ClickRate(25)), MovementSettings.disabled(), TimeLimit.unlimited());

    private final List<String> calls = new ArrayList<>();
    private final List<SessionConfiguration> started = new ArrayList<>();
    private boolean sessionActive;
    private Preferences preferences = Preferences.defaults();
    private SessionSettings workspace = CLICKING;
    private HotkeyCommandHandler handler;

    @BeforeEach
    void setUp() {
        final PreferencesUseCase preferencesUseCase = new PreferencesUseCase() {
            @Override
            public Preferences preferences() {
                return preferences;
            }

            @Override
            public void updatePreferences(Preferences updated) {
                preferences = updated;
            }
        };
        final WorkspaceUseCase workspaceUseCase = new WorkspaceUseCase() {
            @Override
            public SessionSettings workspace() {
                return workspace;
            }

            @Override
            public void updateWorkspace(SessionSettings settings) {
                workspace = settings;
            }
        };
        handler = new HotkeyCommandHandler(
                preferencesUseCase,
                workspaceUseCase,
                () -> sessionActive,
                started::add,
                () -> calls.add("stop"),
                () -> calls.add("pause"),
                () -> calls.add("exit"));
    }

    private Key keyOf(Hotkey hotkey) {
        return preferences.keys().keyFor(hotkey);
    }

    @Nested
    class DuringASession {

        @BeforeEach
        void sessionIsActive() {
            sessionActive = true;
        }

        @Test
        void escape_closes_the_application() {
            handler.keyReleased(Key.ESCAPE);

            assertThat(calls).containsExactly("exit");
        }

        @Test
        void the_stop_key_stops_the_session() {
            handler.keyReleased(Key.X);

            assertThat(calls).containsExactly("stop");
        }

        @Test
        void the_pause_key_toggles_the_pause() {
            handler.keyReleased(Key.P);

            assertThat(calls).containsExactly("pause");
        }

        @Test
        void the_start_key_is_ignored() {
            handler.keyReleased(keyOf(Hotkey.START));

            assertThat(started).isEmpty();
            assertThat(calls).isEmpty();
        }
    }

    @Nested
    class WithoutASession {

        @ParameterizedTest
        @EnumSource(value = Hotkey.class, names = "START", mode = EnumSource.Mode.EXCLUDE)
        void session_hotkeys_are_ignored(Hotkey hotkey) {
            handler.keyReleased(keyOf(hotkey));

            assertThat(calls).isEmpty();
            assertThat(started).isEmpty();
        }

        @Test
        void the_start_key_starts_a_session_with_the_current_workspace() {
            handler.keyReleased(Key.F6);

            assertThat(started).containsExactly(CLICKING.toConfiguration());
        }

        @Test
        void the_start_key_does_nothing_when_no_action_is_enabled() {
            workspace = SessionSettings.defaults();

            handler.keyReleased(Key.F6);

            assertThat(started).isEmpty();
        }

        @Test
        void a_session_started_meanwhile_from_the_window_is_not_an_error() {
            final HotkeyCommandHandler racing = new HotkeyCommandHandler(
                    new PreferencesUseCase() {
                        @Override
                        public Preferences preferences() {
                            return Preferences.defaults();
                        }

                        @Override
                        public void updatePreferences(Preferences updated) {
                        }
                    },
                    new WorkspaceUseCase() {
                        @Override
                        public SessionSettings workspace() {
                            return CLICKING;
                        }

                        @Override
                        public void updateWorkspace(SessionSettings settings) {
                        }
                    },
                    () -> false,
                    configuration -> {
                        throw new SessionAlreadyActiveException();
                    },
                    () -> { },
                    () -> { },
                    () -> { });

            racing.keyReleased(Key.F6);
        }
    }

    @Test
    void unbound_keys_do_nothing() {
        sessionActive = true;

        handler.keyReleased(Key.A);

        assertThat(calls).isEmpty();
    }

    @Test
    void rebinding_a_hotkey_takes_effect_immediately() {
        sessionActive = true;
        preferences = preferences.withKeys(preferences.keys().with(Hotkey.STOP, Key.S));

        handler.keyReleased(Key.X);
        handler.keyReleased(Key.S);

        assertThat(calls).containsExactly("stop");
    }

    @Test
    void suspended_hotkeys_do_nothing_until_resumed() {
        sessionActive = true;

        handler.suspendHotkeys();
        handler.keyReleased(Key.X);
        assertThat(calls).isEmpty();

        handler.resumeHotkeys();
        handler.keyReleased(Key.X);
        assertThat(calls).containsExactly("stop");
    }
}
