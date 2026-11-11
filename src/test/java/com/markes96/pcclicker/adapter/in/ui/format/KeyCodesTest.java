package com.markes96.pcclicker.adapter.in.ui.format;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.control.Key;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class KeyCodesTest {

    @ParameterizedTest
    @EnumSource(Key.class)
    void every_bindable_key_can_be_typed(Key key) {
        assertThat(KeyCodes.keyOf(KeyCode.valueOf(key.name()))).contains(key);
    }

    @Test
    void other_keys_cannot_be_bound() {
        assertThat(KeyCodes.keyOf(KeyCode.ENTER)).isEmpty();
        assertThat(KeyCodes.keyOf(KeyCode.DIGIT1)).isEmpty();
        assertThat(KeyCodes.keyOf(KeyCode.F13)).isEmpty();
        assertThat(KeyCodes.keyOf(KeyCode.SHIFT)).isEmpty();
        assertThat(KeyCodes.keyOf(null)).isEmpty();
    }
}
