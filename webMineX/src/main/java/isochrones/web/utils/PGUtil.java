/**
 * 
 */
package isochrones.web.utils;

import isochrones.web.coverage.IsoEdge;
import isochrones.web.geometry.PGLineString;
import isochrones.web.geometry.Point;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * The <code>PGUtil</code> class
 * </p>
 * <p>
 * Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a>
 * </p>
 * <p>
 * Domenikanerplatz - Bozen, Italy.
 * </p>
 * <p>
 * </p>
 * 
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class PGUtil {

  /**
   * @param points
   * @return
   */
  public static org.postgis.Point[] asPGPoints(Point[] points) {
    org.postgis.Point[] pgPts = new org.postgis.Point[points.length];
    for (int i = 0; i < points.length; i++) {
      pgPts[i] = new org.postgis.Point(points[i].getX(), points[i].getY());
    }
    return pgPts;
  }
  
  /**
   * 
   * <p>Method asPGPoints</p>
   * @param points
   * @return
   */
  public static org.postgis.Point[] asPGPoints(List<Point> pts) {
    Set<Point> points = new HashSet<Point>(pts);
    org.postgis.Point[] pgPts = new org.postgis.Point[points.size()];
    int i = 0;
    for (Point point : points) {
      pgPts[i++] = new org.postgis.Point(point.getX(), point.getY());
    }
    return pgPts;
  }
  
  /**
   * 
   * <p>Method asPGLineString</p>
   * @param edges
   * @return
   */
  public static org.postgis.LineString[] asPGLineString(List<IsoEdge> edges) {
    org.postgis.LineString[] lines  = new org.postgis.LineString[edges.size()];
    int i = 0;
    for (IsoEdge edge : edges) {
      lines[i++] = ((PGLineString)edge.getGeometry()).getGeometry();
    }
    return lines;
  }

}
