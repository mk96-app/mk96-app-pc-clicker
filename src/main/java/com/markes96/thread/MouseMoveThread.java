package com.markes96.thread;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import com.markes96.flag.Flags;
import com.markes96.frame.main.panel.MoveMousePanel;
import com.markes96.mesh.Mesh;
import com.markes96.mesh.type.CircleMesh;
import com.markes96.mesh.type.PolygonMesh;
import com.markes96.mesh.type.RandomMesh;
import com.markes96.mesh.type.ReboundMesh;

public class MouseMoveThread extends Thread {

  private final Robot robot;

  public MouseMoveThread() throws AWTException {
    this.robot = new Robot();
  }

  private final int periodms = 20;

  @Override
  public void run() {

    try {
      final Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Mesh mesh = new Mesh() {};

      switch (MoveMousePanel.getMoveType()) {
        case RANDOM:
          mesh = new RandomMesh(screenSize, mouseLocation);
          break;
        case CIRCLE:
          mesh = new CircleMesh(mouseLocation, MoveMousePanel.getRadius());
          break;
        case POLYGON:
          mesh = new PolygonMesh(mouseLocation, MoveMousePanel.getNVertex(),
              MoveMousePanel.getRadius());
          break;
        case REBOUND:
          mesh = new ReboundMesh(screenSize, mouseLocation);
          break;
      }

      while (Flags.on) {
        if (!Flags.paused) {
          final Point point = mesh.getNextPoint();
          this.robot.mouseMove(point.x, point.y);
        }
        Thread.sleep(this.periodms);
      }
    } catch (final Exception e) {
      Flags.on = false;
    }
  }
}
