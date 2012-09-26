package isochrones.mrnex.algorithm.test;

import isochrones.mrnex.utils.NetworkMonitorEntry;
import isochrones.network.Location;

import java.util.Calendar;
import java.util.List;

public interface IMonitorNetworkLoadingTest {

  /**
   * 
   * <p>Method getLogEntries</p>
   * @return
   */
  public List<NetworkMonitorEntry> getLogEntries();
  
  /**
   * 
   * <p>Method computeIsochrone</p>
   * @param nodeIds
   * @param duration
   * @param walkingSpeed
   * @param targetTime
   * @param durationCheckPoints
   */
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime) ;
  
  /**
   * 
   * <p>Method computeIsochrone</p>
   * @param locations
   * @param duration
   * @param walkingSpeed
   * @param targetTime
   * @param durationCheckPoints
   */
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime);
  
}
