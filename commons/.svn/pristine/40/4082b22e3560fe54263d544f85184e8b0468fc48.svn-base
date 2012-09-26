/**
 * 
 */
package isochrones.algorithm.statistics;

import isochrones.algorithm.Mode;
import isochrones.network.link.ILink;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * <p>The <code>Statistic</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class Statistic {
  
  protected Map<DBType, Long> runtimesR = new HashMap<DBType, Long>();
  protected Map<DBWType, Long> runtimesW = new HashMap<DBWType, Long>();
  protected Map<DBType, Integer> dbInvokesR = new HashMap<DBType, Integer>();
  protected Map<DBWType, Integer> dbInvokesW = new HashMap<DBWType, Integer>();
  protected Map<Type, Integer> invokes = new HashMap<Type, Integer>();
  protected String algorithmName;
  protected Mode mode;
  protected int maxTraceSize,maxQueueSize,expandedNodes;
  
  protected Set<Integer> exploredNodes = new HashSet<Integer>();
  
  public Statistic(String name, Mode mode) {
    this.algorithmName = name;
    this.mode = mode;
    for(DBType type : DBType.values()){
      dbInvokesR.put(type, 0);
      runtimesR.put(type,0L);
    }
    for(Type type : Type.values()){
      invokes.put(type, 0);
    }
    for(DBWType type : DBWType.values()){
      dbInvokesW.put(type, 0);
      runtimesW.put(type,0L);
    }
  }
  
  public void setMode(Mode mode) {
    this.mode = mode;
  }
  
  public String logRuntime(DBType type, long time) {
    dbInvokesR.put(type, dbInvokesR.get(type)+1);
    runtimesR.put(type, runtimesR.get(type)+time);
    return "";
  }
  
  public void logRuntime(DBWType type, long time) {
    dbInvokesW.put(type, dbInvokesW.get(type)+1);
    runtimesW.put(type, runtimesW.get(type)+time);
  }
  
  /**
   * 
   * <p>Method log</p> increments the specified log entry by one
   * @param type
   */
  public void log(Type type) {
    if(type.equals(Type.EXPANDED_NODES)) 
      expandedNodes++;
    invokes.put(type, invokes.get(type)+1);
    
  }


  /**
   * 
   * <p>Method log</p> 
   * @param link
   * @param type
   */
  public void log(ILink link, Type type) {
    logExploredNode(link.getStartNodeId());
    invokes.put(type, invokes.get(type)+1);
  }

  /**
   * 
   * @param nodeId the id of the node
   */
  public void logExploredNode(int nodeId) {
    if(!exploredNodes.contains(nodeId)){
      log(Type.EXPLORED_NODES);
      exploredNodes.add(nodeId);
    }
  }
  
  /**
   * 
   * <p>Method log</p> increments the specified log entry by the passed value
   * @param type
   * @param value
   */
  public void log(Type type, int value) {
    invokes.put(type, invokes.get(type)+value);
  }

  public long getTotalDBReadingTime() {
    long totalRuntime =0;
    for (long runtime : runtimesR.values()) {
      totalRuntime += runtime;
    }
    return totalRuntime;
  }
  

  public int getTotalDBReadingCalls() {
    int totalInvocation =0;
    for (int invok : dbInvokesR.values()) {
      totalInvocation += invok;
    }
    return totalInvocation;
  }
  

  public long getTotalDBWritingTime() {
    long totalRuntime =0;
    for (long runtime : runtimesW.values()) {
      totalRuntime += runtime;
    }
    return totalRuntime;
  }
  
  
  /**
   * 
   * <p>Method getExploredLinks</p> returns the total number of discovered links
   * @return
   */
  public int getExploredLinks() {
    return getExploredContinuousLinks() + getExploredDiscreteLinks();
  }
  
  public int getExploredContinuousLinks() {
    return invokes.get(Type.EXPLORED_CONTINUOUS_LINKS);
  }
  
  public int getExploredDiscreteLinks() {
    return invokes.get(Type.EXPLORED_SINGLE_DISCRETE_LINK) +  invokes.get(Type.EXPLORED_HETERO_DISCRETE_LINKS) +invokes.get(Type.EXPLORED_HOMO_DISCRETE_LINKS);
  }
  
  /**
   * <p>
   * Method getExploredNodes
   * </p>
   * 
   * @return
   */
  public int getExploredNodes() {
    // return invokes.get(Type.ADDED_NODES);
    return exploredNodes.size();
    // return invokes.get(Type.EXPLORED_NODES);
  }
  
  public int getAddedNodes() {
    return invokes.get(Type.LOADED_NODES);
  }
  
  /**
   * 
   * <p>Method getExpandedNodes</p> return the number of expanded nodes, which represents the number of expanded nodes.
   * @return the number of expanded nodes
   */
  public int getExpandedNodes() {
    return invokes.get(Type.EXPANDED_NODES);
  }
  
  
  
  /**
   * 
   * <p>Method getLoadedContinuousEdges</p>
   * @return
   */
  public int getLoadedContinuousEdges(){
    return invokes.get(Type.LOADED_CONTINUOUS_LINKS);
  }
  
  /**
   * 
   * <p>Method getLoadedDiscreteEdges</p>
   * @return
   */
  public int getLoadedDiscreteEdges(){
    return invokes.get(Type.LOADED_DISCRETE_LINKS);
  }
  
  /**
   * 
   * <p>Method getUnexploredLinks</p>
   * @return
   */
  public int getUnexploredLinks(){
    return invokes.get(Type.UNEXPLORED_LINKS);
  }
  
  /**
   * 
   * <p>Method getMaxTraceSize</p>
   * @return
   */
  public int getMaxTraceSize(){
    return maxTraceSize;
  }
  
  /**
   * 
   * <p>Method getMaxQueueSize</p>
   * @return
   */
  public int getMaxQueueSize(){
    return maxQueueSize;
  }
  
  /**
   * 
   * <p>Method setSizeValues</p> sets the maximal size values
   * @param maxTraceSize the maximal size of the trace
   * @param maxQueueSize the maximal size of the queue
   */
  public void setSizeValues(int maxTraceSize, int maxQueueSize){
    this.maxTraceSize = maxTraceSize;
    this.maxQueueSize = maxQueueSize;
  }
  
  protected void printHeader() {
    System.out.println("---------------------------------------------------------------------------------");
    System.out.format("| %-77s |\n", algorithmName + " STATISTICAL DATA");
    System.out.println("---------------------------------------------------------------------------------");
  }
  
  protected void printSummary(){
    System.out.format("| %77s |\n", "");
    System.out.format("| %-77s |\n", "NETWORK EXPLORATION: ");
    System.out.format("| %77s |\n", "");
    System.out.println("-------------- Link Info --------------------------------------------------------");
    System.out.format("| %-60s : %10d %3s |\n", "Number of explored continuous links",getExploredContinuousLinks(), "");
    System.out.format("| %-60s : %10d %3s |\n", "Number of loaded continuous links",invokes.get(Type.LOADED_CONTINUOUS_LINKS), "");
    System.out.format("| %-60s : %10d %3s |\n", "Number of added continuous links",invokes.get(Type.ADDED_CONTINUOUS_LINKS), "");
    if (mode == Mode.MULTIMODAL) {
      System.out.format("| %-60s : %10d %3s |\n", "Number of explored discrete links", getExploredDiscreteLinks(), "");
      System.out.format("| %-60s : %10d %3s |\n", "Number of loaded discrete links",invokes.get(Type.LOADED_DISCRETE_LINKS), "");
      System.out.format("| %-60s : %10d %3s |\n", "Number of added discrete links", invokes.get(Type.ADDED_DISCRETE_LINKS), "");
    }
    System.out.println("-------------- Node Info --------------------------------------------------------");
    System.out.format("| %-60s : %10d %3s |\n", "Number of expanded nodes", invokes.get(Type.EXPANDED_NODES), "");
    System.out.format("| %-60s : %10d %3s |\n", "Number of explored nodes", invokes.get(Type.EXPLORED_NODES), "");
    System.out.format("| %-60s : %10d %3s |\n", "Number of loaded nodes", invokes.get(Type.LOADED_NODES), "");
    System.out.format("| %-60s : %10d %3s |\n", "Number of removed nodes", invokes.get(Type.REMOVED_NODES), "");
    System.out.format("| %-60s : %10d %3s |\n", "Number of remaining nodes", invokes.get(Type.REMAINING_NODES), "");
    System.out.println("---------------------------------------------------------------------------------");
  }
  
  protected void printTail() {
    System.out.println("---------------------------------------------------------------------------------");
  }
  
  protected void printDBSummary() {
    System.out.format("| %77s |\n", "");
    System.out.format("| %-77s |\n", "DATABASE OPERATIONS - summary ");
    System.out.format("| %77s |\n", "");
    System.out.format("| %-60s : %10d %3s |\n", "Total number of database readings", getTotalDBReadingCalls(), "");
    System.out.format("| %-60s : %10d %3s |\n", "Total database reading time", getTotalDBReadingTime(), "ms");
    System.out.format("| %77s |\n", "");
    long totalDBWritingTime = getTotalDBWritingTime();
    if(totalDBWritingTime>0){
      System.out.format("| %-60s : %10d %3s |\n", "Total database writing time", totalDBWritingTime, "ms");
      System.out.format("| %77s |\n", "");
    }
    System.out.println("---------------------------------------------------------------------------------");
  }

  protected void printDBDetails(){
    System.out.format("| %77s |\n", "");
    System.out.format("| %-77s |\n", "DATABASE OPERATIONS - details: ");
    System.out.format("| %77s |\n", "");
    System.out.println("-------------- Pedestrian Network Expansion -------------------------------------");
    int dbInvocation = dbInvokesR.get(DBType.GET_CONTINUOUS_LINK) + dbInvokesR.get(DBType.GET_LINKS);
    long runtime = runtimesR.get(DBType.GET_CONTINUOUS_LINK) + runtimesR.get(DBType.GET_LINKS); 
    System.out.format("| %-60s : %10d %3s |\n", "Total number of \"getIncidentLinks()\" calls", dbInvocation , "");
    System.out.format("| %-60s : %10d %3s |\n", "Total time \"getIncidentLinks()\"", runtime, "ms");
    float avg = (float) runtime / (float) dbInvocation;
    System.out.format("| %-60s : %10f %3s |\n", "Avg time for \"getIncidentLinks()\"", avg, "ms");
    if (mode == Mode.MULTIMODAL) {
      printDBScheduleLookupDetails();
      if(getUnexploredLinks()>0){
        System.out.println("---------------------------------------------------------------------------------");
        System.out.format("| %-60s : %10d %3s |\n", "Unexplored links fetched",
            getUnexploredLinks(), "");
      }
    }
  }
  
  protected void printDBScheduleLookupDetails() {
    System.out.println("-------------- Transportation Network Expansion ---------------------------------");
    System.out.format("| %77s |\n", "");
    
    int callSHomoLExact =  dbInvokesR.get(DBType.GET_DISCRETE_HOMO_COST);
    long runtimeSHomoLExact =  runtimesR.get(DBType.GET_DISCRETE_HOMO_COST);
    
    int callSHeteroLExact =  dbInvokesR.get(DBType.GET_DISCRETE_HETERO_COST);
    long runtimeSHeteroLExact =  runtimesR.get(DBType.GET_DISCRETE_HETERO_COST);
    
    System.out.format("| %-60s : %10d %3s |\n", "Total calls of schedule lookups",
        callSHomoLExact + callSHeteroLExact, "");
    System.out.format("| %-60s : %10d %3s |\n", "Total time of schedule lookups",
        runtimeSHomoLExact + runtimeSHeteroLExact , "ms");
    System.out.println("---------------------------------------------------------------------------------");
    System.out.format("| %-77s |\n", "Details");
    System.out.format("| %77s |\n", "");
    System.out.format("| %-60s : %10d %3s |\n", "Total calls of homogenous schedule lookups",
        callSHomoLExact ,"");
    System.out.format("| %-60s : %10d %3s |\n", "Total time of homogenous schedule lookups",
        runtimeSHomoLExact, "ms");
    System.out.format("| %-60s : %10f %3s |\n", "Avg DB read. time of homogenous schedule lookups",
        (float) runtimeSHomoLExact / (float) callSHomoLExact, "ms");
    System.out.format("| %77s |\n", "");
    System.out.format("| %-60s : %10d %3s |\n", "Total calls of heterogenous schedule lookups",
        callSHeteroLExact ,"");
    System.out.format("| %-60s : %10d %3s |\n", "Total time of heterogenous schedule lookups",
        runtimeSHeteroLExact, "ms");
    System.out.format("| %-60s : %10f %3s |\n", "Avg DB read. time of heterogenous schedule lookups",
        (float) runtimeSHeteroLExact / (float) callSHeteroLExact, "ms");
    System.out.format("| %77s |\n", "");
  }
  
  public void print() {
    printHeader();
    printSummary();
    printDBSummary();
    printDBDetails();
    printTail();
  }
  
  public long getScheduleHomogeneousLookupTime() {
    return runtimesR.get(DBType.GET_DISCRETE_HOMO_COST);
  }
  
  public int getScheduleHomogeneousLookups() {
    return dbInvokesR.get(DBType.GET_DISCRETE_HOMO_COST);
  }
  
  public long getScheduleHeterogenousLookupTime() {
    return runtimesR.get(DBType.GET_DISCRETE_HETERO_COST);
  }
  
  public int getScheduleHeterogenousLookups() {
    return dbInvokesR.get(DBType.GET_DISCRETE_HETERO_COST);
  }
  
  public Map<Type, Integer> getInvokes() {
    return invokes;
  }
  
}
