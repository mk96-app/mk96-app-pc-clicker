package com.markes96.pcclicker.adapter.in.ui.component;

import com.markes96.pcclicker.domain.control.Key;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/** Keyboard key drawn as a physical key cap, optionally followed by what it does. */
public final class KeyCap {

    private KeyCap() {
    }

    public static Label of(Key key) {
        final Label cap = Nodes.label(key.label(), "key-cap");
        cap.setMinWidth(Label.USE_PREF_SIZE);
        return cap;
    }

    public static HBox withCaption(Key key, String caption) {
        final HBox box = new HBox(6, of(key), Nodes.label(caption, "key-caption"));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}
