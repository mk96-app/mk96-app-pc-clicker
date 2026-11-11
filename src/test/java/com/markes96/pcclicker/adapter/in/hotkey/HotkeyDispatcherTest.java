package com.markes96.pcclicker.adapter.in.hotkey;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.control.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;

class HotkeyDispatcherTest {

    private final List<Key> handled = new ArrayList<>();
    private final List<Runnable> queued = new ArrayList<>();
    private final Executor queueingExecutor = queued::add;

    private final HotkeyDispatcher dispatcher = new HotkeyDispatcher(handled::add, queueingExecutor);

    @Test
    void forwards_known_keys_to_the_use_case_off_the_hook_thread() {
        dispatcher.onKeyReleased(88);

        assertThat(handled).isEmpty();
        queued.forEach(Runnable::run);
        assertThat(handled).containsExactly(Key.X);
    }

    @Test
    void translates_letters_function_keys_and_escape() {
        dispatcher.onKeyReleased(27);
        dispatcher.onKeyReleased(80);
        dispatcher.onKeyReleased(0x75);
        queued.forEach(Runnable::run);

        assertThat(handled).containsExactly(Key.ESCAPE, Key.P, Key.F6);
    }

    @Test
    void ignores_keys_that_can_never_be_a_hotkey() {
        dispatcher.onKeyReleased(13);
        dispatcher.onKeyReleased(0x20);

        assertThat(queued).isEmpty();
    }

    @Test
    void a_failing_use_case_does_not_break_the_dispatcher() {
        final HotkeyDispatcher failing = new HotkeyDispatcher(key -> {
            throw new IllegalStateException("boom");
        }, Runnable::run);

        failing.onKeyReleased(88);
        failing.onKeyReleased(80);
    }
}
