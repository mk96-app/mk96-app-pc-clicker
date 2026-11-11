package com.markes96.pcclicker.adapter.out.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.markes96.pcclicker.domain.configuration.MouseButton;
import java.awt.event.InputEvent;
import org.junit.jupiter.api.Test;

class AwtMouseDriverTest {

    @Test
    void presses_the_awt_button_matching_each_mouse_button() {
        assertThat(AwtMouseDriver.maskOf(MouseButton.LEFT)).isEqualTo(InputEvent.BUTTON1_DOWN_MASK);
        assertThat(AwtMouseDriver.maskOf(MouseButton.MIDDLE)).isEqualTo(InputEvent.BUTTON2_DOWN_MASK);
        assertThat(AwtMouseDriver.maskOf(MouseButton.RIGHT)).isEqualTo(InputEvent.BUTTON3_DOWN_MASK);
    }
}
