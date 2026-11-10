package com.markes96.pcclicker.bootstrap;

import com.markes96.pcclicker.adapter.in.hotkey.GlobalHotkeyListener;
import com.markes96.pcclicker.adapter.in.ui.UiContext;
import com.markes96.pcclicker.adapter.in.ui.i18n.Messages;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.SessionEventPresenter;
import com.markes96.pcclicker.adapter.in.ui.viewmodel.SessionViewModel;
import com.markes96.pcclicker.adapter.out.scheduling.ExecutorTaskScheduler;
import com.markes96.pcclicker.adapter.out.system.AwtMouseDriver;
import com.markes96.pcclicker.adapter.out.system.AwtScreenGeometry;
import com.markes96.pcclicker.application.port.out.ApplicationLifecycle;
import com.markes96.pcclicker.application.service.ExitApplicationService;
import com.markes96.pcclicker.application.service.HotkeyCommandHandler;
import com.markes96.pcclicker.application.service.SessionService;
import com.markes96.pcclicker.application.service.SessionTimings;
import com.markes96.pcclicker.domain.control.KeyBindings;
import com.markes96.pcclicker.domain.movement.MovementPathFactory;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
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

    private static final Logger LOG = System.getLogger(ApplicationContext.class.getName());

    private final ExecutorTaskScheduler scheduler;
    private final GlobalHotkeyListener hotkeys;
    private final UiContext uiContext;

    private ApplicationContext(ExecutorTaskScheduler scheduler, GlobalHotkeyListener hotkeys, UiContext uiContext) {
        this.scheduler = scheduler;
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
        final SessionTimings timings = SessionTimings.defaults();
        final KeyBindings keyBindings = KeyBindings.defaults();
        final SessionViewModel sessionViewModel = new SessionViewModel();

        final SessionService sessions = new SessionService(
                mouse,
                AwtScreenGeometry.ofSystem(),
                scheduler,
                InstantSource.system(),
                new SessionEventPresenter(sessionViewModel, Platform::runLater),
                new MovementPathFactory(new SplittableRandom()),
                timings);
        final ApplicationLifecycle lifecycle = Platform::exit;
        final ExitApplicationService exit = new ExitApplicationService(sessions, lifecycle);
        final GlobalHotkeyListener hotkeys = installHotkeys(
                keyBindings, new HotkeyCommandHandler(sessions, sessions, sessions, exit)).orElse(null);

        final UiContext uiContext = new UiContext(
                sessions,
                sessions,
                exit,
                sessionViewModel,
                keyBindings,
                timings.countdownSeconds(),
                hotkeys != null,
                ApplicationInfoLoader.load(),
                Messages.forDefaultLocale(),
                browser);
        return new ApplicationContext(scheduler, hotkeys, uiContext);
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
    }

    private static Optional<GlobalHotkeyListener> installHotkeys(KeyBindings bindings, HotkeyCommandHandler handler) {
        try {
            return Optional.of(GlobalHotkeyListener.install(bindings, handler));
        } catch (UnsupportedOperationException unavailable) {
            LOG.log(Level.WARNING, "Global hotkeys are disabled", unavailable);
            return Optional.empty();
        }
    }
}
