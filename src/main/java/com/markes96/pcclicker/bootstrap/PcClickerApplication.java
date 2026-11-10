package com.markes96.pcclicker.bootstrap;

import com.markes96.pcclicker.adapter.in.ui.DesktopUi;
import com.markes96.pcclicker.adapter.in.ui.theme.Theme;
import com.markes96.pcclicker.adapter.out.system.AwtMouseDriver;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/** JavaFX lifecycle: builds the context on start and releases it on stop. */
public final class PcClickerApplication extends Application {

    private static final Logger LOG = System.getLogger(PcClickerApplication.class.getName());

    private ApplicationContext context;

    @Override
    public void start(Stage primaryStage) {
        Theme.install();
        // The main window is hidden during sessions; closing is always explicit.
        Platform.setImplicitExit(false);
        try {
            context = ApplicationContext.create(getHostServices()::showDocument);
        } catch (AwtMouseDriver.MouseUnavailableException unavailable) {
            LOG.log(Level.ERROR, "Cannot control the mouse", unavailable);
            final Alert alert = new Alert(Alert.AlertType.ERROR, unavailable.getMessage());
            alert.setHeaderText("MK96 PC Clicker");
            alert.showAndWait();
            Platform.exit();
            return;
        }
        new DesktopUi(context.uiContext()).start(primaryStage);
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }
}
