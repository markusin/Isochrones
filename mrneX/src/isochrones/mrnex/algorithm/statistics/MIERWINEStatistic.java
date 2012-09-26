package isochrones.mrnex.algorithm.statistics;

import isochrones.algorithm.Mode;
import isochrones.algorithm.statistics.DBType;
import isochrones.algorithm.statistics.Type;

public class MIERWINEStatistic extends isochrones.algorithm.statistics.Statistic {

  long minIERTime = Long.MAX_VALUE, maxIERTime = Long.MIN_VALUE;

  public MIERWINEStatistic(String name, Mode mode) {
    super(name, mode);
  }

  @Override
  public void printDBDetails() {
    System.out.format("| %77s |\n", "");
    System.out.format("| %-77s |\n", "DATABASE OPERATIONS - details: ");
    System.out.format("| %77s |\n", "");
    System.out.println("-------------- Pedestrian Network Expansion -------------------------------------");
    System.out.format("| %77s |\n", "");
    int dbInvocation = dbInvokesR.get(DBType.GET_IER_LINKS) + totalCallsPreINE()
        + dbInvokesR.get(DBType.GET_CONTINUOUS_LINK) + dbInvokesR.get(DBType.GET_LINKS)
        + dbInvokesR.get(DBType.GET_COORDINATE);
    long runtime = runtimesR.get(DBType.GET_IER_LINKS) + totalTimePreINE() + runtimesR.get(DBType.GET_CONTINUOUS_LINK)
        + runtimesR.get(DBType.GET_LINKS) + runtimesR.get(DBType.GET_COORDINATE);
    float avg = (float) runtime / (float) dbInvocation;
    System.out.format("| %-60s : %10d %3s |\n", "Total number of DB lookup calls", dbInvocation, "");
    System.out.format("| %-60s : %10d %3s |\n", "Total time of DB lookup calls", runtime, "ms");
    System.out.format("| %-60s : %10f %3s |\n", "Avg time for DB lookup", avg, "ms");
    System.out.println("-------------- IER Queries -------------------------------------");
    dbInvocation = dbInvokesR.get(DBType.GET_IER_LINKS);
    runtime = runtimesR.get(DBType.GET_IER_LINKS);
    System.out.format("| %-60s : %10d %3s |\n", "Total number of \"getIER()\" calls", dbInvocation, "");
    System.out.format("| %-60s : %10d %3s |\n", "Total time \"getIER()\"", runtime, "ms");
    avg = (float) runtime / (float) dbInvocation;
    System.out.format("| %-60s : %10f %3s |\n", "Avg time for \"getIER()\"", avg, "ms");
    System.out.format("| %-60s : %10d %3s |\n", "Total number of fetched edges",
        invokes.get(Type.LOADED_LINKS_WITH_IER), "");
    System.out.println("-------------- INE Queries -------------------------------------");
    dbInvocation = dbInvokesR.get(DBType.GET_CONTINUOUS_LINK) + dbInvokesR.get(DBType.GET_INE_LINKS);
    runtime = runtimesR.get(DBType.GET_CONTINUOUS_LINK) + runtimesR.get(DBType.GET_INE_LINKS);
    System.out.format("| %-60s : %10d %3s |\n", "Total number of \"getINE()\" calls", dbInvocation, "");
    System.out.format("| %-60s : %10d %3s |\n", "Total time \"getINE()\"", runtime, "ms");
    avg = (float) runtime / (float) dbInvocation;
    System.out.format("| %-60s : %10f %3s |\n", "Avg time for \"getINE()\"", avg, "ms");
    System.out.format("| %-60s : %10d %3s |\n", "Total number of fetched edges",
        invokes.get(Type.LOADED_LINKS_WITH_INE), "");
    System.out.println("---------------------------------------------------------------------------------");
    System.out.format("| %-60s : %10d %3s |\n", "Total number of pre \"getINE()\" calls", totalCallsPreINE(), "");
    System.out.format("| %-60s : %10d %3s |\n", "Total time pre \"getINE()\"", totalTimePreINE(), "ms");
    avg = (float) totalTimePreINE() / (float) totalCallsPreINE();
    System.out.format("| %-60s : %10f %3s |\n", "Avg time for pre \"getINE()\"", avg, "ms");

    System.out.println("---------------------------------------------------------------------------------");
    if (mode.equals(Mode.MULTIMODAL)) {
      dbInvocation = dbInvokesR.get(DBType.GET_COORDINATE);
      runtime = runtimesR.get(DBType.GET_COORDINATE);
      System.out.format("| %-60s : %10d %3s |\n", "Total number of coordinate lookups calls", dbInvocation, "");
      System.out.format("| %-60s : %10d %3s |\n", "Total time of coordinate lookups calls", runtime, "ms");
      avg = (float) runtime / (float) dbInvocation;
      System.out.format("| %-60s : %10f %3s |\n", "Avg time of getCoordinate lookups calls", avg, "ms");
      System.out.println("---------------------------------------------------------------------------------");
      dbInvocation = dbInvokesR.get(DBType.GET_RANGE_FROM_SIZE);
      runtime = runtimesR.get(DBType.GET_RANGE_FROM_SIZE);
      System.out.format("| %-60s : %10d %3s |\n", "Total number of range lookups calls", dbInvocation, "");
      System.out.format("| %-60s : %10d %3s |\n", "Total time of range lookups calls", runtime, "ms");
      avg = (float) runtime / (float) dbInvocation;
      System.out.format("| %-60s : %10f %3s |\n", "Avg time of range lookups calls", avg, "ms");
      printDBScheduleLookupDetails();

    }
  }

  /**
   * <p>
   * Method getEdgesLoadedWithIER
   * </p>
   * 
   * @return the number of edges loaded with a range query
   */
  public int getEdgesLoadedWithIER() {
    return invokes.get(Type.LOADED_LINKS_WITH_IER);
  }

  /**
   * <p>
   * Method totalTimeIER
   * </p>
   * 
   * @return
   */
  public long totalTimeIER() {
    return runtimesR.get(DBType.GET_IER_LINKS);
  }

  public int totalCallsIER() {
    return dbInvokesR.get(DBType.GET_IER_LINKS);
  }

  /**
   * <p>
   * Method getEdgesLoadedWithINE
   * </p>
   * 
   * @return the number of edges loaded with a point query
   */
  public int getEdgesLoadedWithINE() {
    return invokes.get(Type.LOADED_LINKS_WITH_INE);
  }

  /**
   * <p>
   * Method totalTimeINE
   * </p>
   * 
   * @return
   */
  public long totalTimeINE() {
    return runtimesR.get(DBType.GET_INE_LINKS);
  }

  public int totalCallsINE() {
    return dbInvokesR.get(DBType.GET_INE_LINKS);
  }

  /**
   * <p>
   * Method totalTimeINE
   * </p>
   * 
   * @return
   */
  public long totalTimePreINE() {
    return runtimesR.get(DBType.GET_INE_PRE_LINKS);
  }

  public int totalCallsPreINE() {
    return dbInvokesR.get(DBType.GET_INE_PRE_LINKS);
  }

  public long getRangeLookupTime() {
    return runtimesR.get(DBType.GET_RANGE_FROM_SIZE);
  }

  public int getRangeLookups() {
    return dbInvokesR.get(DBType.GET_RANGE_FROM_SIZE);
  }

  public long getLocationLookupTime() {
    return runtimesR.get(DBType.GET_COORDINATE);
  }

  public int getLocationLookups() {
    return dbInvokesR.get(DBType.GET_COORDINATE);
  }

  /**
   * <p>
   * Method logDBIER
   * </p>
   * 
   * @param typeIERLinks
   * @param time
   */
  public void logDBIER(DBType typeIERLinks, long time) {
    minIERTime = Math.min(minIERTime, time);
    maxIERTime = Math.max(maxIERTime, time);
    logRuntime(typeIERLinks, time);
  }
  
  public long getMaxIERTime() {
    return maxIERTime;
  }
  
  public long getMinIERTime() {
    return minIERTime;
  }

}
