/**
 * 
 */
package isochrones.algorithm.test;

import isochrones.network.Location;
import isochrones.utils.Config;
import isochrones.utils.RuntimeEntry;

import java.util.Calendar;
import java.util.SortedMap;

/**
 * <p>
 * The <code>IRuntimeDurationExperiment</code> class
 * </p>
 * <p>
 * Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a>
 * </p>
 * <p>
 * Domenikanerplatz - Bozen, Italy.
 * </p>
 * <p>
 * </p>
 * 
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public interface IRuntimeDurationTest {

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
   * <p>
   * Method getLogEntries
   * </p>
   * 
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
