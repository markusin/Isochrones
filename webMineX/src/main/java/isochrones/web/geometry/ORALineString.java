/**
 * 
 */
package isochrones.web.geometry;

import isochrones.web.utils.ORAUtil;
import isochrones.web.utils.SBAUtil;

import java.util.ArrayList;
import java.util.List;

import oracle.spatial.geometry.JGeometry;

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
public class ORALineString extends AbstractLineString {

  JGeometry geometry;

  /**
   * <p>
   * Constructs a(n) <code>ORALineString</code> object.
   * </p>
   * 
   * @param geometry
   */
  public ORALineString(JGeometry geometry) {
    this.geometry = geometry;
    points = new Point[geometry.getNumPoints()];
    double[] ordinatesArray = geometry.getOrdinatesArray();
    int j=0;
    for (int i = 0; i < geometry.getNumPoints(); i++) {
      points[i] = new Point(ordinatesArray[j++], ordinatesArray[j++]);
    }
  }

  @Override
  public AbstractLineString reverse() {
    double[] ordinates = geometry.getOrdinatesArray();
    double[] reversedOrdinates = new double[ordinates.length];
    int i = 0;
    int dimensions = geometry.getDimensions();
    for (int j = ordinates.length - dimensions; j >= 0; j -= dimensions, i += dimensions) {
      reversedOrdinates[i] = ordinates[j];
      reversedOrdinates[i + 1] = ordinates[j + 1];
    }
    return new ORALineString(JGeometry.createLinearLineString(reversedOrdinates, geometry.getDimensions(), geometry.getSRID()));
  }

  @Override
  public BBox getMBR() {
    double[] mbr = geometry.getMBR();
    return new BBox(mbr[0], mbr[1], mbr[2], mbr[3]);
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
    return new ORALineString(JGeometry.createLinearLineString(ORAUtil.asOrdinates(mergedPoints), 2, geometry.getSRID()));
  }

}
