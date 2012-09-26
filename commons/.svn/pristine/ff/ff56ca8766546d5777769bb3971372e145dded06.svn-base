/**
 * 
 */
package isochrones.utils;

/**
 *
 * <p>The <code>MathUtil</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class MathUtil {
  
  /**
   * 
   * <p>Method interpolate</p>
   * @param f0 start value of the interval from which the interpolated value is calculated
   * @param f1 end value of the interval from which the interpolated value is calculated
   * @param x0 start value of the interval from which the intermediate value is given
   * @param x1 end value of the interval from which the intermediate value is given 
   * @param x the given intermediate value
   * @return the interpolated value 
   */
  public static double interpolate(double f0, double f1, double x0, double x1, double x) {
    return f0 + (f1-f0)/(x1-x0)*(x-x0);
  }

}
