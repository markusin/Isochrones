package isochrones.web.utils;

import isochrones.web.coverage.IsoEdge;
import isochrones.web.geometry.Point;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class SBAUtil {

  private static final NumberFormat formatter = new DecimalFormat("#0.00");

  /**
   * <p>
   * Method calculateAngle
   * </p>
   * 
   * @param fromLink
   * @param toLink
   * @return
   */
  public static double calculateAngle(IsoEdge fromLink, IsoEdge toLink) {
    Point fromPoint = null, toPoint = null, intermediate = null;
    if (fromLink.getStartNodeId() == toLink.getEndNodeId()) {
      fromPoint = fromLink.getGeometry().getLastPoint();
      intermediate = fromLink.getGeometry().getFirstPoint();
    } else if (fromLink.getEndNodeId() == toLink.getEndNodeId()) {
      fromPoint = fromLink.getGeometry().getFirstPoint();
      intermediate = fromLink.getGeometry().getLastPoint();
    } else {
      System.err.println("This case should not appear!!");
    }
    toPoint = toLink.getGeometry().getFirstPoint();
    // new
    if ((fromLink.getStartNodeId() == toLink.getEndNodeId() && fromLink.getEndNodeId() == toLink.getStartNodeId())
        || (toLink.getStartNodeId() == fromLink.getEndNodeId() && toLink.getEndNodeId() == fromLink.getStartNodeId())) {
      return 360;
    }

    double referenceAngle = calculateAngle(intermediate, fromPoint);
    double angle = calculateAngle(intermediate, toPoint);
    // double angle = calculateAngle(fromPoint,toPoint);
    Double difference = angle - referenceAngle;
    if (difference < 0) {
      difference = Math.toDegrees(2 * Math.PI) + difference;
    }
    if (difference.equals(Double.NaN))
      return Double.MAX_VALUE;
    // System.out.println("Degrees: " + difference);
    return Double.parseDouble(formatter.format(difference));
  }

  /**
   * <p>
   * Method calculateAngle
   * </p>
   * 
   * @param toLink
   * @return the angle between the last point and the second last one
   */
  public static double calculateAngle(boolean fromLast, IsoEdge toLink) {
    Point toPoint = null, intermediate = null;
    if (fromLast) {
      intermediate = toLink.getGeometry().getLastPoint();
      toPoint = toLink.getGeometry().getPoint(toLink.getGeometry().numPoints() - 2);
    } else {
      intermediate = toLink.getGeometry().getFirstPoint();
      toPoint = toLink.getGeometry().getPoint(1);
    }
    return calculateAngle(intermediate, toPoint);
  }

  /**
   * <p>
   * Method calculateAngleNew
   * </p>
   * calculates the angle by using the first point
   * 
   * @param fromLink
   * @param toLink
   * @return
   */
  public static double calculateAngleNew(IsoEdge fromLink, IsoEdge toLink) {
    Point fromPoint = null, toPoint = null, intermediate = null;

    if (fromLink.getStartNodeId() == toLink.getEndNodeId()) {
      if (fromLink.getGeometry().numPoints() >= 2) {
        fromPoint = fromLink.getGeometry().getPoint(1);
      } else {
        System.err.println("Edge " + fromLink.getId() + " has only two ordinates.");
        return Double.MAX_VALUE;
      }
      intermediate = fromLink.getGeometry().getFirstPoint();
    } else if (fromLink.getEndNodeId() == toLink.getEndNodeId()) {
      // fromPoint = fromLink.getGeometry().getFirstPoint();
      if (fromLink.getGeometry().numPoints() >= 2) {
        fromPoint = fromLink.getGeometry().getPoint(fromLink.getGeometry().numPoints() - 2);
      } else {
        System.err.println("Edge " + fromLink.getId() + " has only two ordinates.");
        return Double.MAX_VALUE;
      }
      intermediate = fromLink.getGeometry().getLastPoint();
    } else {
      System.err.println("This case should not appear!!");
    }

    if (toLink.getGeometry().numPoints() >= 2) {
      toPoint = toLink.getGeometry().getPoint(toLink.getGeometry().numPoints() - 2);
    } else {
      System.err.println("Edge  " + toLink.getId() + " has only two ordinates.");
      return Double.MAX_VALUE;
    }
    // toPoint = toLink.getGeometry().getFirstPoint();
    // new
    if ((fromLink.getStartNodeId() == toLink.getEndNodeId() && fromLink.getEndNodeId() == toLink.getStartNodeId())
        || (toLink.getStartNodeId() == fromLink.getEndNodeId() && toLink.getEndNodeId() == fromLink.getStartNodeId())) {
      return 360;
    }

    double referenceAngle = calculateAngle(intermediate, fromPoint);
    double angle = calculateAngle(intermediate, toPoint);
    // double angle = calculateAngle(fromPoint,toPoint);
    Double difference = angle - referenceAngle;
    if (difference < 0) {
      difference = Math.toDegrees(2 * Math.PI) + difference;
    }
    if (difference.equals(Double.NaN)) {
      System.err.println("Problems with angle computation.");
      return Double.MAX_VALUE;
    }

    // System.out.println("Degrees: " + difference);
    return Double.parseDouble(formatter.format(difference));
  }

  /**
   * <p>
   * Method calculateAngle
   * </p>
   * 
   * @param fromLink
   * @param toLink
   * @return
   */
  public static double calculateAngleFiner(IsoEdge fromLink, IsoEdge toLink) {
    Point fromPoint = null, toPoint = null, intermediate = null;
    if (fromLink.getStartNodeId() == toLink.getEndNodeId()) {
      fromPoint = fromLink.getGeometry().getLastPoint();
      intermediate = fromLink.getGeometry().getFirstPoint();
    } else if (fromLink.getEndNodeId() == toLink.getEndNodeId()) {
      fromPoint = fromLink.getGeometry().getFirstPoint();
      intermediate = fromLink.getGeometry().getLastPoint();
    } else {
      System.err.println("This case should not appear!!");
    }
    toPoint = toLink.getGeometry().getPoint(1);

    if (toPoint.equals(intermediate)) {
      toPoint = toLink.getGeometry().getFirstPoint();
    }

    // new
    if ((fromLink.getStartNodeId() == toLink.getEndNodeId() && fromLink.getEndNodeId() == toLink.getStartNodeId())
        || (toLink.getStartNodeId() == fromLink.getEndNodeId() && toLink.getEndNodeId() == fromLink.getStartNodeId())) {
      return 360;
    }

    double referenceAngle = calculateAngle(intermediate, fromPoint);
    double angle = calculateAngle(intermediate, toPoint);
    // double angle = calculateAngle(fromPoint,toPoint);
    double difference = angle - referenceAngle;
    if (difference < 0) {
      difference = Math.toDegrees(2 * Math.PI) + difference;
    }
    // System.out.println("Degrees: " + difference);
    return difference;
  }

  /**
   * <p>
   * Method calculateAngle
   * </p>
   * 
   * @param fromPoint
   * @param toPoint
   * @return
   */
  public static double calculateAngle(double[] fromPoint, double[] toPoint) {
    double deltaX = toPoint[0] - fromPoint[0];
    double deltaY = toPoint[1] - fromPoint[1];
    if (deltaX == 0 && deltaY == 0)
      return Double.NaN;
    double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
    // double angle2 = Math.toDegrees(Math.atan2(Math.abs(deltaY),Math.abs(deltaX)));
    if (angle < 0) {
      // angle = Math.toDegrees(2*Math.PI) - (Math.toDegrees(Math.PI) + angle);
      angle = Math.toDegrees(2 * Math.PI) + angle;
    }
    return angle;
  }

  public static double calculateAngle(Point fromPoint, Point toPoint) {
    double deltaX = toPoint.getX() - fromPoint.getX();
    double deltaY = toPoint.getY() - fromPoint.getY();
    if (deltaX == 0 && deltaY == 0)
      return Double.NaN;
    double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
    // double angle2 = Math.toDegrees(Math.atan2(Math.abs(deltaY),Math.abs(deltaX)));
    if (angle < 0) {
      // angle = Math.toDegrees(2*Math.PI) - (Math.toDegrees(Math.PI) + angle);
      angle = Math.toDegrees(2 * Math.PI) + angle;
    }
    return angle;
  }

  public static double eucideanDist(double x1, double y1, double x2, double y2) {
    return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
  }
  
  /**
   * 
   * <p>Method eucideanDist</p>
   * @param fromPoint
   * @param toPoint
   * @return
   */
  public static double eucideanDist(Point fromPoint, Point toPoint) {
    return Math.sqrt(Math.pow(toPoint.getX() - fromPoint.getX(), 2) + Math.pow(toPoint.getY() - fromPoint.getY(), 2));
  }

  /**
   * <p>
   * Method calcultateRelativeAngle
   * </p>
   * 
   * @param currentAngle
   * @param referenceAngle
   * @return
   */
  public static double calcultateRelativeAngle(double currentAngle, double referenceAngle) {
    double relativeAngle = currentAngle - referenceAngle;
    if (relativeAngle < 0) {
      relativeAngle = Math.toDegrees(2 * Math.PI) + relativeAngle;
    }
    if (relativeAngle == Double.NaN) {
      System.err.println("Problems with angle computation.");
      return Double.MAX_VALUE;
    }
    return relativeAngle;
  }
  
  /**
   * <p>
   * Method reachedFromBothSides
   * </p>
   * checks if a link is reached from both sides.
   * 
   * @param link the link to be checked
   * @param inverted the inverted link (may also be null)
   * @param incoming if incoming direction is considered
   * @return true if all points along the link are reached
   */
  public static boolean reachedFromBothSides(IsoEdge link, IsoEdge inverted, boolean incoming) {
	  if(!link.isPartial()) return true;
    // if (link.getOffset().getStartOffset() == 0)
    if (inverted == null)
      return false;
    if (!inverted.isPartial())
      return true;
    if(incoming) {
      return inverted.getOffset().getStartOffset() + link.getOffset().getStartOffset() <= link.getLength();
    } else {
      return inverted.getOffset().getEndOffset() + link.getOffset().getEndOffset() >= link.getLength();
    }
  }

}
