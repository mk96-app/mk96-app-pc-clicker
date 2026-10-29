package com.markes96.thread;

import java.awt.AWTException;
import java.awt.Robot;
import com.markes96.flag.Flags;
import com.markes96.frame.main.panel.MouseClickPanel;


public class MouseClickThread extends Thread {

  private final Robot robot;

  public MouseClickThread() throws AWTException {
    this.robot = new Robot();
  }

  @Override
  public void run() {
    try {
      final int period = 1000 / MouseClickPanel.getClickPerSec();
      while (Flags.on) {
        if (!Flags.paused) {
          this.robot.mousePress(16);
          this.robot.mouseRelease(16);
        }
        Thread.sleep(period);
      }
    } catch (final Exception e) {
      Flags.on = false;
    }
  }
}
