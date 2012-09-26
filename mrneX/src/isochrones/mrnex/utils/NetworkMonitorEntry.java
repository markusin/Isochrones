package isochrones.mrnex.utils;


public class NetworkMonitorEntry {

    
int duration, exploredEdges, loadedWithIER, loadedWithINE, totalLoaded;
  

  public NetworkMonitorEntry(int duaration, int exploredEdges, int loadedWithIER, int loadedWithINE, int totalLoaded) {
    this.duration = duaration;
    this.exploredEdges = exploredEdges;
    this.loadedWithIER = loadedWithIER;
    this.loadedWithINE = loadedWithINE;
    this.totalLoaded = totalLoaded;
  }
  

  public int getDuration() {
    return duration;
  }

  public int getExploredEdges() {
    return exploredEdges;
  }
  
  public int getLoadedWithIER() {
    return loadedWithIER;
  }
  
  public int getLoadedWithINE() {
    return loadedWithINE;
  }
  
  public int getTotalLoaded() {
    return totalLoaded;
  }
  
}
