package isochrones.minex.utils;

import isochrones.minex.algorithm.test.RuntimePercentageTest;
import isochrones.utils.BenchmarkUtil;
import isochrones.utils.DBUtility;
import isochrones.utils.RuntimeEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Markus Innerebner
 */
public class RuntimeExperimentPercentage {

  private BenchmarkUtil util;

  List<SortedMap<Integer, RuntimeEntry>> runTimes = new ArrayList<SortedMap<Integer, RuntimeEntry>>();
  Map<Integer,Double> reachRatios = new HashMap<Integer,Double>();

  private Comparator<Long> comparator = new Comparator<Long>() {
    @Override
    public int compare(Long o1, Long o2) {
      final int BEFORE = -1;
      final int EQUAL = 0;
      final int AFTER = 1;

      if (o1 < o2) {
        return BEFORE;
      }
      if (o1 > o2) {
        return AFTER;
      }
      return EQUAL;
    }
  };

  public RuntimeExperimentPercentage(BenchmarkUtil util) {
    this.util = util;
  }

  public void log(SortedMap<Integer, RuntimeEntry> runTime, int totalSize) {
    runTimes.add(runTime);
    for (Integer duration : runTime.keySet()) {
      if(!reachRatios.containsKey(duration)){
        reachRatios.put(duration, (double)runTime.get(duration).getDiscoveredSize()/totalSize);
      }
    }
  }

  public SortedMap<Integer, Double> computeMedian() {
    SortedMap<Integer, Double> output = new TreeMap<Integer, Double>();
    SortedMap<Integer, List<Long>> runtimesClustered = new TreeMap<Integer, List<Long>>();

    for (SortedMap<Integer, RuntimeEntry> runTime : runTimes) {
      for (Entry<Integer, RuntimeEntry> entry : runTime.entrySet()) {
        int currentDur = entry.getKey();
        if (!runtimesClustered.containsKey(currentDur)) {
          runtimesClustered.put(entry.getKey(), new ArrayList<Long>());
        }
        runtimesClustered.get(currentDur).add(entry.getValue().getRuntime());
      }
    }

    for (Integer dMax : runtimesClustered.keySet()) {
      List<Long> values = runtimesClustered.get(dMax);
      Collections.sort(values, this.comparator);
      output.put(dMax, median(values.toArray(new Long[values.size()])));
    }
    return output;
  }

  public SortedMap<Integer, Double> computeAverage() {
    SortedMap<Integer, Double> output = new TreeMap<Integer, Double>();
    SortedMap<Integer, List<Long>> runtimesClustered = new TreeMap<Integer, List<Long>>();

    for (SortedMap<Integer, RuntimeEntry> runTime : runTimes) {
      for (Entry<Integer, RuntimeEntry> entry : runTime.entrySet()) {
        int currentDur = entry.getKey();
        if (!runtimesClustered.containsKey(currentDur)) {
          runtimesClustered.put(entry.getKey(), new ArrayList<Long>());
        }
        runtimesClustered.get(currentDur).add(entry.getValue().getRuntime());
      }
    }

    for (Integer dMax : runtimesClustered.keySet()) {
      List<Long> values = runtimesClustered.get(dMax);
      Collections.sort(values, this.comparator);
      output.put(dMax, average(values));
    }
    return output;
  }

  private double median(Long[] values) {
    int middle = values.length / 2; // subscript of middle element
    if (values.length % 2 == 1) {
      // Odd number of elements -- return the middle one.
      return values[middle];
    } else {
      // Even number -- return average of middle two
      // Must cast the numbers to double before dividing.
      return (values[middle - 1] + values[middle]) / 2.0;
    }
  }

  private double average(Collection<Long> values) {
    double avg = 0;
    for (Long val : values) {
      avg += val;
    }
    avg = avg / values.size();
    return avg;
  }
  
  private void logInFiles(File file) throws IOException {
    SortedMap<Integer,Double> averages = computeAverage();
    SortedMap<Integer,Double> medians = computeMedian();
    
    StringBuilder bF = new StringBuilder();

    bF.append("Percentage\t Average \t Median\t Reached (in %)\t ### ");
    bF.append("Querypoint: ");
    for (int i = 0; i < util.getLocations().length; i++) {
      bF.append(util.getLocations()[i].toStringNotation()).append(",");
    }
    bF.append("###\n");
    
    for (int duration : averages.keySet()) {
      bF.append(duration).append("\t");
      bF.append(averages.get(duration)).append("\t");
      bF.append(medians.get(duration)).append("\t");
      bF.append(Math.round(reachRatios.get(duration)*100)).append("\n");
    }
    
    Writer output = new BufferedWriter(new FileWriter(file));
    output.write(bF.toString());
    output.close();
    System.out.println("Your file has been written into " + file.getAbsolutePath());
  }
  
  public static void main(String[] args) throws SQLException, IOException {

    BenchmarkUtil util = new BenchmarkUtil(args, '=', "BINE");
    RuntimeExperimentPercentage launcher = new RuntimeExperimentPercentage(util);
    System.out.println(util.printHead());
    // the first query we do not count
    int frequency = util.getFrequency();
    int i = 0, totalSize= 0;
    while (i < frequency) {
      RuntimePercentageTest algorithm = new RuntimePercentageTest(util.getConfig());
      if(totalSize==0){
        totalSize = DBUtility.getTotalVertexSize(util.getConfig().getConnection(),util.getConfig().getVertexTable(),true); 
      }
      algorithm.setTotalSize(totalSize);
      algorithm.computeIsochrone(util.getLocations(), util.getMaxDuration(),
          util.getSpeed(), util.getTime());
      launcher.log(algorithm.getRunTimes(),totalSize);
      i++;
    }
    launcher.logInFiles(util.getOutputFile());
  }

}
