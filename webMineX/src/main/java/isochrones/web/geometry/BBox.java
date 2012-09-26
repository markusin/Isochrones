package isochrones.web.geometry;

import org.json.JSONArray;
import org.json.JSONException;

public class BBox implements Comparable<BBox> {
  long minX, minY, maxX, maxY;

  public BBox(double minX, double minY, double maxX, double maxY) {
    this.minX = Math.round(minX);
    this.minY = Math.round(minY);
    this.maxX = Math.round(maxX);
    this.maxY = Math.round(maxY);
  }

  public BBox(Point lowerCorner, Point upperCorner) {
    this.minX = Math.round(lowerCorner.getX());
    this.minY = Math.round(lowerCorner.getY());
    this.maxX = Math.round(upperCorner.getX());
    this.maxY = Math.round(upperCorner.getY());
  }

  /**
   * <p>
   * Method toJSON
   * </p>
   * 
   * @return
   * @throws JSONException
   */
  public JSONArray toJSON() throws JSONException {
    /*
     * System.out.println("minx" + minX); System.out.println("minY" + minY); System.out.println("maxX" + maxX);
     * System.out.println("maxY" + maxY);
     */
    return new JSONArray(new long[] { minX, minY, maxX, maxY });
  }

  public long getMinX() {
    return minX;
  }

  public void setMinX(long minX) {
    this.minX = minX;
  }

  public long getMinY() {
    return minY;
  }

  public void setMinY(long minY) {
    this.minY = minY;
  }

  public long getMaxX() {
    return maxX;
  }

  public void setMaxX(long maxX) {
    this.maxX = maxX;
  }

  public long getMaxY() {
    return maxY;
  }

  public void setMaxY(long maxY) {
    this.maxY = maxY;
  }

  @Override
  public int compareTo(BBox other) {
    if (getMinX() < other.getMinX()) {
      return isochrones.web.constants.Comparable.BEFORE;
    } else if (getMinX() > other.getMinX()) {
      return isochrones.web.constants.Comparable.AFTER;
    } else { // x is equal
      if (getMinY() < other.getMinY())
        return isochrones.web.constants.Comparable.BEFORE;
      else if (getMinY() > other.getMinY())
        return isochrones.web.constants.Comparable.AFTER;
    }
    return isochrones.web.constants.Comparable.EQUAL;
  }

}
