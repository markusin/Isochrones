package isochrones.mrnex.algorithm.test;

import isochrones.algorithm.test.IMemorySizeTest;
import isochrones.mrnex.algorithm.MRNEX;
import isochrones.mrnex.network.node.ANode;
import isochrones.utils.Config;
import isochrones.utils.MemoryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>MemorySizeTest</code>
 * 
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
 */
public class MemorySizeTestNaive extends MRNEX implements IMemorySizeTest {

  List<MemoryEntry> logEntries = new ArrayList<MemoryEntry>();
  private int[] checkPoints;

  /**
   * <p>
   * Constructs a(n) <code>BINEMemoryTest</code> object.
   * </p>
   * 
   * <p>Constructs a(n) <code>BINEMemoryTest</code> object.</p>
   * @param outputWriting
   * @param mode
   * @param database
   * @param checkPoints
   */
  public MemorySizeTestNaive(Config config, int[] checkPoints) {
    super(config);
    this.checkPoints = checkPoints;
  }
  
  @Override
  public void compute() {
    int i = 0;
    ANode node = priorityQueue.poll(); // Dequeues the first node from priority queue
    while (node != null) {
      int isoSize = statistic.getExploredNodes();
      if(i<checkPoints.length && isoSize>=checkPoints[i]){
        System.out.println(checkPoints[i]);
        MemoryEntry memoryEntry = new MemoryEntry(statistic.getExploredContinuousLinks(), statistic.getExploredDiscreteLinks(),checkPoints[i],
            trace.getContinuousLinkSize(),trace.getDiscreteLinkSize(), trace.getMaxNodeSize(),(int)Math.round(node.getDistance()/60));
        logEntries.add(memoryEntry);
        i++;
      }
      expandNode(node);
      node = priorityQueue.poll();
    }
    if (i < checkPoints.length) {
      System.out.println(checkPoints[i]);
      MemoryEntry memoryEntry = new MemoryEntry(statistic.getExploredContinuousLinks(), statistic.getExploredDiscreteLinks(),checkPoints[i],
          trace.getContinuousLinkSize(),trace.getDiscreteLinkSize(), trace.getMaxNodeSize(),(int)Math.round(maxDuration/60));
      logEntries.add(memoryEntry);
    }
  }
  
  /**
   * 
   * <p>Method getLogEntries</p>
   * @return
   */
  public List<MemoryEntry> getLogEntries() {
    return logEntries;
  }

  @Override
  public int getNodeSizeInBytes() {
    return ANode.sizeOf();
  }
  
}
