package isochrones.mrnex.algorithm.test;

import isochrones.algorithm.test.IMemoryDurationTest;
import isochrones.mrnex.algorithm.MRNEX;
import isochrones.mrnex.network.node.ANode;
import isochrones.network.Location;
import isochrones.utils.Config;
import isochrones.utils.DurationEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 
 * <p>Constructs a(n) <code>MemoryDurationTest</code> object.</p>
 * @param outputWriting
 * @param mode
 * @param database
 */
public class MemoryDurationTest extends MRNEX implements IMemoryDurationTest {

  List<DurationEntry> logEntries = new ArrayList<DurationEntry>();
  private int[] durationCheckPoints;

  public MemoryDurationTest(Config config) {
    super(config);
  }

  @Override
  public void compute() {
    ANode node = priorityQueue.poll();
    int i = 0;
    while (node != null) {
      double dur = node.getDistance() / 60;
      if (i<durationCheckPoints.length && dur >= durationCheckPoints[i]) {
        System.out.println(durationCheckPoints[i]);
        logEntries.add(new DurationEntry(durationCheckPoints[i++], trace.getMaxNodeSize(),trace.getContinuousLinkSize(),trace.getDiscreteLinkSize()));
      }
      expandNode(node);
      node = priorityQueue.poll();
    }
    if (i < durationCheckPoints.length) {
      System.out.println(durationCheckPoints[i]);
      logEntries.add(new DurationEntry(durationCheckPoints[i++], trace.getMaxNodeSize(),trace.getContinuousLinkSize(),trace.getDiscreteLinkSize()));
    }
    terminate();
  }

  @Override
  public List<DurationEntry> getLogEntries() {
    return logEntries;
  }

  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime,int[] durationCheckPoints) {
    this.durationCheckPoints = durationCheckPoints;
    super.computeIsochrone(nodeIds, duration, walkingSpeed, targetTime);
  }

  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime,int[] durationCheckPoints) {
    this.durationCheckPoints = durationCheckPoints;
    super.computeIsochrone(locations, duration, walkingSpeed, targetTime);
  }
}
