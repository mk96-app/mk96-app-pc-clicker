package com.markes96.pcclicker.adapter.in.ui;

import com.markes96.pcclicker.adapter.in.ui.format.DurationFormat;
import com.markes96.pcclicker.adapter.in.ui.format.SettingsDescriber;
import com.markes96.pcclicker.adapter.in.ui.hud.SessionHud;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.page.AboutPage;
import com.markes96.pcclicker.adapter.in.ui.page.HelpPage;
import com.markes96.pcclicker.adapter.in.ui.page.SessionPage;
import com.markes96.pcclicker.adapter.in.ui.page.SettingsPage;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ConfigurationViewModel;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.ProfilesViewModel;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.SessionOutcome;
import com.markes96.pcclicker.adapter.in.ui.window.MainWindow;
import com.markes96.pcclicker.adapter.in.ui.window.Notifier;
import com.markes96.pcclicker.adapter.in.ui.window.Page;
import com.markes96.pcclicker.adapter.in.ui.window.ToastLayer;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Objects;
import javafx.beans.Observable;
import javafx.stage.Stage;

/**
 * Entry point of the desktop user interface. Builds the windows and drives the flow between them:
 * the main window while idle, the floating panel during a session, and a notification with the
 * outcome when it ends.
 */
public final class DesktopUi {

    private static final Logger LOG = System.getLogger(DesktopUi.class.getName());

    private final UiContext context;
    private final Messages messages;
    private final ConfigurationViewModel form = new ConfigurationViewModel();
    private final SettingsDescriber describer;
    private final ProfilesViewModel profiles;

    private MainWindow window;

    public DesktopUi(UiContext context) {
        this.context = Objects.requireNonNull(context, "context");
        this.messages = context.messages();
        this.describer = new SettingsDescriber(messages);
        this.profiles = new ProfilesViewModel(context.profiles(), form, describer::presetName);
    }

    /** Builds and shows the interface. Must run on the JavaFX application thread. */
    public void start(Stage primaryStage) {
        rememberWorkspace();
        final Notifier notifier = (kind, title, detail) -> window.toasts().show(kind, title, detail);
        final List<Page> pages = List.of(
                new SessionPage(form, profiles, context, describer, MainWindow.visibleOnScreen(primaryStage), notifier,
                        this::startSession),
                new SettingsPage(context, describer, notifier),
                new HelpPage(context, describer),
                new AboutPage(context));
        window = new MainWindow(primaryStage, context.info(), messages, pages, context.exitApplication()::exit);
        final SessionHud hud = new SessionHud(context, describer);

        context.session().active().addListener((observable, wasActive, active) -> {
            if (active) {
                window.hide();
                if (context.showsPanel()) {
                    hud.show();
                }
            } else {
                hud.hide();
                window.show();
            }
        });
        context.session().onFinished(this::announce);
        profiles.onStorageFailure(failure -> storageFailed(failure, "error.saveProfiles"));
        context.preferences().onStorageFailure(failure -> storageFailed(failure, "error.savePreferences"));

        window.show();
    }

    /**
     * The form starts with the settings of the last run and hands every edit to the application,
     * which keeps them for the next launch and for the start hotkey.
     */
    private void rememberWorkspace() {
        form.load(context.workspace().workspace());
        for (Observable setting : form.settings()) {
            setting.addListener(observable -> context.workspace().updateWorkspace(form.toSettings()));
        }
    }

    private void storageFailed(RuntimeException failure, String titleKey) {
        LOG.log(Level.WARNING, "Settings could not be saved", failure);
        window.toasts().show(ToastLayer.Kind.DANGER, messages.get(titleKey), messages.get("error.saveDetail"));
    }

    private void startSession() {
        try {
            context.startSession().start(form.toConfiguration());
        } catch (RuntimeException failure) {
            LOG.log(Level.ERROR, "The session could not start", failure);
            window.toasts().show(ToastLayer.Kind.DANGER, messages.get("error.start"), failure.getMessage());
        }
    }

    private void announce(SessionOutcome outcome) {
        final ToastLayer.Kind kind = switch (outcome.reason()) {
            case TIME_LIMIT_REACHED, CLICK_LIMIT_REACHED -> ToastLayer.Kind.SUCCESS;
            case STOPPED_BY_USER -> ToastLayer.Kind.INFO;
            case FAILED -> ToastLayer.Kind.DANGER;
        };
        final String activeTime = DurationFormat.human(outcome.activeTime());
        final String detail = outcome.clicks() > 0
                ? messages.format("outcome.details", activeTime, messages.number(outcome.clicks()))
                : messages.format("outcome.detailsTime", activeTime);
        window.toasts().show(kind, messages.get("outcome." + outcome.reason().name()), detail);
    }
}
