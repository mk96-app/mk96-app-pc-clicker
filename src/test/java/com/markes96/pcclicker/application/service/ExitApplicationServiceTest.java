package com.markes96.pcclicker.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExitApplicationServiceTest {

    @Test
    void stops_any_running_session_before_shutting_down() {
        final List<String> calls = new ArrayList<>();
        final ExitApplicationService service =
                new ExitApplicationService(() -> calls.add("stop"), () -> calls.add("shutdown"));

        service.exit();

        assertThat(calls).containsExactly("stop", "shutdown");
    }

    @Test
    void shuts_down_even_if_stopping_the_session_fails() {
        final List<String> calls = new ArrayList<>();
        final ExitApplicationService service = new ExitApplicationService(
                () -> {
                    throw new IllegalStateException("boom");
                },
                () -> calls.add("shutdown"));

        service.exit();

        assertThat(calls).containsExactly("shutdown");
    }
}
