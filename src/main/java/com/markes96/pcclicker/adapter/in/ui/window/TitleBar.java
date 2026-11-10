package com.markes96.pcclicker.adapter.in.ui.window;

import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import com.markes96.pcclicker.adapter.in.ui.theme.Theme;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;

/** Custom window title bar: brand, drag handle and window buttons. */
final class TitleBar extends HBox {

    private static final double LOGO_SIZE = 22;

    private double dragOffsetX;
    private double dragOffsetY;

    TitleBar(Stage stage, String name, String version, String minimizeText, String closeText, Runnable onClose) {
        getStyleClass().add("title-bar");
        setAlignment(Pos.CENTER_LEFT);

        final ImageView logo = new ImageView(Theme.logo());
        logo.setFitWidth(LOGO_SIZE);
        logo.setFitHeight(LOGO_SIZE);
        logo.setSmooth(true);
        logo.setClip(new Circle(LOGO_SIZE / 2, LOGO_SIZE / 2, LOGO_SIZE / 2));

        final Button minimize = windowButton(MaterialDesignW.WINDOW_MINIMIZE, minimizeText);
        minimize.setOnAction(event -> stage.setIconified(true));
        final Button close = windowButton(MaterialDesignC.CLOSE, closeText);
        close.getStyleClass().add("window-close");
        close.setOnAction(event -> onClose.run());

        getChildren().addAll(
                logo,
                Nodes.label(name, "title-text"),
                Nodes.label("v" + version, "version-chip"),
                Nodes.spacer(),
                minimize,
                close);

        setOnMousePressed(event -> {
            dragOffsetX = stage.getX() - event.getScreenX();
            dragOffsetY = stage.getY() - event.getScreenY();
        });
        setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() + dragOffsetX);
            stage.setY(event.getScreenY() + dragOffsetY);
        });
    }

    private static Button windowButton(Ikon ikon, String text) {
        final Button button = new Button(null, Icons.of(ikon, 16));
        button.getStyleClass().add("window-button");
        button.setFocusTraversable(false);
        button.setTooltip(new Tooltip(text));
        button.setAccessibleText(text);
        return button;
    }
}
