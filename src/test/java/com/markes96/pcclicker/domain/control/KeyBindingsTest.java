package com.markes96.pcclicker.domain.control;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Map;
import org.junit.jupiter.api.Test;

class KeyBindingsTest {

    @Test
    void defaults_are_f6_to_start_escape_to_exit_x_to_stop_and_p_to_pause() {
        final KeyBindings bindings = KeyBindings.defaults();

        assertThat(bindings.keyFor(Hotkey.START)).isEqualTo(Key.F6);
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
                .isThrownBy(() -> new KeyBindings(
                        Map.of(Hotkey.START, Key.F6, Hotkey.EXIT, Key.ESCAPE, Hotkey.STOP, Key.X)))
                .withMessageContaining("TOGGLE_PAUSE");
    }

    @Test
    void a_key_cannot_trigger_two_hotkeys() {
        assertThatIllegalArgumentException().isThrownBy(() -> new KeyBindings(
                Map.of(Hotkey.START, Key.F6, Hotkey.EXIT, Key.ESCAPE, Hotkey.STOP, Key.X, Hotkey.TOGGLE_PAUSE, Key.X)))
                .withMessageContaining("two hotkeys");
    }

    @Test
    void rebinding_to_a_free_key_only_changes_that_hotkey() {
        final KeyBindings rebound = KeyBindings.defaults().with(Hotkey.STOP, Key.S);

        assertThat(rebound.keyFor(Hotkey.STOP)).isEqualTo(Key.S);
        assertThat(rebound.hotkeyFor(Key.X)).isEmpty();
        assertThat(rebound.keyFor(Hotkey.TOGGLE_PAUSE)).isEqualTo(Key.P);
        assertThat(KeyBindings.defaults().keyFor(Hotkey.STOP)).isEqualTo(Key.X);
    }

    @Test
    void rebinding_to_a_key_in_use_swaps_the_keys_of_both_hotkeys() {
        final KeyBindings rebound = KeyBindings.defaults().with(Hotkey.STOP, Key.P);

        assertThat(rebound.keyFor(Hotkey.STOP)).isEqualTo(Key.P);
        assertThat(rebound.keyFor(Hotkey.TOGGLE_PAUSE)).isEqualTo(Key.X);
    }

    @Test
    void rebinding_to_its_own_key_changes_nothing() {
        assertThat(KeyBindings.defaults().with(Hotkey.STOP, Key.X)).isEqualTo(KeyBindings.defaults());
    }

    @Test
    void lists_every_binding_in_hotkey_order() {
        assertThat(KeyBindings.defaults().asMap()).containsExactly(
                Map.entry(Hotkey.START, Key.F6),
                Map.entry(Hotkey.EXIT, Key.ESCAPE),
                Map.entry(Hotkey.STOP, Key.X),
                Map.entry(Hotkey.TOGGLE_PAUSE, Key.P));
    }

    @Test
    void only_start_works_without_a_session() {
        assertThat(Hotkey.START.controlsActiveSession()).isFalse();
        assertThat(Hotkey.STOP.controlsActiveSession()).isTrue();
    }

    @Test
    void keys_have_a_short_label_for_key_caps() {
        assertThat(Key.ESCAPE.label()).isEqualTo("Esc");
        assertThat(Key.X.label()).isEqualTo("X");
        assertThat(Key.F12.label()).isEqualTo("F12");
    }
}
