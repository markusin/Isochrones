package isochrones.mrnex.algorithm.test;

import isochrones.mrnex.algorithm.MRNEX;
import isochrones.mrnex.network.node.ANode;
import isochrones.network.Location;
import isochrones.utils.Config;
import isochrones.utils.RuntimeEntry;

import java.util.Calendar;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Markus Innerebner
 * @version 3.0
 */
public class RuntimePercentageTest extends MRNEX {

  SortedMap<Integer, RuntimeEntry> runTimes = new TreeMap<Integer, RuntimeEntry>();
  int totalSize;
  double[] percentage = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };
  int[] linkSize = new int[percentage.length];

  /**
   * <p>
   * Constructs a(n) <code>RuntimePercentageTest</code> object.
   * </p>
   * 
   * @param config
   */
  public RuntimePercentageTest(Config config) {
    super(config);
  }

  public void setTotalSize(int totalSize) {
    this.totalSize = totalSize;
    for (int i = 0; i < percentage.length; i++) {
      linkSize[i] = (int) (totalSize * percentage[i]);
    }
  }

  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime) {
    long start = System.currentTimeMillis();
    setParameters(locations, duration, walkingSpeed, targetTime, trace);
    /*
     * Starts isochrone computation by exploring the links the query points are situated on.
     */
    exploreInitialLocations();
    ANode node = priorityQueue.poll(); // Dequeues the first node from priority queue

    // testing variables
    int i = 0;
    while (node != null) {
      int exploredLinks = getStatistic().getExploredLinks();
      if (exploredLinks > linkSize[i]) {
        runTimes.put((int) (percentage[i] * 100), new RuntimeEntry(System.currentTimeMillis() - start, exploredLinks));
        i++;
      }
      expandNode(node);
      maxPrioQueueSize = Math.max(maxPrioQueueSize, priorityQueue.size());// Counter
      node = priorityQueue.poll(); // Dequeues the next node
    }
    runTimes.put((int) (percentage[i] * 100), new RuntimeEntry(System.currentTimeMillis() - start, getStatistic().getExploredLinks()));
    terminate();
  }

  /**
   * <p>
   * Method getRunTimes
   * </p>
   * 
   * @return
   */
  public SortedMap<Integer, RuntimeEntry> getRunTimes() {
    return runTimes;
  }
 
}
