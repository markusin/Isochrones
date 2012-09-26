package isochrones.utils;

import isochrones.network.Location;
import isochrones.network.QueryPoint;
import isochrones.utils.TestEntry.TestInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class BenchmarkUtil extends LauncherUtil {

  String outputDir = System.getProperty("user.dir") + System.getProperty("file.separator") + "out"
      + System.getProperty("file.separator");
  int initialDuration = 1, scale = 0, frequency = 1, range = 1;
  int totalRuns;
  int[] durationSequence;
  File speedFile, netMemoryFile, prioMemoryFile;
  Map<Integer, List<Long>> speedInfos = new HashMap<Integer, List<Long>>();
  SortedMap<Integer, Integer> netMemResults = new TreeMap<Integer, Integer>();
  SortedMap<Integer, Integer> prioMemResults = new TreeMap<Integer, Integer>();
  SortedMap<Integer, TestEntry> testEntries;
  int[] checkpoints;
  long startTime;
  private HashSet<Integer> linkIds, testedLinkIds = new HashSet<Integer>();
  boolean randomQueryPoint;
  int numberOfQueryPoints = 1;
  private String queryPointsTable;
  private boolean centralQueryPoint = true;
  private File outputFile;

  public BenchmarkUtil(String[] args, char sep, String algorithm) {
    super(args, sep, algorithm);
    int[] queryPointLinkIds = null;
    for (String arg : args) {
      String value = arg.substring(arg.indexOf(sep) + 1);
      if (arg.startsWith("outputDir")) {
        outputDir = value;
        if (!outputDir.endsWith(System.getProperty("file.separator"))) {
          outputDir.concat(System.getProperty("file.separator"));
        }
      } else if (arg.startsWith("initialDuration") && isSet(value)) {
        initialDuration = Integer.valueOf(value);
      } else if (arg.startsWith("scale") && isSet(value)) {
        scale = Integer.valueOf(value);
      } else if (arg.startsWith("frequency") && isSet(value)) {
        frequency = Integer.valueOf(value);
      } else if (arg.startsWith("range") && isSet(value)) {
        range = Integer.valueOf(value);
      } else if (arg.startsWith("randomQueryPoint") && isSet(value)) {
        randomQueryPoint = Boolean.valueOf(value);
      } else if (arg.startsWith("centralQueryPoint") && isSet(value)) {
        centralQueryPoint = Boolean.valueOf(value);
      } else if (arg.startsWith("numberOfQueryPoints") && isSet(value)) {
        numberOfQueryPoints = Integer.valueOf(value);
      } else if (arg.startsWith("queryPointsTable") && isSet(value)) {
        queryPointsTable = value;
      } else if (arg.startsWith("linkTable") && isSet(value)) {
        if (value != null && !value.isEmpty()) {
          config._setProperty("tbl.links", value);
        }
      } else if (arg.startsWith("nodeTable") && isSet(value)) {
        if (value != null && !value.isEmpty()) {
          config._setProperty("tbl.nodes", value);
        }
      } else if (arg.startsWith("distanceTable") && isSet(value)) {
        if (value != null && !value.isEmpty()) {
          config._setProperty("tbl.node_distances", value);
        }
      } else if (arg.startsWith("partitionTable") && isSet(value)) {
        if (value != null && !value.isEmpty()) {
          config._setProperty("tbl.node_partitions", value);
        }
      } else if (arg.startsWith("outputFile") && isSet(value)) {
        File dir = new File(outputDir);
        if (!dir.exists()) {
          dir.mkdir();
        }
        outputFile = new File(outputDir + value);
      } else if (arg.startsWith("checkPoints") && isSet(value)) {
        String[] entries = value.split(",");
        checkpoints = new int[entries.length];
        for (int i = 0; i < entries.length; i++) {
          checkpoints[i] = Integer.parseInt(entries[i]);
        }
      }
    }
    initDurationSequence();
    String filePraefix = outputDir + dataSet + "_" + algorithm + "_" + dateFormat.format(time.getTime()) + "_";
    if (locations != null) {
      for (int i = 0; i < locations.length; i++) {
        filePraefix += locations[i].toStringNotation() + "_";
      }
    } else {
      for (int i = 0; i < nodesOfInterest.length; i++) {
        filePraefix += nodesOfInterest[i] + "_";
      }
    }
    filePraefix += mode;
    speedFile = new File(filePraefix + "-speed.dat");
    netMemoryFile = new File(filePraefix + "-netmemory.dat");
    prioMemoryFile = new File(filePraefix + "-priomemory.dat");

    testEntries = new TreeMap<Integer, TestEntry>();

    if (queryPointLinkIds == null) {
      if (randomQueryPoint) {
        // loadLinkIds();
        initTestEntries();
      } else {
        if (locations != null) {
          this.numberOfQueryPoints = locations.length;
          initTestEntries(locations);
        } else {
          this.numberOfQueryPoints = nodesOfInterest.length;
        }
      }
    }
    /*
     * else { this.numberOfQueryPoints=queryPointLinkIds.length; this.randomQueryPoint=false;
     * initTestEntries(queryPointLinkIds); }
     */
  }

  /**
   * <p>
   * Method getInitialDuration
   * </p>
   * 
   * @return the initial value of the duration
   */
  public int getInitialDuration() {
    return initialDuration;
  }

  /**
   * <p>
   * Method initDurationSequence
   * </p>
   * initializes the duration sequencduratione
   * 
   * @return
   */
  public void initDurationSequence() {
    durationSequence = new int[range];
    durationSequence[0] = initialDuration;
    for (int i = 1; i < durationSequence.length; i++) {
      if (initialDuration == 1) {
        durationSequence[i] = i * scale;
      } else {
        durationSequence[i] = initialDuration + i * scale;
      }
    }
  }

  public void initTestEntries() {
    TestEntry firstEntry = null;
    for (int i = 0; i < durationSequence.length; i++) {
      TestEntry testEntry = new TestEntry(durationSequence[i], frequency, numberOfQueryPoints);
      if (i == 0) {
        for (int j = 0; j < numberOfQueryPoints; j++) {
          testEntry.add(getRandomQueryPointLinkId());
        }
        firstEntry = testEntry;
      } else {
        for (Integer linkId : firstEntry.getTestedLinkIds()) {
          testEntry.add(linkId);
        }
      }
      testEntries.put(durationSequence[i], testEntry);
    }
  }

  public void initTestEntries(Location[] locations) {
    Arrays.sort(locations, new Comparator<Location>() {
      @Override
      public int compare(Location o1, Location o2) {
        if(o1.getLinkId()<o2.getLinkId()) return -1;
        if(o1.getLinkId()>o2.getLinkId()) return 1;
        return 0;
      }
    });
    for (int i = 0; i < durationSequence.length; i++) {
      TestEntry testEntry = new TestEntry(durationSequence[i], frequency, numberOfQueryPoints);
      for (Location location : locations) {
        testEntry.add(location.getLinkId());
      }
      testEntries.put(durationSequence[i], testEntry);
    }
  }

  /**
   * <p>
   * Method getIncrementFactor
   * </p>
   * 
   * @return the factor how much the duration is incremented
   */
  public int getIncrementFactor() {
    return scale;
  }

  /**
   * <p>
   * Method getFrequency
   * </p>
   * 
   * @return the frequency of the increment
   */
  public int getFrequency() {
    return frequency;
  }

  public String getOutputDir() {
    return outputDir;
  }

  /**
   * <p>
   * Method getOutputFile
   * </p>
   * 
   * @return
   */
  public File getOutputFile() {
    return outputFile;
  }

  public String getQueryPointsTable() {
    return queryPointsTable;
  }

  public int[] getCheckpoints() {
    return checkpoints;
  }

  /**
   * <p>
   * Method getRandomTestEntry
   * </p>
   * 
   * @return
   */
  public TestEntry getRandomTestEntry() {
    int probe = (int) (Math.random() * (range)); // Random goes from 0 to <1 -> range + 1
    int interval = asInterval(probe);
    TestEntry testEntry = testEntries.get(interval);
    int cycleCounter = 0;
    while (testEntry.isFullTested()) {
      probe = (probe + 1) % range;
      int newInterval = asInterval(probe);
      if (cycleCounter > testEntries.size()) { // all processed
        return null;
      }
      testEntry = testEntries.get(newInterval);
      cycleCounter++;
    }
    return testEntry;
  }

  /*
   * private int asProbe(int duration) { if (duration == initialDuration) { return 0; } else { if (initialDuration == 1)
   * { return duration / scale; } else { return (duration - initialDuration) / scale; } } }
   */

  private int asInterval(int probe) {
    if (probe == 0) {
      return initialDuration;
    } else {
      if (initialDuration == 1) {
        return probe * scale;
      } else {
        return initialDuration + (probe) * scale;
      }
    }
  }

  /**
   * <p>
   * Method loadQueryPoints
   * </p>
   * 
   * @param tableName
   * @return
   */
  public Map<Integer, QueryPoint> loadQueryPoints(Connection connection) {
    Map<Integer, QueryPoint> qPoints = DBUtility.loadQueryPoints(connection, queryPointsTable);
    Location[] locations = new Location[qPoints.size()];
    int i = 0;
    for (QueryPoint qPoint : qPoints.values()) {
      new Location(qPoint.getId(),qPoint.getStartOffset());
    }
    numberOfQueryPoints = locations.length;
    return qPoints;
  }

  public TestEntry getTestEntry(int duration) {
    return testEntries.get(duration);
  }

  /**
   * <p>
   * Method getRandomQueryPointLinkId
   * </p>
   * 
   * @return
   */
  public int getRandomQueryPointLinkId() {
    int linkId;
    if (testedLinkIds.size() == numberOfQueryPoints) {
      linkId = (int) Math.round(Math.random() * (testedLinkIds.size() - 1));
      Integer[] links = testedLinkIds.toArray(new Integer[1]);
      linkId = links[linkId];
    } else {
      linkId = (int) Math.round(Math.random() * linkIds.size());
      while (!linkIds.contains(linkId) || testedLinkIds.contains(linkId)) {
        linkId = (linkId + 1) % linkIds.size();
      }
      testedLinkIds.add(linkId);
    }
    return linkId;
  }

  /**
   * <p>
   * Method isRandomQueryPoint
   * </p>
   * 
   * @return
   */
  public boolean isRandomQueryPointChosen() {
    return randomQueryPoint;
  }

  public boolean isCentralQueryPoint() {
    return centralQueryPoint;
  }

  /**
   * <p>
   * Method startTest
   * </p>
   * start the test and safes the time
   */
  public void startTest() {
    startTime = System.currentTimeMillis();
  }

  /**
   * <p>
   * Method endTest
   * </p>
   * ends the test and stores the runtime of the test in the corresponding key
   * 
   * @param duration the duration of the launched test
   * @return true if the test with the specified duration has been launched for the first time
   */
  public boolean endTest(int linkId, TestEntry entry) {
    long time = System.currentTimeMillis() - startTime;
    System.out.println("Duration: " + entry.getDuration() + "\t linkOI: " + linkId + "\t  time: " + time);
    /*
     * try { DBQuery.runDummyQuery(); DBConnector.getConnection().close(); } catch (SQLException e) {
     * e.printStackTrace(); }
     */
    return entry.addTime(linkId, time);
    /*
     * if (speedInfos.get(duration) == null) { speedInfos.put(duration, new ArrayList<Long>()); } else {
     * speedInfos.get(duration).add(time); } int probe = 0; if (duration == initialDuration) { probe = 0; return
     * testEntries[0] == 1; } else { if (initialDuration == 1) { probe = duration / scale; } else { probe = (duration -
     * initialDuration) / scale; } return testEntries[probe] == 1; }
     */
  }

  /**
   * <p>
   * Method endTest
   * </p>
   * 
   * @param duration
   * @param averageTime
   * @return public boolean endTest(int duration, float averageTime) { long time = System.currentTimeMillis() -
   *         startTime; System.out.println(duration + "\t" + time + "\t" + averageTime); try { DBQuery.runDummyQuery();
   *         DBConnector.getConnection().close(); } catch (SQLException e) { e.printStackTrace(); } if
   *         (speedInfos.get(duration) == null) { speedInfos.put(duration, new ArrayList<Long>()); } else {
   *         speedInfos.get(duration).add(time); } int probe = 0; if (duration == initialDuration) { probe = 0; return
   *         testEntries[0] == 1; } else { if (initialDuration == 1) { probe = duration / scale; } else { probe =
   *         (duration - initialDuration) / scale; } return testEntries[probe] == 1; } }
   */
  /**
   * <p>
   * Method saveStatistics
   * </p>
   * 
   * @param duration
   * @param numberOfPrioElements
   * @param numberOfElements
   */
  public void saveStatistics(int duration, int numberOfPrioElements, int numberOfElements) {
    prioMemResults.put(duration, numberOfPrioElements);
    netMemResults.put(duration, numberOfElements);
  }

  public void log() throws IOException {
    FileWriter fWriter = new FileWriter(speedFile);
    BufferedWriter out = new BufferedWriter(fWriter);
    StringBuffer runtimeBuf = new StringBuffer();
    StringBuffer memoryBuf = new StringBuffer();

    runtimeBuf.append("#duration").append("\t");
    memoryBuf.append("#duration").append("\t");
    TestEntry first = testEntries.get(testEntries.firstKey());
    for (Integer linkId : first.getTestedLinkIds()) {
      runtimeBuf.append("\t").append(linkId);
      memoryBuf.append("\t").append(linkId);
    }
    runtimeBuf.append("\n");
    memoryBuf.append("\n");

    for (TestEntry testEntry : testEntries.values()) {
      runtimeBuf.append(testEntry.getDuration());
      memoryBuf.append(testEntry.getDuration());
      for (TestInfo linkTestInfo : testEntry.getLinkTestInfos()) {
        runtimeBuf.append("\t").append(linkTestInfo.getAverageRuntime());
        memoryBuf.append("\t").append(linkTestInfo.getElementsInMM());
      }
      runtimeBuf.append("\n");
      memoryBuf.append("\n");
    }

    out.write(runtimeBuf.toString());
    out.close();
    fWriter.close();

    // second write memory values in file
    fWriter = new FileWriter(netMemoryFile);
    out = new BufferedWriter(fWriter);
    out.write(memoryBuf.toString());
    out.close();
    fWriter.close();

  }

  public String getTimeAsString() {
    return dateFormat.format(time.getTime());
  }

  public void logInFiles() throws IOException {

    // first write speed values in file
    FileWriter fWriter = new FileWriter(speedFile);
    BufferedWriter out = new BufferedWriter(fWriter);
    SortedMap<Integer, Double> speedResults = new TreeMap<Integer, Double>();
    // computing the average
    for (Entry<Integer, List<Long>> speedInfo : speedInfos.entrySet()) {
      int elements = 0;
      long totalTime = 0L;
      for (Long value : speedInfo.getValue()) {
        totalTime += value;
        elements++;
      }
      double avg = totalTime / elements;
      speedResults.put(speedInfo.getKey(), avg);
    }

    StringBuffer buf = new StringBuffer();
    for (Iterator<Integer> iterator = speedResults.keySet().iterator(); iterator.hasNext();) {
      Integer interval = iterator.next();
      buf.append(interval).append("\t").append(speedResults.get(interval)).append("\n");
    }
    out.write(buf.toString());
    out.close();
    fWriter.close();

    // second write memory values in file
    fWriter = new FileWriter(netMemoryFile);
    out = new BufferedWriter(fWriter);
    buf.delete(0, buf.length());
    for (Iterator<Integer> iterator = netMemResults.keySet().iterator(); iterator.hasNext();) {
      Integer interval = iterator.next();
      buf.append(interval).append("\t").append(netMemResults.get(interval)).append("\n");
    }
    out.write(buf.toString());
    out.close();
    fWriter.close();

    // third write priomemory values in file
    fWriter = new FileWriter(prioMemoryFile);
    out = new BufferedWriter(fWriter);
    buf.delete(0, buf.length());
    for (Iterator<Integer> iterator = prioMemResults.keySet().iterator(); iterator.hasNext();) {
      Integer interval = iterator.next();
      buf.append(interval).append("\t").append(prioMemResults.get(interval)).append("\n");
    }
    out.write(buf.toString());
    out.close();
    fWriter.close();
  }

  /**
   * <p>
   * Method printHead
   * </p>
   * 
   * @return
   */
  public String printHead() {
    StringBuilder b = new StringBuilder();
    b.append(super.toString());
    b.append("#### Benchmark Parameters #########\n");
    b.append("Initial duration:").append(initialDuration).append("\n");
    b.append("Scale:").append(scale).append("\n");
    b.append("Range:").append(range).append("\n");
    b.append("Test frequency:").append(getFrequency()).append("\n");
    b.append("Number of query points:").append(numberOfQueryPoints).append("\n");
    int totalQueries = numberOfQueryPoints * frequency;
    b.append("Total number of tested queries:").append(totalQueries).append("\n");
    b.append("Output directory:").append(outputDir).append("\n");
    // b.append("Estimated time in seconds ():").append(totalQueries).append("\n");
    b.append("#### General settings #########\n");
    b.append("Write isochrones in DB: ").append(outputWriting).append("\n");
    b.append("\n#### Start of benchmark tests #####");
    return b.toString();
  }

  /**
   * <p>
   * Method printTail
   * </p>
   * 
   * @return
   */
  public String printTail() {
    StringBuilder b = new StringBuilder();
    b.append("##### End  of benchmark tests #####\n");
    b.append("###################################\n");
    return b.toString();
  }

  /*
   * private int getProbe(int duration) { int probe = 0; if (duration == initialDuration) { return 0; } else { if
   * (initialDuration == 1) { return duration / scale; } else { return (duration - initialDuration) / scale; } } }
   */

}
