package com.markes96.pcclicker.adapter.in.ui.component;

import com.markes96.pcclicker.domain.control.Key;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Keyboard key drawn as a physical key cap, optionally followed by what it does. Caps follow an
 * observable key, so they update as soon as a hotkey is rebound.
 */
public final class KeyCap {

    private KeyCap() {
    }

    public static Label of(ObservableValue<Key> key) {
        final Label cap = Nodes.label("", "key-cap");
        cap.textProperty().bind(Bindings.createStringBinding(
                () -> key.getValue() == null ? "" : key.getValue().label(), key));
        cap.setMinWidth(Label.USE_PREF_SIZE);
        return cap;
    }

    public static HBox withCaption(ObservableValue<Key> key, String caption) {
        final HBox box = new HBox(6, of(key), Nodes.label(caption, "key-caption"));
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}
