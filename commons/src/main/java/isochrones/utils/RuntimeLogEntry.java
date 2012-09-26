package isochrones.utils;

import isochrones.algorithm.Dataset;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class RuntimeLogEntry implements Comparable<RuntimeLogEntry> {

  Map<Integer, Long> durationRunTimes = new HashMap<Integer, Long>();
  int invoked;
  Integer reachabiltyRatio;
  int linkId;

  public RuntimeLogEntry(int linkId, Dataset dSet) {
    this.linkId = linkId;
    durationRunTimes.put(1, 0L);
    durationRunTimes.put(5, 0L);
    durationRunTimes.put(10, 0L);
    durationRunTimes.put(15, 0L);
    durationRunTimes.put(20, 0L);
    durationRunTimes.put(25, 0L);
    durationRunTimes.put(30, 0L);
    durationRunTimes.put(40, 0L);
    durationRunTimes.put(50, 0L);
    durationRunTimes.put(60, 0L);
    durationRunTimes.put(70, 0L);
    durationRunTimes.put(80, 0L);
    durationRunTimes.put(90, 0L);
    if (dSet.equals(Dataset.SF) || dSet.equals(Dataset.WDC)) {
      durationRunTimes.put(100, 0L);
      durationRunTimes.put(110, 0L);
      durationRunTimes.put(120, 0L);
      durationRunTimes.put(130, 0L);
      durationRunTimes.put(140, 0L);
    }
  }

  public int getLinkId() {
    return linkId;
  }

  public void addEntry(Integer duration, long time) {
    int dur = findClosestDuration(duration, durationRunTimes.keySet());
    Long t = durationRunTimes.get(dur);
    if (t == null) {
      t = time;
    } else {
      t += time;
    }
    durationRunTimes.put(dur, t);
  }

  public void incrementInvocation() {
    invoked++;
  }

  public int getInvocations() {
    return invoked;
  }

  /**
   * <p>
   * Method getAverageRuntime
   * </p>
   * 
   * @param duration
   * @return
   */
  public long getAverageRuntime(int duration) {
    Long time = durationRunTimes.get(duration);
    if (time != null)
      return durationRunTimes.get(duration) / invoked;
    else
      return -1;
  }

  /**
   * <p>
   * Method getTotalRuntime
   * </p>
   * 
   * @param duration
   * @return
   */
  public long getTotalRuntime(int duration) {
    return durationRunTimes.get(duration);
  }

  public void setReachabiltyRatio(Integer reachabiltyRatio) {
    this.reachabiltyRatio = reachabiltyRatio;
  }

  public Integer getReachabiltyRatio() {
    return reachabiltyRatio;
  }

  @Override
  public int compareTo(RuntimeLogEntry other) {
    final int less = -1;
    final int equal = 0;
    final int greater = 1;

    if (this == other) {
      return equal;
    }

    int value = Double.compare(this.getReachabiltyRatio(), other.getReachabiltyRatio());

    if (value < 0) {
      return less;
    }
    if (value > 0) {
      return greater;
    }
    if(this.linkId<other.linkId) {
      return less; 
    } else return greater;
  }
  
  @Override
  public boolean equals(Object other) {
    if(other==null || !(other instanceof RuntimeLogEntry)) return false;
    return ((RuntimeLogEntry)other).getLinkId()==getLinkId();
  }
  
  @Override
  public int hashCode() {
    return getLinkId();
  }
  
  private int findClosestDuration(Integer duration, Set<Integer> durations) {
    int closestDur = Integer.MAX_VALUE;
    double diff = Double.MAX_VALUE;
    for (Integer dur : durations) {
      double d = Math.abs((double) dur - (double) duration);
      if (d < diff) {
        diff = d;
        closestDur = dur;
      }
    }
    return closestDur;
  }

}
