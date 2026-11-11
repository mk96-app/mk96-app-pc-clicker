package com.markes96.pcclicker.adapter.in.ui.component;

import atlantafx.base.controls.ToggleSwitch;
import com.markes96.pcclicker.adapter.in.ui.theme.Animations;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import java.util.Locale;
import javafx.beans.property.BooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.Ikon;

/**
 * Card that represents one capability of a session: an icon, a title, a short explanation and a
 * switch. Its settings unfold only while it is on. New capabilities are added to the app as new
 * cards, so the layout scales without redesign.
 */
public final class FeatureCard extends VBox {

    private static final PseudoClass ON = PseudoClass.getPseudoClass("on");

    /** Colour family of a card, mapped to the logo palette in CSS. */
    public enum Accent {
        VIOLET, BLUE, YELLOW, GREEN;

        String styleClass() {
            return "accent-" + name().toLowerCase(Locale.ROOT);
        }
    }

    public FeatureCard(Accent accent, Ikon ikon, String title, String description, BooleanProperty enabled, Node body) {
        getStyleClass().addAll("feature-card", accent.styleClass());

        final StackPane badge = Nodes.styled(new StackPane(Icons.of(ikon, 20)), "feature-badge");
        final Label titleLabel = Nodes.label(title, "feature-title");
        final Label descriptionLabel = Nodes.paragraph(description, "feature-description");
        final VBox texts = new VBox(2, titleLabel, descriptionLabel);
        HBox.setHgrow(texts, Priority.ALWAYS);

        final ToggleSwitch toggle = new ToggleSwitch();
        toggle.selectedProperty().bindBidirectional(enabled);
        toggle.setAccessibleText(title);

        final HBox header = new HBox(14, badge, texts, toggle);
        header.getStyleClass().add("feature-header");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setCursor(Cursor.HAND);
        header.setOnMouseClicked(event -> {
            if (!toggle.isHover()) {
                enabled.set(!enabled.get());
            }
        });

        final VBox bodyWrapper = new VBox(body);
        bodyWrapper.getStyleClass().add("feature-body");
        Nodes.showWhen(bodyWrapper, enabled);

        pseudoClassStateChanged(ON, enabled.get());
        enabled.addListener((observable, wasOn, on) -> {
            pseudoClassStateChanged(ON, on);
            if (on) {
                Animations.enter(bodyWrapper);
            }
        });

        getChildren().addAll(header, bodyWrapper);
    }

    /** A labelled row inside a card body: name and hint on the left, control on the right. */
    public static HBox field(String name, String hint, Node control) {
        final VBox texts = new VBox(1, Nodes.label(name, "field-name"));
        if (hint != null) {
            texts.getChildren().add(Nodes.paragraph(hint, "field-hint")); // wraps instead of being cut
        }
        HBox.setHgrow(texts, Priority.ALWAYS);
        if (control instanceof Region region) {
            region.setMinWidth(Region.USE_PREF_SIZE); // the control keeps its size; the texts give way
        }
        final HBox row = new HBox(12, texts, control);
        row.getStyleClass().add("field-row");
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }
}
