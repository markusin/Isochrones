package isochrones.launchers.experiments;

import isochrones.algorithm.test.IRuntimeSizeTest;
import isochrones.utils.AggregateUtil;
import isochrones.utils.Config;
import isochrones.utils.DBUtility;
import isochrones.utils.IOUtility;
import isochrones.utils.ReflectionUtils;
import isochrones.utils.RuntimeEntry;
import isochrones.utils.RuntimeMeasurement;
import isochrones.utils.RuntimeSizeUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class AbstractRuntimeSizeExperiment {

  List<SortedMap<Integer, RuntimeEntry>> runTimes = new ArrayList<SortedMap<Integer, RuntimeEntry>>();
  Map<Integer, RuntimeEntry> durationEntry = new HashMap<Integer, RuntimeEntry>();  private RuntimeSizeUtil util;
  private static final NumberFormat formatter = new DecimalFormat("#0.00");
  public int totalSize; 

  public AbstractRuntimeSizeExperiment(RuntimeSizeUtil util) {
    this.util = util;
  }

  /**
   * <p>
   * Method compute
   * </p>
   * computes the isochrones calling the class via reflection
   * 
   * @param className
   */
  @SuppressWarnings("unchecked")
  public void compute(String className) {
    if(util.printHeader()){
      System.out.println(util.printInfo());  
    }

    @SuppressWarnings("rawtypes")
    Class testClass;
    @SuppressWarnings("rawtypes")
    Class[] intArgsClass = new Class[] { Config.class, int[].class };

    try {
      testClass = Class.forName(className);
      @SuppressWarnings("rawtypes")
      Constructor constructor = testClass.getConstructor(intArgsClass);
      int durationCheckPoints[] = (util.getDurationCheckpoints() == null) ? new int[] { util.getMaxDuration() } : util
          .getDurationCheckpoints();
      int i = 0;
      while (i < util.getFrequency()) {
        // System.out.println("Iteration " + i);
        SortedMap<Integer, RuntimeEntry> logEntries = new TreeMap<Integer, RuntimeEntry>();
        for (int j = 0; j < durationCheckPoints.length; j++) {
          Object[] arguments;
          int dMax = durationCheckPoints[j];
          if (durationCheckPoints.length == 1) {
            arguments = new Object[] { util.getConfig(), util.getSizeCheckpoints() };
          } else {
            arguments = new Object[] { util.getConfig(), new int[] { util.getSizeCheckpoints()[j] } };
          }       
          IRuntimeSizeTest algorithm = (IRuntimeSizeTest) ReflectionUtils.createObject(constructor, arguments);
          if (totalSize == 0) {
            if(algorithm==null){
              System.err.println("Algorithm is null!!");
            }
            // System.out.println("Counting vertex table size");
            totalSize = DBUtility.getTotalRows(algorithm.getConfig().getConnection(), algorithm.getConfig().getVertexTable());
          }
          if (util.getNodesOfInterest() != null) {
            algorithm.computeIsochrone(util.getNodesOfInterest(), dMax, util.getSpeed(), util.getTime());
          } else {
            algorithm.computeIsochrone(util.getLocations(), dMax, util.getSpeed(),
                util.getTime());
          }
          for (int sizeChkpt : algorithm.getLogEntries().keySet()) {
            logEntries.put(sizeChkpt, algorithm.getLogEntries().get(sizeChkpt));
          }
        }
        i++;
        log(logEntries);
      }
      logInFile(util.getOutputFile(), util.appendOutput());
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  public void log(SortedMap<Integer, RuntimeEntry> runTime) {
    /*
    for (Integer sizeChkPt : runTime.keySet()) {
      System.out.println("Checkpoint: " + sizeChkPt + "\t" + runTime.get(sizeChkPt));
    }
    */
    
    runTimes.add(runTime);
    // we only logger once the reachability
    for (Integer sizeCheckPoint : runTime.keySet()) {
      if (!durationEntry.containsKey(sizeCheckPoint)) {
        durationEntry.put(sizeCheckPoint, runTime.get(sizeCheckPoint));
//        durationEntry.put(sizeCheckPoint, runTime.get(sizeCheckPoint).getDiscoveredSize());
      }
    }
  }

  protected void logInFile(File file, boolean appendOutput) throws IOException {
    
    SortedMap<Integer, RuntimeMeasurement> averages = AggregateUtil.computeAverage(runTimes);
    SortedMap<Integer, RuntimeMeasurement> medians = AggregateUtil.computeMedian(runTimes);

//    SortedMap<Integer, Double> averages = AggregateUtil.computeAverage(runTimes);
//    SortedMap<Integer, Double> medians = AggregateUtil.computeMedian(runTimes);

    // Map<Integer, Double> durationCheckPoints = getDurationCheckPoints(runTimes);
    StringBuilder cBuf = new StringBuilder();
    if(!appendOutput) {
      cBuf.append("#|V^iso|\t Time \t Reached (in %) \t Duration");
      cBuf.append("\n");
    }

    for (int size : averages.keySet()) {
      cBuf.append(size).append("\t");
      cBuf.append(formatter.format(averages.get(size).getRuntime())).append("\t");
      cBuf.append(formatter.format((double) durationEntry.get(size).getDiscoveredSize()/totalSize * 100)).append("\t");
      cBuf.append(formatter.format(durationEntry.get(size).getDurationCheckPoint()));
      cBuf.append("\n");
    }

    StringBuilder buf = new StringBuilder();
    if(appendOutput) {
      buf.append(IOUtility.readFromFile(file.toURI()));
    }
    buf.append(cBuf);
    
    Writer output = new BufferedWriter(new FileWriter(file));
    output.write(buf.toString());
    output.close();
  }

  private Map<Integer, Double> getDurationCheckPoints(List<SortedMap<Integer, RuntimeEntry>> runTimes) {
    HashMap<Integer, Double> durations = new HashMap<Integer, Double>();
    for (SortedMap<Integer, RuntimeEntry> runTime : runTimes) {
      for (Entry<Integer, RuntimeEntry> entry : runTime.entrySet()) {
        int currentSize = entry.getKey();
        double duration = runTime.get(currentSize).getDurationCheckPoint();
        durations.put(currentSize, duration);
      }
    }
    return durations;
  }

}
