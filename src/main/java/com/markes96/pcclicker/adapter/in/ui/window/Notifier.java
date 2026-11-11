package com.markes96.pcclicker.adapter.in.ui.window;

/**
 * Shows a short notification to the user. Pages receive it before the window that displays the
 * notifications exists, so it is resolved when called.
 */
@FunctionalInterface
public interface Notifier {

    /** @param detail optional second line; {@code null} for none */
    void notify(ToastLayer.Kind kind, String title, String detail);
}
