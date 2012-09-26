package isochrones.minex.utils;

import isochrones.minex.algorithm.test.RuntimeDurationTest;
import isochrones.utils.DBUtility;
import isochrones.utils.RuntimeEntry;
import isochrones.utils.RuntimeIntervalUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
 * 
*
* <p>The <code>ReachabilityExperiment</code> class</p> does a reachability experiment using different 
* arrival time intervals with the same query point. After some specific time points (e.g. every 10 minutes)
* there are measured the percentage of the entire network to be reached. 
* 
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p> </p>
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
 */
public class RuntimeIntervalExperiment {
  
  public static DateFormat outDateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'-'HH':'mm");
  
  StringBuffer buf = new StringBuffer();
  
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


  public static void main(String[] args) throws SQLException {
    RuntimeIntervalUtil util = new RuntimeIntervalUtil(args, '=',"BINE");
    System.out.println(util);
    String fileName = util.getAlgorithmName() + "_"  + util.getDataset() + "_runtime_interval";
    
    RuntimeIntervalExperiment launcher = new RuntimeIntervalExperiment();
    
    int networkSize = 0;
    int frequency = util.getFrequency();
    
    Calendar targetTime = util.getStartTargetTime();
    do {
      System.out.println("Starting experiment at time: " + outDateFormat.format(targetTime.getTime()));
      int i = 0, totalSize= 0;
      List<SortedMap<Integer, RuntimeEntry>> runTimes = new ArrayList<SortedMap<Integer, RuntimeEntry>>();
      Map<Integer,Double> reachRatios = new HashMap<Integer,Double>();
      
      while (i < frequency) {
        RuntimeDurationTest algorithm = new RuntimeDurationTest(util.getConfig());
        if(totalSize==0){
          totalSize = DBUtility.getTotalVertexSize(util.getConfig().getConnection(),util.getConfig().getVertexTable(),true);
        }
        algorithm.computeIsochrone(util.getLocations(), util.getMaxDuration(),
            util.getSpeed(), targetTime);
        
        if(networkSize==0) {
          networkSize = DBUtility.getNetworkSize(util.getConfig().getConnection(),util.getConfig().getEdgeTable(),util.getMode());
        }
        
        runTimes.add(algorithm.getLogEntries());
        for (Integer duration : algorithm.getLogEntries().keySet()) {
          if(!reachRatios.containsKey(duration)){
            reachRatios.put(duration, (double)algorithm.getLogEntries().get(duration).getDiscoveredSize()/totalSize);
          }
        }
        i++;
      }
      try {
        launcher.logInBuffer(runTimes, reachRatios, targetTime);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(targetTime.getTimeInMillis());
      cal.add(Calendar.MINUTE, +1);
      targetTime = cal;
    } while(targetTime.before(util.getEndTargetTime()));
    launcher.logInFile(fileName);
    /** End computation of the algorithm **/
  }
  
  
  
  
  private void logInBuffer(List<SortedMap<Integer, RuntimeEntry>> runTimes, Map<Integer, Double> reachRatios,
                          Calendar targetTime)  throws IOException {
    SortedMap<Integer,Double> averages = computeAverage(runTimes);
    SortedMap<Integer,Double> medians = computeMedian(runTimes);
    
    for (int duration : averages.keySet()) {
      buf.append(outDateFormat.format(targetTime.getTime())).append("\t");
      buf.append(duration).append("\t");
      buf.append(averages.get(duration)).append("\t");
      buf.append(medians.get(duration)).append("\t");
      buf.append(Math.round(reachRatios.get(duration)*100)).append("\n");
    }
  }
  
  public void logInFile(String fileName) {
    Writer output;
    File dir = new File("out");
    dir.mkdir();
    File file = new File(dir.getPath() + System.getProperty("file.separator") + fileName + ".dat");
    try {
      output = new BufferedWriter(new FileWriter(file));
      output.write(buf.toString());
      output.close();
      System.out.println("Your file has been written into " + file.getAbsolutePath());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public SortedMap<Integer, Double> computeMedian(List<SortedMap<Integer, RuntimeEntry>> runTimes) {
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

  public SortedMap<Integer, Double> computeAverage(List<SortedMap<Integer, RuntimeEntry>> runTimes) {
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
  
}
