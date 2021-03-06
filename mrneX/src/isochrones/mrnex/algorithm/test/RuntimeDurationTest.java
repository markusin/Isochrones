package isochrones.mrnex.algorithm.test;

import isochrones.algorithm.test.IRuntimeDurationTest;
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
public class RuntimeDurationTest extends MRNEX implements IRuntimeDurationTest {

  SortedMap<Integer, RuntimeEntry> runTimes = new TreeMap<Integer, RuntimeEntry>();
  private int[] durationCheckPoints;
  long start;

  public RuntimeDurationTest(Config config) {
    super(config);
  }

  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime,
                               int[] checkPoints) {
    this.durationCheckPoints = checkPoints;
    start = System.currentTimeMillis();
    super.computeIsochrone(locations, duration, walkingSpeed, targetTime);
  }

  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime, int[] checkPoints) {
    this.durationCheckPoints = checkPoints;
    start = System.currentTimeMillis();
    super.computeIsochrone(nodeIds, duration, walkingSpeed, targetTime);
  }

  @Override
  public void compute() {
    ANode node = priorityQueue.poll();
    int i = 0;
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
      System.out.println(maxDuration / 60 + "\t" + t);
      RuntimeEntry runtimeEntry = new RuntimeEntry(t, getStatistic().getExploredNodes());
      runTimes.put(maxDuration / 60, runtimeEntry);
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
