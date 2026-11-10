package com.markes96.pcclicker.adapter.in.ui.theme;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;

/** Factory of vector icons; colour comes from CSS ({@code -fx-icon-color}) so icons follow the theme. */
public final class Icons {

    private Icons() {
    }

    public static FontIcon of(Ikon ikon, int size) {
        final FontIcon icon = new FontIcon(ikon);
        icon.setIconSize(size);
        icon.getStyleClass().add("icon");
        return icon;
    }
}
