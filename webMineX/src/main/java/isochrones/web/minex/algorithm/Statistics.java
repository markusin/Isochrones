package isochrones.web.minex.algorithm;

import java.util.HashMap;
import java.util.Map;

public class Statistics {
  
  Map<String, Operation> stats = new HashMap<String, Operation>();
  
  public void addOperation(String name, String description) {
    stats.put(name, new Operation(name, description));
  }
  
  public void append(String name, long time) {
    stats.get(name).append(time);
  }
  
  public void print() {
    
  }
  
  
  public class Operation {
    
    private int totalCalls = 0;
    private long totalTime = 0;
    private String name, description;
    
    /**
     * 
     * <p>Constructs a(n) <code>Operation</code> object.</p>
     * @param name
     */
    public Operation(String name, String description){
      this.name = name;
      this.description = description;
    }
    
    /**
     * 
     * <p>Method append</p>
     * @param time
     */
    public void append(long time){
      totalCalls++;
      totalTime += time;
    }
    
    /**
     * 
     * <p>Method getTotalCalls</p>
     * @return
     */
    public int getTotalCalls() {
      return totalCalls;
    }
    
    /**
     * 
     * <p>Method getTotalTime</p>
     * @return
     */
    public long getTotalTime() {
      return totalTime;
    }
    
    /**
     * 
     * <p>Method getAverageTime</p>
     * @return
     */
    public double getAverageTime() {
      return (double)totalTime/totalCalls;
    }
    
    /**
     * 
     * <p>Method getName</p>
     * @return
     */
    public String getName() {
      return name;
    }
    
    /**
     * 
     * <p>Method getDescription</p>
     * @return
     */
    public String getDescription() {
      return description;
    }
    
  }
  
  

}
