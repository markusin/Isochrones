package isochrones.mrnex.launchers.experiments;

import isochrones.mrnex.algorithm.test.IMonitorNetworkLoadingTest;
import isochrones.mrnex.utils.MonitorNetworkLoadingUtil;
import isochrones.mrnex.utils.NetworkMonitorEntry;
import isochrones.utils.Config;
import isochrones.utils.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMonitorNetworkLoadingExperiment {

  private MonitorNetworkLoadingUtil util;

  public AbstractMonitorNetworkLoadingExperiment(MonitorNetworkLoadingUtil util) {
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
    System.out.println(util.printInfo());
    // the first query we do not count

    @SuppressWarnings("rawtypes")
    Class testClass;
    @SuppressWarnings("rawtypes")
    Class[] intArgsClass = new Class[] { Config.class };
    Object[] arguments = new Object[] { util.getConfig() };

    try {
      testClass = Class.forName(className);
      @SuppressWarnings("rawtypes")
      Constructor constructor = testClass.getConstructor(intArgsClass);
      List<NetworkMonitorEntry> logEntries = new ArrayList<NetworkMonitorEntry>();
      int breakPoints[] = util.isSingleBreakpoint() ? util.getDurationCheckpoints() : new int[] { util
          .getDurationCheckpoints()[util.getDurationCheckpoints().length - 1] + 1 };
      for (int j = 0; j < breakPoints.length; j++) {
        int dMax = breakPoints[j];
        IMonitorNetworkLoadingTest algorithm = (IMonitorNetworkLoadingTest) ReflectionUtils.createObject(constructor, arguments);
        if (util.getNodesOfInterest() != null) {
          algorithm.computeIsochrone(util.getNodesOfInterest(), dMax, util.getSpeed(), util.getTime());
        } else {
          algorithm.computeIsochrone(util.getLocations(), dMax, util.getSpeed(), util.getTime());
        }
        for(NetworkMonitorEntry entry: algorithm.getLogEntries()) {
          logEntries.add(entry);
        }
      }
      logInFile(util.getOutputFile(),logEntries);
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
  
  public void logInFile(File file, List<NetworkMonitorEntry> logEntries) throws IOException {
    StringBuilder bF = new StringBuilder();
    bF.append("Duration\t |E|^explored \t  \t |E|^IER \t |E|^INE \t |E|^loaded \n");
      for (NetworkMonitorEntry logEntry : logEntries) {
        bF.append(logEntry.getDuration() + "\t");
        bF.append(logEntry.getExploredEdges() + "\t");
        bF.append(logEntry.getLoadedWithIER() + "\t");
        bF.append(logEntry.getLoadedWithINE() + "\t");
        bF.append(logEntry.getTotalLoaded() + "\n");
      }
    Writer output = new BufferedWriter(new FileWriter(file));
    output.write(bF.toString());
    output.close();
    System.out.println(bF);
    System.out.println("Network density experiment has been written into the file " + file.getAbsolutePath());
  }

}
