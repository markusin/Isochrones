package isochrones.network;

/**
 * 
*
* <p>The <code>TripEntity</code> class</p>
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p> </p>
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
 */
public class TripEntity {
  int tripId;
  double distance;
  boolean reachedByTripQuery;
  
  public TripEntity(int tripId, double distance, boolean reachedByTripQuery) {
    this.tripId = tripId;
    this.distance = distance;
    this.reachedByTripQuery = reachedByTripQuery;
  }
  
  public int getTripId() {
    return tripId;
  }
  
  public double getDistance() {
    return distance;
  }
  
  
  public void setTripId(int tripId) {
    this.tripId = tripId;
  }
  
  public void setDistance(double distance) {
    this.distance = distance;
  }
  
  public void setReachedByTripQuery(boolean reachedByTripQuery) {
    this.reachedByTripQuery = reachedByTripQuery;
  }
  
  public boolean isReachedByTripQuery() {
    return reachedByTripQuery;
  }
  
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("{tripId: ").append(tripId).append(", distance:");
    buf.append(distance).append(", reachedByTrip: ").append(reachedByTripQuery).append("}");
    return buf.toString();
  }
  
  
}