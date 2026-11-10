package com.markes96.pcclicker.adapter.in.hotkey;

import com.markes96.pcclicker.domain.control.Key;
import java.util.Optional;

/** Translation between Windows virtual-key codes and platform-independent {@link Key}s. */
final class WindowsVirtualKeys {

    private static final int VK_ESCAPE = 0x1B;
    private static final int VK_A = 0x41;
    private static final int VK_Z = 0x5A;
    private static final int VK_F1 = 0x70;
    private static final int VK_F12 = 0x7B;

    private WindowsVirtualKeys() {
    }

    static Optional<Key> keyOf(int virtualKeyCode) {
        if (virtualKeyCode == VK_ESCAPE) {
            return Optional.of(Key.ESCAPE);
        }
        if (virtualKeyCode >= VK_A && virtualKeyCode <= VK_Z) {
            return Optional.of(Key.valueOf(String.valueOf((char) virtualKeyCode)));
        }
        if (virtualKeyCode >= VK_F1 && virtualKeyCode <= VK_F12) {
            return Optional.of(Key.valueOf("F" + (virtualKeyCode - VK_F1 + 1)));
        }
        return Optional.empty();
    }

    static int codeOf(Key key) {
        final String name = key.name();
        if (key == Key.ESCAPE) {
            return VK_ESCAPE;
        }
        if (name.length() == 1) {
            return name.charAt(0);
        }
        return VK_F1 + Integer.parseInt(name.substring(1)) - 1;
    }
}
