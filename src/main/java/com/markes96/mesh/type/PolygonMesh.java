package com.markes96.mesh.type;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import com.markes96.mesh.Mesh;

public class PolygonMesh extends Mesh {

  private final double SQUARE_ANGLE_OFFSET = -Math.PI / 4;

  private final double EVEN_ANGLE_OFFSET = 0.0;
  private final double ODD_ANGLE_OFFSET = -Math.PI / 2;

  public PolygonMesh(final Point centre, final int vertexNum, final int radius) {

    final double radPerVertex = super.pix2 / vertexNum;

    final double angleOffSet = (vertexNum == 4) ? this.SQUARE_ANGLE_OFFSET
        : ((vertexNum % 2) == 0) ? this.EVEN_ANGLE_OFFSET : this.ODD_ANGLE_OFFSET;

    final List<Point> vertices = new ArrayList<>();
    for (int vertex = 0; vertex <= vertexNum; vertex++) {

      final double angle = (radPerVertex * vertex) + angleOffSet;
      final int vertexX = (int) Math.round(Math.cos(angle) * radius);
      final int vertexY = (int) Math.round(Math.sin(angle) * radius);
      final Point vertexPoint = new Point(centre.x + vertexX, centre.y + vertexY);
      vertices.add(vertexPoint);

      if (vertex != 0) {
        final Point pointSrc = vertices.get(vertex - 1);
        final Point pointDst = vertices.get(vertex != vertexNum ? vertex : 0);
        this.generateMeshLine(pointSrc, pointDst);
      }
    }
  }

  private void generateMeshLine(final Point src, final Point dst) {

    final Point relativePoint = this.getAbsolutePoint(src, dst);
    final double module = this.module(relativePoint);
    final int nPoints = (int) Math.round(module * super.nPointsRatio);

    final double pixelPerPointX = relativePoint.getX() / nPoints;
    final double pixelPerPointY = relativePoint.getY() / nPoints;

    for (int nPoint = 0; nPoint < nPoints; nPoint++) {
      final int pixelX = (int) Math.round(pixelPerPointX * nPoint);
      final int pixelY = (int) Math.round(pixelPerPointY * nPoint);
      super.pointMesh.add(new Point(src.x + pixelX, src.y + pixelY));
    }
  }

  private Point getAbsolutePoint(final Point src, final Point dst) {
    return new Point(dst.x - src.x, dst.y - src.y);
  }

  public double module(final Point point) {
    return Math.sqrt(Math.pow(point.x, 2.0D) + Math.pow(point.y, 2.0D));
  }
}
