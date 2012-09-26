package isochrones.utils;

import isochrones.network.GeoPoint;

public class NetworkUtil {
  /**
   * 
   * <p>Method euclideanDistance</p>
   * @param p1
   * @param p2
   * @return
   */
  public static double euclideanDistance(GeoPoint p1, GeoPoint p2) {
    double value = Math.pow(p1.getX()-p2.getX(),2) + Math.pow(p1.getY()-p2.getY(),2);
    return Math.sqrt(value);
  }
  
/**
 * 
 * <p>Method calculateOverlappingDistance</p>
 * @param p1
 * @param r1
 * @param p2
 * @param r2
 * @return
 */
  public static double calculateOverlappingDistance(GeoPoint p1, double r1, GeoPoint p2, double r2) {
    double eDist = euclideanDistance(p1,p2);
    return eDist - (2 * eDist - r1 - r2);
  }

}
