package isochrones.minex.algorithm.test;

import isochrones.minex.algorithm.MineX;
import isochrones.minex.network.node.NodeBINE;
import isochrones.network.Location;
import isochrones.utils.Config;
import isochrones.utils.RuntimeEntry;

import java.util.Calendar;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The <code>BineSN</code> class allows to compute isochrones in a bi-modal, schedule-based transport network, where
 * walking in combination with the public transport is considered. This version of the isochrone algorithm uses
 * separated pedestrian and bus networks which are connected together through a mapping table.
 * <p>
 * Isochrones are defined as the set of all points from which a specific query point is reachable within a given time
 * span. The used graph exploration algorithm is based on Dijkstra's Shortest Path. This particular version of the
 * isochrone algorithm tries to optimize memory utilization. It starts with an empty graph, adding nodes and links as
 * the network exploration proceeds. Moreover, network segments, which are no longer used, are removed from the graph.
 * 
 * @author Markus Innerebner
 * @version 3.0
 */
public class RuntimePercentageTest extends MineX {

  SortedMap<Integer, RuntimeEntry> runTimes = new TreeMap<Integer, RuntimeEntry>();
  int totalSize;
  //double[] percentage = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };
  double[] percentage = { 0.2, 0.21, 0.22, 0.23, 0.24, 0.25, 0.26, 0.27, 0.28, 0.29};
  int[] linkSize = new int[percentage.length];

  public RuntimePercentageTest(Config config) {
    super(config);
  }

  public void setTotalSize(int totalSize) {
    this.totalSize = totalSize;
    for (int i = 0; i < percentage.length; i++) {
      linkSize[i] = (int) (totalSize * percentage[i]);
    }
  }

  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed,
                               Calendar targetTime){
    long start = System.currentTimeMillis();
    setParameters(locations, duration, walkingSpeed, targetTime,trace);
    /*
     * Starts isochrone computation by exploring the links the query points are situated on.
     */
    exploreInitialLocations();
    NodeBINE node = priorityQueue.poll(); // Dequeues the first node from priority queue

    // testing variables
    int i = 0;
    while (node != null) {
      int discoveredLinks = getStatistic().getExploredLinks();
      if (i<linkSize.length && discoveredLinks >= linkSize[i]) {
        runTimes.put((int)Math.round(percentage[i] * 100), new RuntimeEntry(System.currentTimeMillis() - start, discoveredLinks));
        i++;
      }
      expandNode(node);
      maxPrioQueueSize = Math.max(maxPrioQueueSize, priorityQueue.size());// Counter
      node = priorityQueue.poll(); // Dequeues the next node
    }
    //runTimes.put((int)(percentage[i] * 100), new RuntimeEntry(System.currentTimeMillis() - start, getStatistic().getDiscoveredLinks()));
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
