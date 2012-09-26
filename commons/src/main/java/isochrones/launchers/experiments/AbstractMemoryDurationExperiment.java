/**
 * 
 */
package isochrones.launchers.experiments;

import isochrones.algorithm.test.IMemoryDurationTest;
import isochrones.utils.Config;
import isochrones.utils.DurationEntry;
import isochrones.utils.MemoryDurationUtil;
import isochrones.utils.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * The <code>AbstractMemoryDurationExperiment</code> class
 * </p>
 * <p>
 * Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a>
 * </p>
 * <p>
 * Domenikanerplatz - Bozen, Italy.
 * </p>
 * <p>
 * </p>
 * 
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public abstract class AbstractMemoryDurationExperiment {

  private MemoryDurationUtil util;

  public AbstractMemoryDurationExperiment(MemoryDurationUtil util) {
    this.util = util;
  }

  @SuppressWarnings("unchecked")
  public void compute(String className) {
    System.out.println(util.printInfo());
    @SuppressWarnings("rawtypes")
    Class testClass;
    @SuppressWarnings("rawtypes")
    Class[] intArgsClass = new Class[] { Config.class };
    Object[] arguments = new Object[] { util.getConfig() };
    try {
      testClass = Class.forName(className);
      @SuppressWarnings("rawtypes")
      Constructor constructor = testClass.getConstructor(intArgsClass);
      int breakPoints[] = util.isSingleBreakpoint() ? util.getDurationCheckpoints()
          : new int[] { util.getMaxDuration() };
      List<DurationEntry> logEntries = new ArrayList<DurationEntry>();
      for (int j = 0; j < breakPoints.length; j++) {
        int dMax = breakPoints[j];
        IMemoryDurationTest algorithm = (IMemoryDurationTest) ReflectionUtils.createObject(constructor, arguments);
        if (util.getNodesOfInterest() != null) {
          algorithm.computeIsochrone(util.getNodesOfInterest(), dMax, util.getSpeed(), util.getTime(),
              util.isSingleBreakpoint() ? new int[] { breakPoints[j] } : util.getDurationCheckpoints());
        } else {
          algorithm.computeIsochrone(util.getLocations(), dMax, util.getSpeed(), util.getTime(),
              util.isSingleBreakpoint() ? new int[] { breakPoints[j] } : util.getDurationCheckpoints());
        }
        for (DurationEntry entry : algorithm.getLogEntries()) {
          logEntries.add(entry);
        }
      }
      logInFile(util.getOutputFile(), logEntries);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * <p>
   * Method logInFile
   * </p>
   * writes the log entries into the file
   * 
   * @param logEntries
   */
  public void logInFile(File file, List<DurationEntry> logEntries) throws IOException {
    StringBuilder bF = new StringBuilder();
    bF.append("#Duration \t |V|+|E| \t |V| \t |E_cont| \t |E_disc|\n");
    for (DurationEntry logEntry : logEntries) {
      bF.append(logEntry.getDuration() + "\t");
      int totalSize = logEntry.getNumberOfNodes() + logEntry.getNumberOfPedLinks() + logEntry.getNumberOfBusLinks();
      bF.append(totalSize + "\t");
      bF.append(logEntry.getNumberOfNodes() + "\t");
      bF.append(logEntry.getNumberOfPedLinks() + "\t");
      bF.append(logEntry.getNumberOfBusLinks() + "\n");
    }
    Writer output = new BufferedWriter(new FileWriter(file));
    output.write(bF.toString());
    output.close();
    System.out.println(bF);
    System.out.println("Memory duration experiment has been written into the file " + file.getAbsolutePath());
  }
}
