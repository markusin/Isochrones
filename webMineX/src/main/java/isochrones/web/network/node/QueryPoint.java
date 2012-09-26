package isochrones.web.network.node;

/**
 * The <code>QueryPoint</code> class represents a query point. Query points are particular pedestrian nodes with
 * temporal distance equal to zero, since they are used as the starting point of the isochrone computation.
 * 
 * @author Markus Innerebner
 * @author Willi Cometti
 * @version 3.0
 */
public class QueryPoint extends WebNode {

  int x, y;

  /**
   * Class Constructor that creates a query point. Query points have a temporal distance equal to zero.
   * 
   * @param nodeId the node ID
   * @param x the x coordinate
   * @param y the y coordinate
   */

  public QueryPoint(int nodeId, int x, int y) {
    super(nodeId);
    setDistance(0);
    this.x = x;
    this.y = y;
  }

  public int getX() {
    return x;
  }

  /**
   * <p>
   * Method getY
   * </p>
   * 
   * @return
   */
  public int getY() {
    return y;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof QueryPoint))
      return false;
    QueryPoint other = (QueryPoint) obj;
    return this.getId() == other.getId() && this.x == other.x && this.y == other.y;
  }

  @Override
  public int hashCode() {
    return (int) (getId() + x + y);
  }

  @Override
  public String toString() {
    return "[" + getId() + ", (" + x + "," + y + ")]";
  }

  @Override
  public boolean isClosed() {
    return true;
  }

}
