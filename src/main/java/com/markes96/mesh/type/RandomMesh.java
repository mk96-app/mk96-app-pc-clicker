package com.markes96.mesh.type;

import java.awt.Dimension;
import java.awt.Point;
import com.markes96.mesh.Mesh;

public class RandomMesh extends Mesh {

  private final Dimension screenSize;
  private final double pixelSpeed = 5;
  private final double maxRandomAngle = super.pix2 / 8;

  private Point actualPoint;
  private int pixelX;
  private int pixelY;

  public RandomMesh(final Dimension screenSize, final Point actualPoint) {
    this.screenSize = screenSize;
    this.actualPoint = actualPoint;

    final double angle = super.pix2 * Math.random();
    this.calculePixelXY(angle);
  }

  @Override
  public Point getNextPoint() {

    int x = this.actualPoint.x + this.pixelX;
    int y = this.actualPoint.y + this.pixelY;
    this.calculePixelXY(this.getAngle() + this.getRangomSmallAngle());

    if ((x >= this.screenSize.width) || (x <= 0)) {
      this.pixelX *= -1;
      x = (x <= 0) ? 0 : this.screenSize.width;
    }

    if ((y >= this.screenSize.height) || (y <= 0)) {
      this.pixelY *= -1;
      y = (y <= 0) ? 0 : this.screenSize.height;
    }

    this.actualPoint = new Point(x, y);
    return this.actualPoint;
  }

  private double getRangomSmallAngle() {
    return (this.maxRandomAngle * Math.random()) * (2 * (Math.random() - 0.5));
  }

  private double getAngle() {
    return Math.atan2(this.pixelY, this.pixelX);
  }

  private void calculePixelXY(final double angle) {
    this.pixelX = (int) Math.round(Math.cos(angle) * this.pixelSpeed);
    this.pixelY = (int) Math.round(Math.sin(angle) * this.pixelSpeed);
  }

}
