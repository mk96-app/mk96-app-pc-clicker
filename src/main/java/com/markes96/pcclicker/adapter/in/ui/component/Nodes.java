package com.markes96.pcclicker.adapter.in.ui.component;

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Tiny builders that keep view code declarative. */
public final class Nodes {

    private Nodes() {
    }

    public static Label label(String text, String... styleClasses) {
        final Label label = new Label(text);
        label.getStyleClass().addAll(styleClasses);
        return label;
    }

    /** Wrapping label for paragraphs. */
    public static Label paragraph(String text, String... styleClasses) {
        final Label label = label(text, styleClasses);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    /** Grows to push its siblings apart in an {@link HBox} or a {@link VBox}. */
    public static Region spacer() {
        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public static <T extends Node> T styled(T node, String... styleClasses) {
        node.getStyleClass().addAll(styleClasses);
        return node;
    }

    /** Binds visibility and layout participation together, so hidden nodes leave no gap. */
    public static void showWhen(Node node, ObservableBooleanValue condition) {
        node.visibleProperty().bind(condition);
        node.managedProperty().bind(condition);
    }
}
