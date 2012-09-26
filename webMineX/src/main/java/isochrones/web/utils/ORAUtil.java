package isochrones.web.utils;

import isochrones.web.geometry.Point;

import java.util.List;

public class ORAUtil {

  /**
   * 
   * <p>Method asOrdinates</p>
   * @param points
   * @return
   */
  public static double[] asOrdinates(Point[] points) {
    double[] ordinates = new double[points.length*2];
    for (int i = 0,j=0; i < points.length; i++,j=+2) {
      ordinates[j] = points[i].getX();
      ordinates[j+1] = points[i].getX();
    }
    return ordinates;
  }
  
  /**
   * 
   * <p>Method asOrdinates</p>
   * @param points
   * @return
   */
  public static double[] asOrdinates(List<Point> points) {
    double[] ordinates = new double[points.size()*2];
    int i=0;
    for (Point point : points) {
      ordinates[i++] = point.getX();
      ordinates[i++] = point.getY();
    }
    return ordinates;
  }
  
}
