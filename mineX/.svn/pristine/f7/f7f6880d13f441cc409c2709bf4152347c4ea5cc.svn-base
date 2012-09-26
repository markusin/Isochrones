package isochrones.minex.algorithm.test;

import isochrones.algorithm.test.IReachabilityTest;
import isochrones.minex.algorithm.MineX;
import isochrones.minex.network.node.NodeBINE;
import isochrones.network.Location;
import isochrones.utils.Config;
import isochrones.utils.ReachabilityEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
public class ReachabilityTest extends MineX implements IReachabilityTest {

  List<ReachabilityEntry> logEntries = new ArrayList<ReachabilityEntry>();

  /**
   * 
   * <p>Constructs a(n) <code>BINEDurationTest</code> object.</p>
   * @param outputWriting
   * @param mode
   * @param logFileSuffix 
   * @param nodeFile the name of the file in which to write the number of occured nodes
   */
  public ReachabilityTest(Config config) {
    super(config);
  }

  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed,
                               Calendar targetTime) {
    setParameters(locations, duration, walkingSpeed, targetTime, trace);
    /*
     * Starts isochrone computation by exploring the links the query points are situated on.
     */
    exploreInitialLocations();
    NodeBINE node = priorityQueue.poll(); // Dequeues the first node from priority queue
    // testing variables
    boolean first = true;
    int i = 1, j = 2;
    
    while (node != null) {
      double dur = node.getDistance() / 60;
      if (dur>=i) {
        if (dur<j) {
          if (first) {
            logEntries.add(new ReachabilityEntry((int)dur, statistic.getExpandedNodes(), statistic.getExploredContinuousLinks(), statistic.getExploredDiscreteLinks()));
            first = false;
          }
        } else {
          first = true;
          if (i < 5) {
            i++;
            j++;
          } else if (i >= 5 && i < 30) {
            i += 5;
            j = i + 5;
          } else if (i >= 30) {
            i += 10;
            j = i + 10;
          }
        }
      }
      expandNode(node);
      maxPrioQueueSize = Math.max(maxPrioQueueSize, priorityQueue.size());// Counter
      node = priorityQueue.poll(); // Dequeues the next node
    }
    //writeTraceInfosIntoFile(logEntries, logFileSuffix, NodeBINE.sizeOf());
    terminate();
  }
  
  @Override
  public List<ReachabilityEntry> getLogEntries() {
    return logEntries;
  }
}
