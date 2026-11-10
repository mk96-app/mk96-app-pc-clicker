package com.markes96.pcclicker.adapter.in.ui.window;

import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import java.util.List;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;

/** Vertical navigation between pages, with the exit action anchored at the bottom. */
final class NavigationRail extends VBox {

    private final ToggleGroup group = new ToggleGroup();

    NavigationRail(List<Page> pages, Consumer<Page> onSelect, String exitText, Runnable onExit) {
        getStyleClass().add("nav-rail");
        setAlignment(Pos.TOP_CENTER);

        for (Page page : pages) {
            final ToggleButton item = new ToggleButton(page.title(), Icons.of(page.icon(), 22));
            item.getStyleClass().add("nav-item");
            item.setContentDisplay(ContentDisplay.TOP);
            item.setToggleGroup(group);
            item.setUserData(page);
            item.setTooltip(new Tooltip(page.title()));
            getChildren().add(item);
        }
        group.selectedToggleProperty().addListener((observable, previous, current) -> {
            if (current == null) {
                group.selectToggle(previous); // there is always a page on screen
            } else {
                onSelect.accept((Page) current.getUserData());
            }
        });

        final Button exit = new Button(exitText, Icons.of(MaterialDesignP.POWER, 20));
        exit.getStyleClass().addAll("nav-item", "nav-exit");
        exit.setContentDisplay(ContentDisplay.TOP);
        exit.setOnAction(event -> onExit.run());
        getChildren().addAll(Nodes.spacer(), exit);
    }

    void select(int index) {
        group.selectToggle(group.getToggles().get(index));
    }
}
