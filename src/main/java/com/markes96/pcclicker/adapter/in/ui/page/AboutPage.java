package com.markes96.pcclicker.adapter.in.ui.page;

import com.markes96.pcclicker.adapter.in.ui.ApplicationInfo;
import com.markes96.pcclicker.adapter.in.ui.UiContext;
import com.markes96.pcclicker.adapter.in.ui.component.Nodes;
import com.markes96.pcclicker.adapter.in.ui.component.PageHeader;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.theme.Icons;
import com.markes96.pcclicker.adapter.in.ui.theme.Theme;
import com.markes96.pcclicker.adapter.in.ui.window.Page;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignG;
import org.kordamp.ikonli.materialdesign2.MaterialDesignI;

/** Identity of the product: logo, version, author, build and credits. */
public final class AboutPage implements Page {

    private static final double LOGO_SIZE = 132;

    private final UiContext context;
    private final Messages messages;

    public AboutPage(UiContext context) {
        this.context = Objects.requireNonNull(context, "context");
        this.messages = context.messages();
    }

    @Override
    public String title() {
        return messages.get("nav.about");
    }

    @Override
    public Ikon icon() {
        return MaterialDesignI.INFORMATION_OUTLINE;
    }

    @Override
    public Node view() {
        final ApplicationInfo info = context.info();

        final ImageView logo = new ImageView(Theme.logo());
        logo.setFitWidth(LOGO_SIZE);
        logo.setFitHeight(LOGO_SIZE);
        logo.setSmooth(true);
        logo.setClip(new Circle(LOGO_SIZE / 2, LOGO_SIZE / 2, LOGO_SIZE / 2));
        final StackPane logoFrame = Nodes.styled(new StackPane(logo), "about-logo");

        final GridPane facts = new GridPane();
        facts.getStyleClass().add("about-facts");
        facts.setHgap(28);
        facts.setVgap(10);
        addFact(facts, 0, messages.get("about.version"), info.version());
        addFact(facts, 1, messages.get("about.author"), info.author());
        addFact(facts, 2, messages.get("about.buildDate"), info.buildDate());

        final Button repository = new Button(messages.get("about.openRepository"), Icons.of(MaterialDesignG.GITHUB, 18));
        repository.getStyleClass().add("secondary-button");
        repository.setOnAction(event -> context.browser().accept(info.repositoryUrl()));

        final VBox identity = new VBox(16,
                PageHeader.of(messages.get("about.eyebrow"), info.name(), messages.get("about.tagline")),
                facts,
                repository);
        final HBox hero = new HBox(36, logoFrame, identity);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("about-hero");

        final VBox credits = new VBox(8,
                Nodes.label(messages.get("about.credits.title"), "section-title"),
                Nodes.paragraph(messages.get("about.credits.text"), "step-text"));
        credits.getStyleClass().add("help-section");

        final VBox column = new VBox(24, hero, credits);
        column.getStyleClass().add("page-column");
        return column;
    }

    private static void addFact(GridPane grid, int row, String name, String value) {
        grid.add(Nodes.label(name, "fact-name"), 0, row);
        grid.add(Nodes.label(value, "fact-value"), 1, row);
    }
}
