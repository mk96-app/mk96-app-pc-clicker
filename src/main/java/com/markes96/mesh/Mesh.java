package com.markes96.mesh;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class Mesh {

  protected final double pix2 = 2 * Math.PI;
  protected final double nPointsRatio = 0.5;

  private int autoIndex = 0;
  protected final List<Point> pointMesh = new ArrayList<>();

  public List<Point> getPointMesh() {
    return this.pointMesh;
  }

  public int getSize() {
    return this.pointMesh.size();
  }

  public Point getPoint(final int index) {
    return this.pointMesh.get(index);
  }

  public Point getNextPoint() {
    final Point point = this.pointMesh.get(this.autoIndex);
    final boolean isLastPoint = this.autoIndex == (this.getSize() - 1);
    this.autoIndex = (isLastPoint) ? 0 : (this.autoIndex + 1);
    return point;
  }
}
