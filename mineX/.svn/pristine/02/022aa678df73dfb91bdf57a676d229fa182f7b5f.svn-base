package isochrones.minex.algorithm.test;

import isochrones.algorithm.test.IMemoryDurationTest;
import isochrones.minex.algorithm.MineX;
import isochrones.minex.network.node.NodeBINE;
import isochrones.network.Location;
import isochrones.utils.Config;
import isochrones.utils.DurationEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * The <code>MemoryDurationTest</code>
 * 
 * @author Markus Innerebner
 * @version 3.0
 */
public class MemoryDurationTest extends MineX implements IMemoryDurationTest {

  List<DurationEntry> logEntries = new ArrayList<DurationEntry>();
  private int[] durationCheckPoints;

  /**
   * 
   * <p>Constructs a(n) <code>MemoryDurationTest</code> object.</p>
   * @param config
   */
  public MemoryDurationTest(Config config) {
    super(config);
  }
  
  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime, int[] durationCheckPoints) {
    this.durationCheckPoints = durationCheckPoints;
    super.computeIsochrone(locations, duration, walkingSpeed, targetTime);
  }
  
  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime, int[] durationCheckPoints) {
    this.durationCheckPoints = durationCheckPoints;
    super.computeIsochrone(nodeIds, duration, walkingSpeed, targetTime);
  }

  @Override
  public void compute() {
    NodeBINE node = priorityQueue.poll();
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
    terminate();
  };

  @Override
  public List<DurationEntry> getLogEntries() {
    return logEntries;
  }
}
