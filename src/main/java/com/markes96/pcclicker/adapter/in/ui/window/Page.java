package com.markes96.pcclicker.adapter.in.ui.window;

import javafx.scene.Node;
import org.kordamp.ikonli.Ikon;

/**
 * A destination of the navigation rail. Adding a feature with its own screen (profiles, history,
 * settings...) means implementing this interface and registering it; the shell adapts by itself.
 */
public interface Page {

    String title();

    Ikon icon();

    /** The page content. Called once; the same node is reused every time the page is shown. */
    Node view();
}
