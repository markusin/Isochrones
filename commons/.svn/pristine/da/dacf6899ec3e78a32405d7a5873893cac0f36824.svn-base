package isochrones.utils;

import java.util.Calendar;

public class ReachabilityEntry implements Comparable<ReachabilityEntry> {
  int duration;
  int numberOfNodes;
  int numberOfPedLinks;
  int numberOfBusLinks;
  Calendar arrivalTime;
  
  

  public ReachabilityEntry(int duration, int numberOfNodes, int numberOfPedLinks, int numberOfBusLinks) {
    super();
    this.duration = duration;
    this.numberOfNodes = numberOfNodes;
    this.numberOfPedLinks = numberOfPedLinks;
    this.numberOfBusLinks = numberOfBusLinks;
  }



  public int getDuration() {
    return duration;
  }



  public int getNumberOfNodes() {
    return numberOfNodes;
  }



  public int getNumberOfPedLinks() {
    return numberOfPedLinks;
  }



  public int getNumberOfBusLinks() {
    return numberOfBusLinks;
  }
  
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }
  
  @Override
  public int hashCode() {
    return duration;
  }
  
  



  @Override
  public int compareTo(ReachabilityEntry other) {
    final int BEFORE = -1;
    final int EQUAL = 0;
    final int AFTER = 1;

    if (this == other) {
        return EQUAL;
    }

    int value = Double.compare(this.duration, duration);

    if (value < 0) {
        return BEFORE;
    }
    if (value > 0) {
        return AFTER;
    }
    return EQUAL;
  }
  
}
