package isochrones.algorithm.test;

import isochrones.network.Location;
import isochrones.utils.DurationEntry;

import java.util.Calendar;
import java.util.List;

public interface IMemoryDurationTest {
  
  /**
   * <p>
   * Method computeIsochrone
   * </p>
   * 
   * @param nodeIds
   * @param duration
   * @param walkingSpeed
   * @param targetTime
   * @param durationCheckPoints
   */
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime,
                               int[] durationCheckPoints);

  /**
   * 
   * <p>Method computeIsochrone</p>
   * @param locations
   * @param duration
   * @param walkingSpeed
   * @param targetTime
   */
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed,
                               Calendar targetTime, int[] durationCheckPoints);
  
  /**
   * 
   * <p>Method getLogEntries</p>
   * @return
   */
  public List<DurationEntry> getLogEntries();

}
