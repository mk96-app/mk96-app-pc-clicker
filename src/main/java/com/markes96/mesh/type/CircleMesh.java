package com.markes96.mesh.type;

import java.awt.Point;
import com.markes96.mesh.Mesh;

public class CircleMesh extends Mesh {

  public CircleMesh(final Point centre, final int radius) {

    final int nPoints = (int) Math.round(super.pix2 * radius * super.nPointsRatio);
    final double radPerPoint = super.pix2 / nPoints;

    for (int nPoint = 0; nPoint <= nPoints; nPoint++) {

      final double angle = radPerPoint * nPoint;

      final int x = (int) Math.round(Math.cos(angle) * radius);
      final int y = (int) Math.round(Math.sin(angle) * radius);

      final Point ciclePoint = new Point(centre.x + x, centre.y + y);
      super.pointMesh.add(ciclePoint);
    }
  }
}
