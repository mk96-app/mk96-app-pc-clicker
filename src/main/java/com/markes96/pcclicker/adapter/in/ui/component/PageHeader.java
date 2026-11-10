package com.markes96.pcclicker.adapter.in.ui.component;

import javafx.scene.layout.VBox;

/** Eyebrow, title and lead paragraph shared by every page, so all of them share one rhythm. */
public final class PageHeader {

    private PageHeader() {
    }

    public static VBox of(String eyebrow, String title, String subtitle) {
        final VBox header = new VBox(4,
                Nodes.label(eyebrow, "page-eyebrow"),
                Nodes.label(title, "page-title"),
                Nodes.paragraph(subtitle, "page-subtitle"));
        header.getStyleClass().add("page-header");
        return header;
    }
}
