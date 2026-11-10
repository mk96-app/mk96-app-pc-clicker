package com.markes96.pcclicker.adapter.in.ui.theme;

import atlantafx.base.theme.PrimerDark;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Visual identity of the application: the AtlantaFX Primer Dark base re-coloured with the palette
 * of the MK96 logo (night indigo, violet ring, neon green, blue and star yellow), plus bundled fonts.
 */
public final class Theme {

    private static final Logger LOG = System.getLogger(Theme.class.getName());

    private static final String BASE = "/com/markes96/pcclicker/ui/";
    private static final String STYLESHEET = BASE + "css/app.css";
    private static final String LOGO = BASE + "images/logo.png";
    private static final List<String> FONTS = List.of(
            "Inter-400.ttf", "Inter-500.ttf", "Inter-600.ttf", "Inter-700.ttf",
            "JetBrainsMono-500.ttf", "JetBrainsMono-700.ttf");

    /** Palette, mirrored from {@code app.css} for the few things painted from code (canvas previews). */
    public static final Color VIOLET = Color.web("#A848F0");
    public static final Color VIOLET_BRIGHT = Color.web("#C88BFF");
    public static final Color GREEN = Color.web("#8CFC02");
    public static final Color BLUE = Color.web("#3D7BFF");
    public static final Color YELLOW = Color.web("#FEE532");
    public static final Color GRID = Color.web("#2A1F52");
    public static final Color TEXT_SUBTLE = Color.web("#7A6DA6");

    private Theme() {
    }

    /** Loads fonts and the base stylesheet. Call once on the JavaFX thread before building any scene. */
    public static void install() {
        FONTS.forEach(Theme::loadFont);
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
    }

    /** Applies the application stylesheet to a transparent-window scene. */
    public static void style(Scene scene) {
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(resource(STYLESHEET).toExternalForm());
    }

    public static Image logo() {
        return new Image(resource(LOGO).toExternalForm(), true);
    }

    private static void loadFont(String file) {
        try (InputStream in = Theme.class.getResourceAsStream(BASE + "fonts/" + file)) {
            if (in == null || Font.loadFont(in, 13) == null) {
                LOG.log(Level.WARNING, "Font {0} could not be loaded; falling back to system fonts", file);
            }
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }

    private static URL resource(String path) {
        return Objects.requireNonNull(Theme.class.getResource(path), () -> "Missing resource " + path);
    }
}
