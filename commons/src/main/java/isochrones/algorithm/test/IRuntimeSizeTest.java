package isochrones.algorithm.test;

import isochrones.network.Location;
import isochrones.utils.Config;
import isochrones.utils.RuntimeEntry;

import java.util.Calendar;
import java.util.SortedMap;

public interface IRuntimeSizeTest {
  /**
   * 
   * <p>Method computeIsochrone</p>
   * @param nodeIds
   * @param duration
   * @param walkingSpeed
   * @param targetTime
   */
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime);
  
  /**
   * 
   * <p>Method computeIsochrone</p>
   * @param locations
   * @param duration
   * @param walkingSpeed
   * @param targetTime
   */
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime);
  
  
  /**
   * 
   * <p>Method getLogEntries</p>
   * @return
   */
  public SortedMap<Integer, RuntimeEntry> getLogEntries();
  
  /**
   * 
   * <p>Method getConfig</p>
   * @return
   */
  public Config getConfig();

}
