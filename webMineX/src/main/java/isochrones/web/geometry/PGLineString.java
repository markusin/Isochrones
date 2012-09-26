/**
 * 
 */
package isochrones.web.geometry;

import isochrones.web.utils.PGUtil;
import isochrones.web.utils.SBAUtil;

import java.util.ArrayList;
import java.util.List;

import org.postgis.LineString;

/**
 * <p>
 * The <code>PGLineString</code> class
 * </p>
 * <p>
 * Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a>
 * </p>
 * <p>
 * Domenikanerplatz - Bozen, Italy.
 * </p>
 * <p>
 * Represents the postgres implementation of a line string
 * </p>
 * 
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class PGLineString extends AbstractLineString {

  LineString geometry;
  BBox bbox;

  /**
   * <p>
   * Constructs a(n) <code>PGLineString</code> object.
   * </p>
   * 
   * @param geometry
   */
  public PGLineString(LineString geometry) {
    this.geometry = geometry;
    points = new Point[geometry.getPoints().length];
    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
    for (int i = 0; i < geometry.getPoints().length; i++) {
      org.postgis.Point p = geometry.getPoints()[i];
      points[i] = new Point(p.getX(), p.getY());
      minX = Math.min(p.getX(), minX);
      minY = Math.min(p.getX(), minY);
      maxX = Math.max(p.getX(), maxX);
      maxY = Math.max(p.getX(), maxY);
    }
    bbox = new BBox(minX, minY, maxX, maxY);
  }

  @Override
  public AbstractLineString reverse() {
    return new PGLineString(geometry.reverse());
  }

  @Override
  public BBox getMBR() {
    return bbox;
  }
  
  @Override
  public AbstractLineString merge(AbstractLineString other) {
    int i = 0;
    int sum = other.numPoints() + this.numPoints();
    List<Point> mergedPoints = new ArrayList<Point>();

    int cThis = 0, cOther = 1;
    Point current = other.getFirstPoint();
    mergedPoints.add(current);
    for (i = 1; i < sum; i++) {
      double thisDist = cThis<this.numPoints() ? SBAUtil.eucideanDist(current, getPoint(cThis)) : Double.MAX_VALUE;
      double otherDist = cOther<other.numPoints() ? SBAUtil.eucideanDist(current, other.getPoint(cOther)) :  Double.MAX_VALUE;
      if (thisDist < otherDist) {
        current = getPoint(cThis++);
      } else if (thisDist > otherDist) {
        current = other.getPoint(cOther++);
      } else {
        current = getPoint(cThis++);
        cOther++;
        i++;
      }
      mergedPoints.add(current);
    }
    return new PGLineString(new LineString(PGUtil.asPGPoints(mergedPoints)));
  }

  public LineString getGeometry() {
    return geometry;
  }
  
}
