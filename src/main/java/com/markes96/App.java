package com.markes96;

import java.awt.AWTException;
import java.time.Duration;
import com.markes96.flag.Flags;
import com.markes96.frame.display.DisplayFrame;
import com.markes96.frame.main.MainFrame;
import com.markes96.frame.main.panel.MouseClickPanel;
import com.markes96.frame.main.panel.MoveMousePanel;
import com.markes96.frame.main.panel.TimePanel;
import com.markes96.frame.wait.PlaceMouseFrame;
import com.markes96.keylistener.ClikerKeyListener;
import com.markes96.thread.MouseClickThread;
import com.markes96.thread.MouseMoveThread;
import com.markes96.utils.FrameUtils;
import lc.kra.system.keyboard.GlobalKeyboardHook;

public class App {

  private static MainFrame mainFrame;
  private static PlaceMouseFrame placeMouseFrame;
  private static DisplayFrame displayFrame;

  private static ClikerKeyListener clikerKeyListener;
  private static GlobalKeyboardHook keyboardHook;

  public static void main(final String[] args) throws Exception {

    mainFrame = MainFrame.getInstance();
    placeMouseFrame = PlaceMouseFrame.getInstance();
    displayFrame = DisplayFrame.getInstance();

    clikerKeyListener = ClikerKeyListener.getInstance();

    while (true) {

      if (Flags.on) {
        initClicker();
        placeMouse();
        runClicker();
        while (Flags.on && isOverTimed()) {
          if (!Flags.paused) {
            displayFrame.actualize();
          }
          Thread.sleep(500);
        }
        reset();
      }

      Thread.sleep(1000);
    }
  }

  private static boolean isOverTimed() {

    if ((TimePanel.getInputTime() <= -1) || Flags.paused) {
      return true;
    }

    final Duration actualDuration = FrameUtils.getDuration(displayFrame.getInitInstant());
    final Duration maxDuration = Duration.ofMinutes(TimePanel.getInputTime());

    System.out.println(actualDuration);
    System.out.println(maxDuration);
    System.out.println(actualDuration.compareTo(maxDuration));
    System.out.println();

    return actualDuration.compareTo(maxDuration) < 0;
  }

  private static void initClicker() {
    Flags.paused = false;
    mainFrame.dispose();
    keyboardHook = new GlobalKeyboardHook(true);
    keyboardHook.addKeyListener(clikerKeyListener);
  }

  private static void placeMouse() throws Exception {
    placeMouseFrame.setVisible(true);
    for (int i = 3; i >= 0; i--) {
      placeMouseFrame.setTime(i);
      Thread.sleep(1000);
    }
    placeMouseFrame.dispose();
  }

  private static void runClicker() {
    displayFrame.init();

    try {
      if (MoveMousePanel.isMoveMouseChecked()) {
        new MouseMoveThread().start();
      }

      if (MouseClickPanel.isMouseClickChecked()) {
        new MouseClickThread().start();
      }

    } catch (final AWTException e) {
      Flags.on = false;
    }
  }

  private static void reset() {
    Flags.on = false;
    Flags.paused = false;
    mainFrame.setVisible(true);
    displayFrame.dispose();
    keyboardHook.removeKeyListener(clikerKeyListener);
    keyboardHook.shutdownHook();
  }
}
