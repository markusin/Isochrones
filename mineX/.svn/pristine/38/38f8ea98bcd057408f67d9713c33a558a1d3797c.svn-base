package isochrones.minex.algorithm.test;

import isochrones.minex.algorithm.MineX;
import isochrones.minex.network.node.NodeBINE;
import isochrones.network.Location;
import isochrones.utils.Config;

import java.util.Calendar;
import java.util.SortedMap;
import java.util.TreeMap;

public class TraceTest extends MineX {
  
  SortedMap<Integer, ExperimentEntry> experiments = new TreeMap<Integer, ExperimentEntry>();

  public TraceTest(Config config) {
    super(config);
  }
  
  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed,
                               Calendar targetTime) {

    long start = System.currentTimeMillis();
    setParameters(locations, duration, walkingSpeed, targetTime,trace);
    /*
     * Starts isochrone computation by exploring the links the query points are situated on.
     */
    exploreInitialLocations();
    NodeBINE node = priorityQueue.poll(); // Dequeues the first node from priority queue
    // testing variables
    int dur; 
    boolean firstOccurences = true;
    int fromDur=1, toDur=5;

    while (node != null) {
      int traceSize = trace.size();
      dur = (int)node.getDistance()/60;
      if(dur>=fromDur) {
        if(dur<toDur){
          if(firstOccurences) {
            long runTime = System.currentTimeMillis()-start;
            experiments.put(dur, new ExperimentEntry(runTime, traceSize));
            firstOccurences = false;
          }
        } else {
          long runTime = System.currentTimeMillis()-start;
          experiments.put(toDur, new ExperimentEntry(runTime, traceSize));
          if(fromDur==1) {
            fromDur = 5;
            toDur = 10;
          } else if(fromDur>1 && fromDur<=5) {
            fromDur+=5;
            toDur += 10;
          } else {
            fromDur +=10;
            toDur = fromDur+10;
          }
        }
      }
      expandNode(node);
      maxPrioQueueSize = Math.max(maxPrioQueueSize, priorityQueue.size());// Counter
      node = priorityQueue.poll(); // Dequeues the next node
    }
    terminate();
  }
  
  public class ExperimentEntry {
    long runTime;
    int memorySize;
    
    public ExperimentEntry(long runTime,  int memorySize) {
      this.runTime = runTime;
      this.memorySize = memorySize;
    }
    
    public long getRunTime() {
      return runTime;
    }
    
    public int getMemorySize() {
      return memorySize;
    }
  }
  
  public SortedMap<Integer, ExperimentEntry> getExperiments() {
    return experiments;
  }
  

}
