package com.markes96.pcclicker.bootstrap;

import com.markes96.pcclicker.adapter.in.hotkey.GlobalHotkeyListener;
import com.markes96.pcclicker.adapter.in.ui.UiContext;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.PreferencesViewModel;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.SessionEventPresenter;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.SessionViewModel;
import com.markes96.pcclicker.adapter.out.persistence.PropertiesSettingsRepository;
import com.markes96.pcclicker.adapter.out.scheduling.ExecutorTaskScheduler;
import com.markes96.pcclicker.adapter.out.system.AwtMouseDriver;
import com.markes96.pcclicker.adapter.out.system.AwtScreenGeometry;
import com.markes96.pcclicker.application.port.out.ApplicationLifecycle;
import com.markes96.pcclicker.application.service.ExitApplicationService;
import com.markes96.pcclicker.application.service.HotkeyCommandHandler;
import com.markes96.pcclicker.application.service.SessionService;
import com.markes96.pcclicker.application.service.SessionTimings;
import com.markes96.pcclicker.application.service.SettingsService;
import com.markes96.pcclicker.domain.movement.MovementPathFactory;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Path;
import java.time.InstantSource;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.function.Consumer;
import javafx.application.Platform;

/**
 * Composition root: the only place that knows every concrete class. It wires adapters to ports
 * by hand (no framework needed at this size) and owns the resources that must be released.
 */
final class ApplicationContext implements AutoCloseable {

    /** System property to store the settings somewhere else, e.g. for a portable copy or for tests. */
    static final String SETTINGS_FILE_PROPERTY = "mk96.settingsFile";

    private static final Logger LOG = System.getLogger(ApplicationContext.class.getName());

    private final ExecutorTaskScheduler scheduler;
    private final SettingsService settings;
    private final GlobalHotkeyListener hotkeys;
    private final UiContext uiContext;

    private ApplicationContext(ExecutorTaskScheduler scheduler, SettingsService settings, GlobalHotkeyListener hotkeys,
                               UiContext uiContext) {
        this.scheduler = scheduler;
        this.settings = settings;
        this.hotkeys = hotkeys;
        this.uiContext = uiContext;
    }

    /**
     * @param browser opens URLs for the About page
     * @throws AwtMouseDriver.MouseUnavailableException if the system does not allow controlling the mouse
     */
    static ApplicationContext create(Consumer<String> browser) {
        final AwtMouseDriver mouse = AwtMouseDriver.create();
        final ExecutorTaskScheduler scheduler = new ExecutorTaskScheduler();
        final Path settingsFile = settingsFile();
        final SettingsService settings = SettingsService.load(new PropertiesSettingsRepository(settingsFile));
        final SessionViewModel sessionViewModel = new SessionViewModel();
        final SplittableRandom random = new SplittableRandom();

        final SessionService sessions = new SessionService(
                mouse,
                AwtScreenGeometry.ofSystem(),
                scheduler,
                InstantSource.system(),
                new SessionEventPresenter(sessionViewModel, Platform::runLater),
                new MovementPathFactory(random.split()),
                random.split(),
                SessionTimings.defaults(),
                () -> settings.preferences().countdown());
        final ApplicationLifecycle lifecycle = Platform::exit;
        final ExitApplicationService exit = new ExitApplicationService(sessions, lifecycle);
        final HotkeyCommandHandler hotkeyHandler =
                new HotkeyCommandHandler(settings, settings, sessions, sessions, sessions, sessions, exit);
        final GlobalHotkeyListener hotkeys = installHotkeys(hotkeyHandler).orElse(null);

        final UiContext uiContext = new UiContext(
                sessions,
                sessions,
                exit,
                settings,
                settings,
                hotkeyHandler,
                sessionViewModel,
                new PreferencesViewModel(settings),
                hotkeys != null,
                settingsFile.toAbsolutePath().toString(),
                ApplicationInfoLoader.load(),
                Messages.forDefaultLocale(),
                browser);
        return new ApplicationContext(scheduler, settings, hotkeys, uiContext);
    }

    UiContext uiContext() {
        return uiContext;
    }

    @Override
    public void close() {
        if (hotkeys != null) {
            hotkeys.close();
        }
        scheduler.close();
        settings.flush();
    }

    /**
     * Per-user location: {@code %APPDATA%\MK96 PC Clicker} on Windows, {@code ~/.mk96-pc-clicker}
     * elsewhere, unless {@value #SETTINGS_FILE_PROPERTY} says otherwise.
     */
    static Path settingsFile() {
        final String override = System.getProperty(SETTINGS_FILE_PROPERTY);
        if (override != null && !override.isBlank()) {
            return Path.of(override);
        }
        final String appData = System.getenv("APPDATA");
        final Path directory = appData != null && !appData.isBlank()
                ? Path.of(appData, "MK96 PC Clicker")
                : Path.of(System.getProperty("user.home"), ".mk96-pc-clicker");
        return directory.resolve("settings.properties");
    }

    private static Optional<GlobalHotkeyListener> installHotkeys(HotkeyCommandHandler handler) {
        try {
            return Optional.of(GlobalHotkeyListener.install(handler));
        } catch (UnsupportedOperationException unavailable) {
            LOG.log(Level.WARNING, "Global hotkeys are disabled", unavailable);
            return Optional.empty();
        }
    }
}
