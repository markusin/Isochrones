/**
 * 
 */
package isochrones.network;

/**
 *
 * <p>The <code>GeoPoint</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class GeoPoint {
  
  double x,y;
  
  public GeoPoint(double x, double y) {
      this.x = x;
      this.y = y;
  }

  public double getX() {
    return x;
  }
  
  public double getY() {
    return y;
  }

}
