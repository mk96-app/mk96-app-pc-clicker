package com.markes96.pcclicker.domain.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Map;
import org.junit.jupiter.api.Test;

class KeyBindingsTest {

    @Test
    void defaults_are_escape_to_exit_x_to_stop_and_p_to_pause() {
        final KeyBindings bindings = KeyBindings.defaults();

        assertThat(bindings.keyFor(Hotkey.EXIT)).isEqualTo(Key.ESCAPE);
        assertThat(bindings.keyFor(Hotkey.STOP)).isEqualTo(Key.X);
        assertThat(bindings.keyFor(Hotkey.TOGGLE_PAUSE)).isEqualTo(Key.P);
    }

    @Test
    void finds_the_hotkey_bound_to_a_key() {
        final KeyBindings bindings = KeyBindings.defaults();

        assertThat(bindings.hotkeyFor(Key.P)).contains(Hotkey.TOGGLE_PAUSE);
        assertThat(bindings.hotkeyFor(Key.A)).isEmpty();
    }

    @Test
    void every_hotkey_must_be_bound() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new KeyBindings(Map.of(Hotkey.EXIT, Key.ESCAPE, Hotkey.STOP, Key.X)))
                .withMessageContaining("TOGGLE_PAUSE");
    }

    @Test
    void a_key_cannot_trigger_two_hotkeys() {
        assertThatIllegalArgumentException().isThrownBy(() -> new KeyBindings(
                Map.of(Hotkey.EXIT, Key.ESCAPE, Hotkey.STOP, Key.X, Hotkey.TOGGLE_PAUSE, Key.X)));
    }

    @Test
    void keys_have_a_short_label_for_key_caps() {
        assertThat(Key.ESCAPE.label()).isEqualTo("Esc");
        assertThat(Key.X.label()).isEqualTo("X");
        assertThat(Key.F12.label()).isEqualTo("F12");
    }
}
