package isochrones.mrnex.algorithm.test;

import isochrones.algorithm.test.IRuntimeDurationTestDetailed;
import isochrones.mrnex.algorithm.MRNEX;
import isochrones.mrnex.algorithm.statistics.MIERWINEStatistic;
import isochrones.mrnex.network.node.ANode;
import isochrones.network.Location;
import isochrones.utils.Config;
import isochrones.utils.entries.Detail;
import isochrones.utils.entries.RuntimeDetailedEntry;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Markus Innerebner
 * @version 3.0
 */
public class RuntimeDurationTestDetailed extends MRNEX implements IRuntimeDurationTestDetailed {

  SortedMap<Integer, RuntimeDetailedEntry> runTimes = new TreeMap<Integer, RuntimeDetailedEntry>();
  long start;

  public RuntimeDurationTestDetailed(Config config) {
    super(config);
  }

  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed,
                               Calendar targetTime, int[] checkPoints) {
    start = System.currentTimeMillis();
    super.computeIsochrone(locations, duration, walkingSpeed, targetTime);
  }

  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime, int[] checkPoints) {
    start = System.currentTimeMillis();
    super.computeIsochrone(nodeIds, duration, walkingSpeed, targetTime);
  }

  @Override
  public void compute() {
    ANode node = priorityQueue.poll();
    while (node != null) {
      expandNode(node);
      node = priorityQueue.poll();
    }
    long runtime = System.currentTimeMillis() - start;
    System.out.println(maxDuration / 60 + "\t" + runtime);
    MIERWINEStatistic stat = getStatistic();
    HashMap<Detail,Long> runtimes = new HashMap<Detail, Long>();
    HashMap<Detail,Integer> lookups = new HashMap<Detail, Integer>();
    Map<Detail,Integer> loaded = new HashMap<Detail,Integer>();
    
    runtimes.put(Detail.TOTAL,runtime);
    runtimes.put(Detail.TOTAL_DB, stat.getTotalDBReadingTime());
    lookups.put(Detail.TOTAL_DB, stat.getTotalDBReadingCalls());
    runtimes.put(Detail.RANGEQUERY, stat.totalTimeIER());
    lookups.put(Detail.RANGEQUERY, stat.totalCallsIER());
    runtimes.put(Detail.RANGEQUERY_MIN, stat.getMinIERTime());
    runtimes.put(Detail.RANGEQUERY_MAX, stat.getMaxIERTime());
    loaded.put(Detail.RANGEQUERY, stat.getEdgesLoadedWithIER());
    runtimes.put(Detail.POINTQUERY, stat.totalTimeINE());
    lookups.put(Detail.POINTQUERY, stat.totalCallsINE());
    loaded.put(Detail.POINTQUERY, stat.getEdgesLoadedWithINE());
    runtimes.put(Detail.POINTQUERY_PRE, stat.totalTimePreINE());
    lookups.put(Detail.POINTQUERY_PRE, stat.totalCallsPreINE());
    runtimes.put(Detail.LOCATIONQUERY, stat.getLocationLookupTime());
    lookups.put(Detail.LOCATIONQUERY, stat.getLocationLookups());
    runtimes.put(Detail.GET_RANGE_FROM_SIZE, stat.getRangeLookupTime());
    lookups.put(Detail.GET_RANGE_FROM_SIZE, stat.getRangeLookups());
    runtimes.put(Detail.SCHEDULEHOMO, stat.getScheduleHomogeneousLookupTime());
    lookups.put(Detail.SCHEDULEHOMO, stat.getScheduleHomogeneousLookups());
    runtimes.put(Detail.SCHEDULEHETERO, stat.getScheduleHeterogenousLookupTime());
    lookups.put(Detail.SCHEDULEHETERO, stat.getScheduleHeterogenousLookups());
    
    RuntimeDetailedEntry runtimeEntry = new RuntimeDetailedEntry(runtimes,lookups,loaded);
    runTimes.put(maxDuration / 60, runtimeEntry);
    terminate();
  }

  @Override
  public SortedMap<Integer, RuntimeDetailedEntry> getLogEntries() {
    return runTimes;
  }

  @Override
  public Config getConfig() {
    return config;
  }

}
