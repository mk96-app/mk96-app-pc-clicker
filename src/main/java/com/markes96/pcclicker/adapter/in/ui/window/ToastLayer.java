package com.markes96.pcclicker.adapter.in.ui.window;

import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.theme.Animations;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import java.util.Locale;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignI;

/** Transient notifications at the bottom of the window. Only one at a time; a new one replaces it. */
public final class ToastLayer extends StackPane {

    private static final Duration VISIBLE_FOR = Duration.seconds(5);

    /** Tone of a notification. */
    public enum Kind {
        SUCCESS(MaterialDesignC.CHECK_CIRCLE_OUTLINE),
        INFO(MaterialDesignI.INFORMATION_OUTLINE),
        DANGER(MaterialDesignA.ALERT_CIRCLE_OUTLINE);

        private final Ikon icon;

        Kind(Ikon icon) {
            this.icon = icon;
        }
    }

    private final PauseTransition autoHide = new PauseTransition(VISIBLE_FOR);

    ToastLayer() {
        getStyleClass().add("toast-layer");
        setAlignment(Pos.BOTTOM_CENTER);
        setPickOnBounds(false);
    }

    public void show(Kind kind, String title, String detail) {
        final VBox texts = new VBox(1, Nodes.label(title, "toast-title"));
        if (detail != null && !detail.isBlank()) {
            texts.getChildren().add(Nodes.label(detail, "toast-detail"));
        }
        final HBox toast = new HBox(12, Icons.of(kind.icon, 22), texts);
        toast.getStyleClass().addAll("toast", "toast-" + kind.name().toLowerCase(Locale.ROOT));
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setMaxWidth(USE_PREF_SIZE);
        toast.setMaxHeight(USE_PREF_SIZE);
        toast.setCursor(Cursor.HAND);
        toast.setOnMouseClicked(event -> dismiss(toast));

        getChildren().setAll(toast);
        Animations.enter(toast);
        autoHide.setOnFinished(event -> dismiss(toast));
        autoHide.playFromStart();
    }

    private void dismiss(HBox toast) {
        final FadeTransition fade = new FadeTransition(Animations.FAST, toast);
        fade.setToValue(0);
        fade.setOnFinished(event -> getChildren().remove(toast));
        fade.play();
    }
}
