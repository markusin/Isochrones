package isochrones.mrnex.utils;

import isochrones.network.GeoPoint;

public class RangeUtil {

  final static int LL = 0;
  final static int LR = 1;
  final static int UR = 2;
  final static int UL = 3;

  /**
   * <p>
   * Method euclideanDistance
   * </p>
   * 
   * @param p1
   * @param p2
   * @return
   */
  public static double euclideanDistance(GeoPoint p1, GeoPoint p2) {
    double value = Math.pow(p1.getX() - p2.getX(), 2) + Math.pow(p1.getY() - p2.getY(), 2);
    return Math.sqrt(value);
  }

  /**
   * <p>
   * Method calculateOverlappingDistanceRadius
   * </p>
   * calculates the overlapping distance between two radii
   * 
   * @param fromPoint
   * @param fromRadius
   * @param toPoint
   * @param toRadius
   * @return
   */
  public static double calculateOverlappingDistanceOld(GeoPoint fromPoint, double fromRadius, GeoPoint toPoint,
                                                       double toRadius) {
    double eDist = euclideanDistance(fromPoint, toPoint);
    return eDist - (2 * eDist - fromRadius - toRadius);
  }

  /**
   * <p>
   * Method calculateOverlappingRadius
   * </p>
   * 
   * @param point
   * @param radius
   * @param loadedPoint
   * @param loadedRadius
   * @return
   */
  public static double calculateOverlappingRadius(GeoPoint point, double radius, GeoPoint loadedPoint,
                                                  double loadedRadius) {
    double overlappingDist = (euclideanDistance(point, loadedPoint) - (radius + loadedRadius)) * -1;
    return overlappingDist;
  }

  /**
   * <p>
   * Method calculateOverlappingArea
   * </p>
   * 
   * @param point
   * @param loadedPoint
   * @param loadedRadius
   * @return the overlapping Area
   */
  public static double calculateOverlappingArea(GeoPoint point, double radius, GeoPoint loadedPoint, double loadedRadius) {

    double overlappingXDist;
    if (loadedPoint.getX() < point.getX()) {
      if (loadedPoint.getX() + loadedRadius > point.getX() + radius) {
        overlappingXDist = 2 * radius;
      } else {
        overlappingXDist = (loadedPoint.getX() + loadedRadius) - (point.getX() - radius);
      }
    } else {
      if (loadedPoint.getX() - loadedRadius < point.getX() - radius) {
        overlappingXDist = 2 * radius;
      } else {
        overlappingXDist = (point.getX() + radius) - (loadedPoint.getX() - loadedRadius);
      }
    }
    if (overlappingXDist < 0)
      return Double.NEGATIVE_INFINITY;

    double overlappingYDist;
    if (loadedPoint.getY() < point.getY()) {
      if (loadedPoint.getY() + loadedRadius > point.getY() + radius) {
        overlappingYDist = 2 * radius;
      } else {
        overlappingYDist = (loadedPoint.getY() + loadedRadius) - (point.getY() - radius);
      }
    } else {
      if (loadedPoint.getY() - loadedRadius < point.getY() - radius) {
        overlappingYDist = 2 * radius;
      } else {
        overlappingYDist = (point.getY() + radius) - (loadedPoint.getY() - loadedRadius);
      }
    }
    if (overlappingYDist < 0)
      return Double.NEGATIVE_INFINITY;
    return overlappingXDist * overlappingYDist;
  }

  /**
   * <p>
   * Method calculateLoadingRangeRadius
   * </p>
   * 
   * @param point the point from which to determine the radius
   * @param loadedPoint the already loaded point
   * @param loadedRadius the radius of the loaded point
   * @return
   */
  public static double calculateLoadingRangeRadius(GeoPoint point, GeoPoint loadedPoint, double loadedRadius) {
    return RangeUtil.euclideanDistance(point, loadedPoint) - loadedRadius;
  }

  /**
   * <p>
   * Method calculateLoadingRangeSquare
   * </p>
   * TODO document me!!!
   * 
   * @param point
   * @param loadedPoint
   * @param loadedRadius
   * @return
   */
  public static double calculateLoadingRangeSquare(GeoPoint point, GeoPoint loadedPoint, double loadedRadius) {
    double xDiff = Math.abs(point.getX() - loadedPoint.getX());
    double yDiff = Math.abs(point.getY() - loadedPoint.getY());
    if (xDiff > yDiff) {
      return xDiff - loadedRadius;
    } else {
      return yDiff - loadedRadius;
    }
  }

  /**
   * <p>
   * Method calculateLoadingRangeRectangle
   * </p>
   * 
   * @param point
   * @param radius
   * @param loadedPoint
   * @param loadedRadius
   * @return
   */
  public static void calculateLoadingRangeRectangle(GeoPoint point, double radius, GeoPoint loadedPoint,
                                                    double loadedRadius, GeoPoint[] rectangle) {
    boolean potentialLeftSideShrink = false;
    double overlappingXDist;
    if (loadedPoint.getX() < point.getX()) { // potential left side shrink
      potentialLeftSideShrink = true;
      if (loadedPoint.getX() + loadedRadius > point.getX() + radius) {
        overlappingXDist = 2 * radius;
      } else {
        overlappingXDist = (loadedPoint.getX() + loadedRadius) - (point.getX() - radius);
      }
    } else { // potential right side shrink
      if (loadedPoint.getX() - loadedRadius < point.getX() - radius) {
        overlappingXDist = 2 * radius;
      } else {
        overlappingXDist = (point.getX() + radius) - (loadedPoint.getX() - loadedRadius);
      }
    }
    boolean potentialLowerSideShrink = false;
    double overlappingYDist;
    if (loadedPoint.getY() < point.getY()) { // potential lower side shrink
      potentialLowerSideShrink = true;
      if (loadedPoint.getY() + loadedRadius > point.getY() + radius) {
        overlappingYDist = 2 * radius;
      } else {
        overlappingYDist = (loadedPoint.getY() + loadedRadius) - (point.getY() - radius);
      }
    } else { // potential upper side shrink
      if (loadedPoint.getY() - loadedRadius < point.getY() - radius) {
        overlappingYDist = 2 * radius;
      } else {
        overlappingYDist = (point.getY() + radius) - (loadedPoint.getY() - loadedRadius);
      }
    }

    if (overlappingXDist > 0 && overlappingYDist > 0) {
      if (overlappingXDist < overlappingYDist) { // decrease quadrat on overlapping x
        if (potentialLeftSideShrink) {
          rectangle[LL] = new GeoPoint(rectangle[LL].getX() + overlappingXDist, rectangle[LL].getY());
          rectangle[UL] = new GeoPoint(rectangle[UL].getX() + overlappingXDist, rectangle[UL].getY());
        } else {
          rectangle[LR] = new GeoPoint(rectangle[LR].getX() - overlappingXDist, rectangle[LR].getY());
          rectangle[UR] = new GeoPoint(rectangle[UR].getX() - overlappingXDist, rectangle[UR].getY());
        }
      } else { // decrease quadrat on overlapping x
        if (potentialLowerSideShrink) {
          rectangle[LL] = new GeoPoint(rectangle[LL].getX(), rectangle[LL].getY() + overlappingYDist);
          rectangle[LR] = new GeoPoint(rectangle[LR].getX(), rectangle[LR].getY() + overlappingYDist);
        } else {
          rectangle[UR] = new GeoPoint(rectangle[UR].getX(), rectangle[UR].getY() - overlappingYDist);
          rectangle[UL] = new GeoPoint(rectangle[UL].getX(), rectangle[UL].getY() - overlappingYDist);
        }
      }
    }
  }

}
