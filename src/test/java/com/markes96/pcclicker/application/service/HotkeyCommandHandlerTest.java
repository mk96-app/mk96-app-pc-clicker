package com.markes96.pcclicker.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.control.Hotkey;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class HotkeyCommandHandlerTest {

    private final List<String> calls = new ArrayList<>();
    private boolean sessionActive;
    private HotkeyCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new HotkeyCommandHandler(
                () -> sessionActive,
                () -> calls.add("stop"),
                () -> calls.add("pause"),
                () -> calls.add("exit"));
    }

    @Test
    void escape_closes_the_application() {
        sessionActive = true;

        handler.handle(Hotkey.EXIT);

        assertThat(calls).containsExactly("exit");
    }

    @Test
    void stop_key_stops_the_session() {
        sessionActive = true;

        handler.handle(Hotkey.STOP);

        assertThat(calls).containsExactly("stop");
    }

    @Test
    void pause_key_toggles_the_pause() {
        sessionActive = true;

        handler.handle(Hotkey.TOGGLE_PAUSE);

        assertThat(calls).containsExactly("pause");
    }

    @ParameterizedTest
    @EnumSource(Hotkey.class)
    void hotkeys_are_ignored_while_no_session_is_active(Hotkey hotkey) {
        sessionActive = false;

        handler.handle(hotkey);

        assertThat(calls).isEmpty();
    }
}
