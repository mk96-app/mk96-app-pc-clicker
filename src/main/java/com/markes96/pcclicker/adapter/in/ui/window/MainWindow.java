package com.markes96.pcclicker.adapter.in.ui.window;

import com.markes96.pcclicker.adapter.in.ui.ApplicationInfo;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.theme.Animations;
import com.markes96.pcclicker.adapter.in.ui.theme.Theme;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Frameless main window with rounded corners and a soft shadow: title bar on top, navigation rail
 * on the left, the selected page in the centre and notifications floating above it.
 */
public final class MainWindow {

    private static final double WIDTH = 1060;
    private static final double HEIGHT = 720;
    private static final double SHADOW_SPACE = 18;
    private static final double CORNER_RADIUS = 16;

    private final Stage stage;
    private final StackPane pageHost = new StackPane();
    private final ToastLayer toasts = new ToastLayer();
    private final Map<Page, Node> views = new HashMap<>();

    public MainWindow(Stage stage, ApplicationInfo info, Messages messages, List<Page> pages, Runnable onExit) {
        if (pages.isEmpty()) {
            throw new IllegalArgumentException("The window needs at least one page");
        }
        this.stage = stage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle(info.name());
        stage.getIcons().add(Theme.logo());
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> {
            event.consume(); // Alt+F4 and the taskbar go through the same exit path as the button
            onExit.run();
        });

        final TitleBar titleBar = new TitleBar(stage, info.name(), info.version(),
                messages.get("window.minimize"), messages.get("window.close"), onExit);
        final NavigationRail rail = new NavigationRail(pages, this::showPage, messages.get("nav.exit"), onExit);

        pageHost.getStyleClass().add("page-host");
        final StackPane center = new StackPane(pageHost, toasts);

        final BorderPane window = new BorderPane(center, titleBar, null, null, rail);
        window.getStyleClass().add("window");
        final Rectangle clip = new Rectangle();
        clip.setArcWidth(CORNER_RADIUS * 2);
        clip.setArcHeight(CORNER_RADIUS * 2);
        clip.widthProperty().bind(window.widthProperty());
        clip.heightProperty().bind(window.heightProperty());
        window.setClip(clip);

        // The shadow lives on a parent: the rounded clip of the window would cut it off.
        final StackPane shadow = new StackPane(window);
        shadow.getStyleClass().add("window-shadow");
        final StackPane frame = new StackPane(shadow);
        frame.getStyleClass().add("window-frame");
        frame.setStyle("-fx-padding: " + SHADOW_SPACE + ";");

        final Scene scene = new Scene(frame, WIDTH + SHADOW_SPACE * 2, HEIGHT + SHADOW_SPACE * 2);
        Theme.style(scene);
        stage.setScene(scene);

        rail.select(0);
    }

    public void show() {
        stage.show();
        stage.toFront();
    }

    public void hide() {
        stage.hide();
    }

    public ToastLayer toasts() {
        return toasts;
    }

    /** True while {@code stage} is on screen and not minimised; used to pause animations nobody sees. */
    public static BooleanBinding visibleOnScreen(Stage stage) {
        return Bindings.createBooleanBinding(
                () -> stage.isShowing() && !stage.isIconified(), stage.showingProperty(), stage.iconifiedProperty());
    }

    private void showPage(Page page) {
        final Node view = views.computeIfAbsent(page, Page::view);
        pageHost.getChildren().setAll(view);
        Animations.enter(view);
    }
}
