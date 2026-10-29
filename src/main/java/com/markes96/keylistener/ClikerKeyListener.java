package com.markes96.keylistener;

import java.util.Objects;
import com.markes96.flag.Flags;
import com.markes96.frame.display.DisplayFrame;
import lc.kra.system.keyboard.event.GlobalKeyAdapter;
import lc.kra.system.keyboard.event.GlobalKeyEvent;

public class ClikerKeyListener extends GlobalKeyAdapter {

  DisplayFrame displayFrame;

  private ClikerKeyListener() {
    this.displayFrame = DisplayFrame.getInstance();
  }

  @Override
  public void keyReleased(final GlobalKeyEvent event) {

    if (event.getVirtualKeyCode() == 27) {
      System.exit(0);
    } else if (event.getVirtualKeyCode() == 88) {
      Flags.on = false;
    } else if (event.getVirtualKeyCode() == 80) {
      Flags.paused = !Flags.paused;
      this.displayFrame.checkIfsPaused();
      if (Flags.paused) {
        this.displayFrame.pause();
      } else {
        this.displayFrame.play();
      }
      this.displayFrame.actualize();
    }
  }

  private static ClikerKeyListener instance;

  public static ClikerKeyListener getInstance() {

    if (Objects.isNull(instance)) {
      instance = new ClikerKeyListener();
    }

    return instance;
  }
}
