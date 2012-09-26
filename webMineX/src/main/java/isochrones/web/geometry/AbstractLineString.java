package isochrones.web.geometry;


import java.util.Iterator;

public abstract class AbstractLineString implements Comparable<AbstractLineString> {

  protected Point[] points;


  /**
   * <p>
   * Method reverse
   * </p>
   * 
   * @return
   */
  public abstract AbstractLineString reverse();
  
  /**
   * <p>
   * Method merge
   * </p>merges the current linestring with the other one. The direction of both points have to be same.
   * 
   * @param other the linestring to be merged
   * @return the merged linestring
   */
  public abstract AbstractLineString merge(AbstractLineString other);

  /**
   * <p>
   * Method getPoint
   * </p>
   * 
   * @param idx
   * @return
   */
  public final Point getPoint(int idx) {
    return points[idx];
  }

  /**
   * <p>
   * Method getPoints
   * </p>
   * 
   * @return
   */
  public final Point[] getPoints() {
    return points;
  }

  /**
   * <p>
   * Method numPoints
   * </p>
   * 
   * @return the number of points of that linestring
   */
  public final int numPoints() {
    return points.length;
  }

  /**
   * <p>
   * Method getFirstPoint
   * </p>
   * 
   * @param idx
   * @return
   */
  public final Point getFirstPoint() {
    if ((points == null) || (points.length == 0)) {
      throw new ArrayIndexOutOfBoundsException("Empty Geometry has no Points!");
    } else {
      return points[0];
    }
  }

  /**
   * <p>
   * Method getLastPoint
   * </p>
   * 
   * @return
   */
  public final Point getLastPoint() {
    if ((points == null) || (points.length == 0)) {
      throw new ArrayIndexOutOfBoundsException("Empty Geometry has no Points!");
    } else {
      return points[points.length - 1];
    }
  }
  
  /**
   * 
   * <p>Method getMBR</p>
   * @return
   */
  public abstract BBox getMBR();

  /**
   * <p>
   * Method iterator
   * </p>
   * 
   * @return
   */
  public final Iterator<Point> iterator() {
    return java.util.Arrays.asList(points).iterator();
  }

  @Override
  public int compareTo(AbstractLineString other) {
    return getMBR().compareTo(other.getMBR());
  }

}
