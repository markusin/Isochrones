package isochrones.algorithm.test;

import isochrones.network.Location;
import isochrones.utils.MemoryEntry;

import java.util.Calendar;
import java.util.List;

public interface IMemorySizeTest {
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
  public List<MemoryEntry> getLogEntries();

  /**
   * 
   * <p>Method getNodeSizeInBytes</p>
   * @return
   */
  public int getNodeSizeInBytes();
}
