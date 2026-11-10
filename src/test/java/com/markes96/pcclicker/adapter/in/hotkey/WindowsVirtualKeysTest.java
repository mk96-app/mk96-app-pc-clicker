package com.markes96.pcclicker.adapter.in.hotkey;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.control.Key;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class WindowsVirtualKeysTest {

    @ParameterizedTest
    @CsvSource({"27,ESCAPE", "65,A", "80,P", "88,X", "90,Z", "112,F1", "123,F12"})
    void translates_virtual_key_codes(int code, Key expected) {
        assertThat(WindowsVirtualKeys.keyOf(code)).contains(expected);
    }

    @ParameterizedTest
    @CsvSource({"0", "13", "48", "64", "91", "124", "255"})
    void ignores_keys_that_cannot_be_bound(int code) {
        assertThat(WindowsVirtualKeys.keyOf(code)).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(Key.class)
    void every_bindable_key_has_a_virtual_key_code(Key key) {
        assertThat(WindowsVirtualKeys.keyOf(WindowsVirtualKeys.codeOf(key))).contains(key);
    }

    @Test
    void escape_is_code_27() {
        assertThat(WindowsVirtualKeys.codeOf(Key.ESCAPE)).isEqualTo(27);
    }
}
