package isochrones.utils.entries;

import java.util.HashMap;
import java.util.Map;

public class RuntimeDetailedEntry {
  
  Map<Detail,Long> runtimes = new HashMap<Detail,Long>();
  Map<Detail,Integer> lookups = new HashMap<Detail,Integer>();
  Map<Detail,Integer> loaded = new HashMap<Detail,Integer>();
  
  
  public RuntimeDetailedEntry(Map<Detail,Long> runtimes,  Map<Detail,Integer> lookups, Map<Detail,Integer> loaded) {
    this.runtimes = runtimes;
    this.lookups = lookups;
    this.loaded = loaded;
  }
  
  public RuntimeDetailedEntry() {}
  

  public long getRuntime(Detail detailType){
    switch (detailType) {
      case NETWORK:
        return runtimes.get(Detail.RANGEQUERY) + runtimes.get(Detail.POINTQUERY) + runtimes.get(Detail.POINTQUERY_PRE) + runtimes.get(Detail.LOCATIONQUERY) + runtimes.get(Detail.DENSITYQUERY);
      case SCHEDULE:
        return runtimes.get(Detail.SCHEDULEHOMO) + runtimes.get(Detail.SCHEDULEHETERO);
      default:
        break;
    }
    return runtimes.get(detailType);
  }
  
  public Map<Detail,Long> getRuntimes() {
    return runtimes;
  }
  
 public Map<Detail, Integer> getLookups() {
  return lookups;
}
 
 public void addLookups(Map<Detail,Integer> lookups){
   this.lookups = lookups;
 }
 
 public void addLoaded(Map<Detail,Integer> loaded){
   this.loaded = loaded;
 }
  
  public int getLookup(Detail detailType){
    switch (detailType) {
      case NETWORK:
        return lookups.get(Detail.RANGEQUERY) + lookups.get(Detail.POINTQUERY) + lookups.get(Detail.POINTQUERY_PRE);
      case SCHEDULE:
        return lookups.get(Detail.SCHEDULEHOMO) + lookups.get(Detail.SCHEDULEHETERO);
      default:
        break;
    }
    return lookups.get(detailType);
  }
  
  public Map<Detail, Integer> getLoaded() {
    return loaded;
  }
  
  public Integer getLoaded(Detail detailType) {
    return loaded.get(detailType);
  }

  public double getAverage(Detail detailType){
    Long runtime = runtimes.get(detailType);
    Integer lookup = lookups.get(detailType);
    if(runtime!=null && lookup!=null){
      return (double)runtime / lookup; 
    }
    return 0;
  }
  
}
