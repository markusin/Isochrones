package isochrones.minex.algorithm.test;

import isochrones.algorithm.test.IMemorySizeTest;
import isochrones.minex.algorithm.MineX;
import isochrones.minex.network.node.NodeBINE;
import isochrones.utils.Config;
import isochrones.utils.MemoryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * The <code>MemorySizeTest</code>
 * 
 * @author Markus Innerebner
 * @version 3.0
 */
public class MemorySizeTest extends MineX implements IMemorySizeTest {

  List<MemoryEntry> logEntries = new ArrayList<MemoryEntry>();
  private int[] checkPoints;
  // only for logging
  LinkInfo lInfo;


  /**
   * 
   * <p>Constructs a(n) <code>BINEMemoryTest</code> object.</p>
   * @param outputWriting
   * @param mode
   * @param database
   * @param checkPoints
   */
  public MemorySizeTest(Config config, int[] checkPoints) {
    super(config);
    this.checkPoints = checkPoints;
  }

  @Override
  public void compute() {
    logEntries.add(new MemoryEntry(0,0,0,0,0,0,0));
    NodeBINE node = priorityQueue.poll();
    int traceBusLinkSize = 0, tracePedLinkSize = 0;
    int i = 0;
    while (node != null) {
      int isoSize = statistic.getExploredNodes();
      if(i<checkPoints.length && isoSize>=checkPoints[i]){
        if(lInfo!=null){
          traceBusLinkSize = lInfo.getBusLinks();
          tracePedLinkSize = lInfo.getPedLinks();
        }
        logEntries.add(new MemoryEntry(statistic.getExploredContinuousLinks(), statistic.getExploredDiscreteLinks(),isoSize, tracePedLinkSize, traceBusLinkSize, trace.size(),(int)Math.round(node.getDistance()/60)));
        i++;
      }
      expandNode(node);
      maxPrioQueueSize = Math.max(maxPrioQueueSize, priorityQueue.size());// Counter
      node = priorityQueue.poll();
    }
    //logEntries.add(new MemoryEntry(totalIsoSize,statistic.getDiscoveredContinuousLinks(), statistic.getDiscoveredDiscreteLinks(), statistic.getDiscoveredNodes(), tracePedLinkSize, traceBusLinkSize, trace.size(),maxDuration/60));
    terminate();
  }
  
  protected void expandNode(NodeBINE node) {
    lInfo = new LinkInfo();
    super.expandNode(node);
  }
  
  /**
   * 
   * <p>Method getLogEntries</p>
   * @return
   */
  public List<MemoryEntry> getLogEntries() {
    return logEntries;
  }
  
  class LinkInfo {
    int pedLinks, busLinks;

    void addBusLink() {
      busLinks++;
    }
    
    void addPedLink() {
      pedLinks++;
    }
    
    public int getBusLinks() {
      return busLinks;
    }
    
    public int getPedLinks() {
      return pedLinks;
    }
  }

  @Override
  public int getNodeSizeInBytes() {
    return NodeBINE.sizeOf();
  }
  
}
