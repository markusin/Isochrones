package isochrones.minex.algorithm.test;

import isochrones.algorithm.test.IRuntimeDurationTest;
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
public class RuntimeDurationTest extends MineX implements IRuntimeDurationTest {

  SortedMap<Integer, RuntimeEntry> runTimes = new TreeMap<Integer, RuntimeEntry>();
  long start;
  private int[] durationCheckPoints;

  public RuntimeDurationTest(Config config) {
    super(config);
  }

  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime,
                               int[] durationCheckPoints) {
    this.durationCheckPoints = durationCheckPoints;
    start = System.currentTimeMillis();
    super.computeIsochrone(locations, duration, walkingSpeed, targetTime);
  }

  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime,
                               int[] durationCheckPoints) {
    this.durationCheckPoints = durationCheckPoints;
    start = System.currentTimeMillis();
    super.computeIsochrone(nodeIds, duration, walkingSpeed, targetTime);
  }

  @Override
  public void compute() {
    int i = 0;
    NodeBINE node = priorityQueue.poll();
    while (node != null) {
      double dur = node.getDistance() / 60;
      if (i < durationCheckPoints.length && dur >= durationCheckPoints[i]) {
        long t = System.currentTimeMillis() - start;
        System.out.println(durationCheckPoints[i] + "\t" + t);
        runTimes.put(durationCheckPoints[i++], new RuntimeEntry(t, getStatistic().getExploredNodes()));
      }
      expandNode(node);
      node = priorityQueue.poll();
    }
    if (i < durationCheckPoints.length) {
      long t = System.currentTimeMillis() - start;
      System.out.println(durationCheckPoints[i] + "\t" + t);
      RuntimeEntry runtimeEntry = new RuntimeEntry(t, getStatistic().getExploredNodes());
      runTimes.put(durationCheckPoints[i], runtimeEntry);
    }
    terminate();
  }

  @Override
  public SortedMap<Integer, RuntimeEntry> getLogEntries() {
    return runTimes;
  }
  
  @Override
  public Config getConfig() {
    return config;
  }

}
