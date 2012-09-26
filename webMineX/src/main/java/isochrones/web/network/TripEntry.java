/**
 * 
 */
package isochrones.web.network;

/**
 *
 * <p>The <code>TripEntry</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class TripEntry {
  
  long time;
  int tripId;
  
  /**
   * 
   * <p>Constructs a(n) <code>TripEnntry</code> object.</p>
   * @param time
   * @param tripId
   */
  public TripEntry(long time, int tripId) {
    this.time = time;
    this.tripId = tripId;
  }
  
  public long getTime() {
    return time;
  }
  
  public int getTripId() {
    return tripId;
  }
  
  @Override
  public String toString() {
    return Long.valueOf(time) + "," + Integer.valueOf(tripId);
  }

}
