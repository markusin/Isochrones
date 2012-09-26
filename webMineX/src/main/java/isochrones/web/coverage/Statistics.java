package isochrones.web.coverage;

import org.json.JSONException;
import org.json.JSONObject;

public class Statistics {

  private int totalInhabitants;
  private int reachedInhabitants;
  private int totalBuildings;
  private int reachedBuildings;
  private long isoAreaInitTime, isoAreaCalculationTime, statsComputationTime;
  private long isoAreaBufferQueryTime;

  public Statistics(int totalInhabitants, int reachedInhabitants) {
    this.totalInhabitants = totalInhabitants;
    this.reachedInhabitants = reachedInhabitants;
  }

  public int getTotalInhabitants() {
    return totalInhabitants;
  }

  public int getReachedInhabitants() {
    return reachedInhabitants;
  }

  public double getAverageReachedInhabitants() {
    return (double)reachedInhabitants / totalInhabitants * 100;
  }

  public JSONObject toJSON() {
    JSONObject root = new JSONObject();
    try {
      root.put("reachedInhabitants", reachedInhabitants);
      root.put("averageInhabitants", (int) getAverageReachedInhabitants());
      root.put("totalInhabitants", totalInhabitants);
      root.put("isoAreaInitTime", isoAreaInitTime);
      root.put("isoAreaCalculationTime", isoAreaCalculationTime);
      root.put("statsComputationTime", statsComputationTime);
      root.put("isoAreaBufferQueryTime", isoAreaBufferQueryTime);
      root.put("isoAreaInitTime", isoAreaInitTime);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return root;
  }
  
  public void setComputationTime(long statsComputationTime) {
    this.statsComputationTime = statsComputationTime;
  }
  
  public void setIsoAreaInitializationTime(long isoAreaInitTime) {
    this.isoAreaInitTime = isoAreaInitTime;
  }
  
  public void setIsoAreaCalculationTime(long isoAreaCalculationTime) {
    this.isoAreaCalculationTime = isoAreaCalculationTime;
  }
  
  public void setIsoAreaBufferQueryTime(long isoAreaBufferQueryTime) {
    this.isoAreaBufferQueryTime = isoAreaBufferQueryTime;
  }
  
 
  

}
