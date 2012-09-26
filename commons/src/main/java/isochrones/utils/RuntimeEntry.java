package isochrones.utils;

public class RuntimeEntry {
  
  protected long runtime, rangeQueryRuntime = Long.MIN_VALUE,pointQueryRuntime = Long.MIN_VALUE;
  protected double checkpoint;
  protected int discoveredSize;
  
  public RuntimeEntry(long runtime, int discoveredSize) {
    this.runtime = runtime;
    this.discoveredSize = discoveredSize;
  }
    
  public RuntimeEntry(long runtime, int discoveredSize, double checkpoint) {
    this.runtime = runtime;
    this.discoveredSize = discoveredSize;
    this.checkpoint = checkpoint;
  }
  
  public long getRuntime() {
    return runtime;
  }
  
  public int getDiscoveredSize() {
    return discoveredSize;
  }
  
  public double getDurationCheckPoint() {
    return checkpoint;
  }
  
  
  
  @Override
  public String toString() {
    return "Runtime: " + getRuntime() + "\t discovered size: " + getDiscoveredSize() + "\t duration: " + getDurationCheckPoint();
  }

}
