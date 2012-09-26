package isochrones.minex.utils;

import isochrones.minex.algorithm.test.RuntimeDurationTest;
import isochrones.network.Location;
import isochrones.network.QueryPoint;
import isochrones.utils.BenchmarkUtil;
import isochrones.utils.DBUtility;
import isochrones.utils.RuntimeEntry;
import isochrones.utils.RuntimeLogEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Markus Innerebner
 */
public class ConfidenceIntervalExperiment {

  Map<Integer, QueryPoint> queryPoints = new HashMap<Integer, QueryPoint>();
  SortedMap<Integer, RuntimeLogEntry> runtimeLogger = new TreeMap<Integer, RuntimeLogEntry>();
  Map<Integer, Integer> qpInvocationLogger = new HashMap<Integer, Integer>();
  List<Integer> durations = new ArrayList<Integer>();

  int[] slotter;
  int frequency, numberOfInvocations;
  private BenchmarkUtil util;

  public ConfidenceIntervalExperiment(BenchmarkUtil util) {
    this.frequency = util.getFrequency();
    this.queryPoints = util.loadQueryPoints(util.getConfig().getConnection());
    this.util = util;
    slotter = new int[queryPoints.size()];
    int i = 0;
    for (QueryPoint qPoint : queryPoints.values()) {
      qpInvocationLogger.put(qPoint.getId(), 0);
      slotter[i++] = qPoint.getId();
    }
  }

  private void logInFiles(String directory, String fileName) throws IOException {
    Writer output = null;
    File dir = new File(directory);
    dir.mkdir();
    File file = new File(directory + fileName);
    output = new BufferedWriter(new FileWriter(file));
    // sort by reachability
    SortedMap<Integer, SortedSet<RuntimeLogEntry>> reachRatios = new TreeMap<Integer, SortedSet<RuntimeLogEntry>>();

    for (int linkId : runtimeLogger.keySet()) {
      Integer reachKey = runtimeLogger.get(linkId).getReachabiltyRatio();
      if (!reachRatios.containsKey(reachKey)) {
        reachRatios.put(reachKey, new TreeSet<RuntimeLogEntry>());
      }
      SortedSet<RuntimeLogEntry> entries = reachRatios.get(reachKey);
      entries.add(runtimeLogger.get(linkId));
      reachRatios.put(reachKey, entries);
    }
    StringBuilder bF = new StringBuilder();
    bF.append("dMax");
    for (Integer reachKey : reachRatios.keySet()) {
      for (RuntimeLogEntry rEntry : reachRatios.get(reachKey)) {
        bF.append("\t").append(rEntry.getLinkId()).append("(").append(reachKey).append(")");
      }
    }
    bF.append("\n");
    for (int duration : durations) {
      bF.append(duration); // add in first column the duration
      for (Integer linkId : reachRatios.keySet()) {
        for (RuntimeLogEntry rEntry : reachRatios.get(linkId)) {
          bF.append("\t").append(rEntry.getAverageRuntime(duration));
        }
      }
      bF.append("\n");
    }

    output.write(bF.toString());
    output.close();
    System.out.println("Your file has been written into " + file.getAbsolutePath());
  }

  /**
   * <p>
   * Method getRandomQueryPoint
   * </p>
   * 
   * @return
   */
  private QueryPoint getRandomQueryPoint() {
    if (numberOfInvocations >= frequency * queryPoints.size())
      return null;
    int pos = (int) Math.round(Math.random() * (queryPoints.size() - 1));
    int linkId = slotter[pos];
    Integer invoked = qpInvocationLogger.get(linkId);
    while (invoked >= frequency) {
      if (numberOfInvocations >= frequency * queryPoints.size())
        return null;
      pos = (int) Math.round(Math.random() * (queryPoints.size() - 1));
      linkId = slotter[pos];
      invoked = qpInvocationLogger.get(linkId);
    }
    qpInvocationLogger.put(linkId, invoked + 1);
    numberOfInvocations++;
    return queryPoints.get(linkId);
  }

  public void log(int linkId, SortedMap<Integer, RuntimeEntry> runTimes, int discoveredLinks, int totalLinks) {
    RuntimeLogEntry runtimeEntry;
    if (runtimeLogger.containsKey(linkId)) {
      runtimeEntry = runtimeLogger.get(linkId);
    } else {
      runtimeEntry = new RuntimeLogEntry(linkId, util.getDataset());
    }
    for (Entry<Integer, RuntimeEntry> entry : runTimes.entrySet()) {
      Integer duration = entry.getKey();
      runtimeEntry.addEntry(duration, entry.getValue().getRuntime());
    }
    if (durations.isEmpty()) {
      durations.addAll(runTimes.keySet());
    }
    runtimeEntry.incrementInvocation();
    if (runtimeEntry.getReachabiltyRatio() == null) {
      runtimeEntry.setReachabiltyRatio((int) ((double) discoveredLinks / (double) totalLinks * 100));
      System.out.println("Reachability ratio of link: " + linkId + ": " + runtimeEntry.getReachabiltyRatio() + "%");
    }
    runtimeLogger.put(linkId, runtimeEntry);
  }

  
  

  /**
   * <p>
   * Method main
   * </p>
   * the main method to run the confidence interval tests
   * 
   * @param args
   * @throws SQLException
   * @throws IOException
   */
  public static void main(String[] args) throws SQLException, IOException {
    BenchmarkUtil util = new BenchmarkUtil(args, '=', "BINE");
    ConfidenceIntervalExperiment launcher = new ConfidenceIntervalExperiment(util);
    System.out.println(util.printHead());
    QueryPoint queryPoint = launcher.getRandomQueryPoint();
    while (queryPoint != null) {
      RuntimeDurationTest algorithm = new RuntimeDurationTest(util.getConfig());
      algorithm.computeIsochrone(new Location[] { new Location(queryPoint.getId(),queryPoint.getStartOffset())},
          util.getMaxDuration(), util.getSpeed(), util.getTime());
      int totalRows = DBUtility.getTotalRows(util.getConfig().getConnection(),util.getConfig().getEdgeTable());
      launcher.log(queryPoint.getId(), algorithm.getLogEntries(), algorithm.getStatistic().getExploredLinks(),totalRows);
      queryPoint = launcher.getRandomQueryPoint();
    }
    String file = util.getAlgorithmName() + "_" + util.getDataset() + ".dat";
    launcher.logInFiles(util.getOutputDir(), file);
    System.out.println(util.printTail());
  }
}