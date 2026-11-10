package com.markes96.pcclicker.adapter.in.hotkey;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.control.Hotkey;
import com.markes96.pcclicker.domain.control.KeyBindings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;

class HotkeyDispatcherTest {

    private final List<Hotkey> handled = new ArrayList<>();
    private final List<Runnable> queued = new ArrayList<>();
    private final Executor queueingExecutor = queued::add;

    private final HotkeyDispatcher dispatcher =
            new HotkeyDispatcher(KeyBindings.defaults(), handled::add, queueingExecutor);

    @Test
    void dispatches_bound_keys_to_the_use_case_off_the_hook_thread() {
        dispatcher.onKeyReleased(88);

        assertThat(handled).isEmpty();
        queued.forEach(Runnable::run);
        assertThat(handled).containsExactly(Hotkey.STOP);
    }

    @Test
    void maps_every_default_key() {
        dispatcher.onKeyReleased(27);
        dispatcher.onKeyReleased(80);
        dispatcher.onKeyReleased(88);
        queued.forEach(Runnable::run);

        assertThat(handled).containsExactly(Hotkey.EXIT, Hotkey.TOGGLE_PAUSE, Hotkey.STOP);
    }

    @Test
    void ignores_unbound_keys() {
        dispatcher.onKeyReleased(65);
        dispatcher.onKeyReleased(13);

        assertThat(queued).isEmpty();
    }

    @Test
    void a_failing_use_case_does_not_break_the_dispatcher() {
        final HotkeyDispatcher failing = new HotkeyDispatcher(KeyBindings.defaults(), hotkey -> {
            throw new IllegalStateException("boom");
        }, Runnable::run);

        failing.onKeyReleased(88);
        failing.onKeyReleased(80);
    }
}
