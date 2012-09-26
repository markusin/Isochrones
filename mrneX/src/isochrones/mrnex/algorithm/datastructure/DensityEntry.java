/**
 * 
 */
package isochrones.mrnex.algorithm.datastructure;

import isochrones.network.GeoPoint;

/**
 *
 * <p>The <code>DensityEntry</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class DensityEntry {
  
  int density;
  double range;
  GeoPoint coordinate;
  
  /**
   * 
   * <p>Constructs a(n) <code>DensityEntry</code> object.</p>
   * @param density the density
   * @param range the maximal possible range to be loaded
   * @param x the x coordinate
   * @param y the y coordinate
   * 
   */
  public DensityEntry(int density, double range, double x, double y) {
    this.density = density;
    this.range = range;
    coordinate = new GeoPoint(x, y);
  }

  /**
   * 
   * <p>Method getDensity</p>
   * @return the density
   */
  public int getDensity() {
    return density;
  }

  /**
   * 
   * <p>Method getRange</p>
   * @return the maximal possible range
   */
  public double getRange() {
    return range;
  }
  
  public GeoPoint getCoordinate() {
    return coordinate;
  }
  

}
