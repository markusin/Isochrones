/**
 * 
 */
package isochrones.web.geometry;


/**
 *
 * <p>The <code>Point</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class Point {
  
  double x,y;

  /**
   * 
   * <p>Constructs a(n) <code>Point</code> object.</p>
   * @param x
   * @param y
   */
  public Point(double x, double y) {
    super();
    this.x = x;
    this.y = y;
  }
  
  /**
   * 
   * <p>Method getX</p>
   * @return
   */
  public double getX() {
    return x;
  }
  
  /**
   * 
   * <p>Method getY</p>
   * @return
   */
  public double getY() {
    return y;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj==null || !(obj instanceof Point)) return false;
    Point objPt = (Point) obj; 
    return objPt.getX()==getX()&& objPt.getY()==getY();
  }
  
  @Override
  public String toString() {
    return "(" + x + " " + y + ")";
  }
  
}
