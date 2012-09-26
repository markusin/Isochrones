package isochrones.mrnex.algorithm.test;

import isochrones.mrnex.algorithm.MRNEX;
import isochrones.mrnex.network.node.ANode;
import isochrones.mrnex.utils.NetworkMonitorEntry;
import isochrones.network.Location;
import isochrones.utils.Config;

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
public class MonitorNetworkLoadingTest extends MRNEX implements IMonitorNetworkLoadingTest {

  List<NetworkMonitorEntry> logEntries = new ArrayList<NetworkMonitorEntry>();

  public MonitorNetworkLoadingTest(Config config) {
    super(config);
  }

  @Override
  public void compute() {
    ANode node = priorityQueue.poll();
    while (node != null) {
      expandNode(node);
      node = priorityQueue.poll();
    }
    NetworkMonitorEntry entry = new NetworkMonitorEntry(maxDuration/60,getStatistic().getExploredLinks(), getStatistic().getEdgesLoadedWithIER(),getStatistic().getEdgesLoadedWithINE(),getStatistic().getLoadedContinuousEdges() + getStatistic().getLoadedDiscreteEdges());
    logEntries.add(entry);
    terminate();
  }

  @Override
  public List<NetworkMonitorEntry> getLogEntries() {
    return logEntries;
  }

  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime) {
    super.computeIsochrone(nodeIds, duration, walkingSpeed, targetTime);
  }

  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime) {
    super.computeIsochrone(locations, duration, walkingSpeed, targetTime);
  }
}
