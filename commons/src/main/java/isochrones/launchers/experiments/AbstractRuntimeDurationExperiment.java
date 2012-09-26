package isochrones.launchers.experiments;

import isochrones.algorithm.test.IRuntimeDurationTest;
import isochrones.utils.AggregateUtil;
import isochrones.utils.Config;
import isochrones.utils.DBUtility;
import isochrones.utils.IOUtility;
import isochrones.utils.ReflectionUtils;
import isochrones.utils.RuntimeDurationUtil;
import isochrones.utils.RuntimeEntry;
import isochrones.utils.RuntimeMeasurement;

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
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class AbstractRuntimeDurationExperiment {

  private RuntimeDurationUtil util;
  private static final NumberFormat formatter = new DecimalFormat("#0.00");
  List<SortedMap<Integer, RuntimeEntry>> runTimes = new ArrayList<SortedMap<Integer, RuntimeEntry>>();
  
  Map<Integer, RuntimeEntry> durationEntry = new HashMap<Integer, RuntimeEntry>();
  public int totalSize; 
  

  public AbstractRuntimeDurationExperiment(RuntimeDurationUtil util) {
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
    Class[] intArgsClass = new Class[] { Config.class};
    Object[] arguments = new Object[] { util.getConfig()};

    try {
      testClass = Class.forName(className);
      @SuppressWarnings("rawtypes")
      Constructor constructor = testClass.getConstructor(intArgsClass);
      int i = 0;
      while (i < util.getFrequency()) {
        SortedMap<Integer, RuntimeEntry> runtimeLogEntries = new TreeMap<Integer, RuntimeEntry>();
        int breakPoints[] = util.isSingleBreakpoint() ? util.getDurationCheckpoints() : new int[] { util.getDurationCheckpoints()[util.getDurationCheckpoints().length-1]};
        // System.out.println("Iteration " + i + " with " + breakPoints.length  + " duration checkpoints");
        for (int j = 0; j < breakPoints.length; j++) {
          int dMax = breakPoints[j];
          IRuntimeDurationTest algorithm = (IRuntimeDurationTest) ReflectionUtils.createObject(constructor, arguments);
          if (totalSize == 0) {
            if(algorithm==null){
              System.err.println("Algorithm is null!!");
            }
            // System.out.println("Counting vertex table size");
            totalSize = DBUtility.getTotalRows(algorithm.getConfig().getConnection(), algorithm.getConfig().getVertexTable());
          }
          if (util.getNodesOfInterest() != null) {
            if(util.isSingleBreakpoint()) {
              algorithm.computeIsochrone(util.getNodesOfInterest(),dMax,util.getSpeed(),util.getTime(), new int[] {breakPoints[j]});
            } else {
              algorithm.computeIsochrone(util.getNodesOfInterest(),dMax,util.getSpeed(),util.getTime(),util.getDurationCheckpoints());
            }
          } else {
            algorithm.computeIsochrone(util.getLocations(),dMax,util.getSpeed(), util.getTime(),util.getDurationCheckpoints());
          }
          for(int duration: algorithm.getLogEntries().keySet()) {
            runtimeLogEntries.put(duration, algorithm.getLogEntries().get(duration));
          }
        }
        i++;
        log(runtimeLogEntries);
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
    runTimes.add(runTime);
    
    for (Integer duration : runTime.keySet()) {
      if (!durationEntry.containsKey(duration)) {
        durationEntry.put(duration, runTime.get(duration));
      }
    }
  }

  protected void logInFile(File file, boolean appendOutput) throws IOException {
    SortedMap<Integer, RuntimeMeasurement> averages = AggregateUtil.computeAverage(runTimes);
    StringBuilder cBuf = new StringBuilder();
    if(!appendOutput) {
      cBuf.append("#Duration\t Time \t |V|^iso \t Reached (in %)");
      cBuf.append("\n");
    } 
    for (int duration : averages.keySet()) {
      cBuf.append(duration).append("\t");
      cBuf.append(formatter.format(averages.get(duration).getRuntime())).append("\t");
      cBuf.append(durationEntry.get(duration).getDiscoveredSize()).append("\t");
      cBuf.append(formatter.format((double) durationEntry.get(duration).getDiscoveredSize()/totalSize * 100));
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

}
