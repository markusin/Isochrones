/**
 * 
 */
package isochrones.launchers.experiments;

import isochrones.algorithm.Isochrone;
import isochrones.utils.Config;
import isochrones.utils.DurationUtil;
import isochrones.utils.ReflectionUtils;
import isochrones.utils.TupleEntry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>
 * The <code>AbstractMemoryCountLoadedTuplesExperiment</code> class counts the number of loaded tuples. Tuples might be
 * pedestrian and bus edges
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
public abstract class AbstractMemoryDurationCountLoadedTuplesExperiment {

  private DurationUtil util;

  // NumberFormat formatter = new DecimalFormat("#0.00");

  public AbstractMemoryDurationCountLoadedTuplesExperiment(DurationUtil util) {
    this.util = util;
  }

  @SuppressWarnings("unchecked")
  public void compute(String className) {
    System.out.println(util.printInfo());
    // the first query we do not count

    @SuppressWarnings("rawtypes")
    Class testClass;
    @SuppressWarnings("rawtypes")
    Class[] intArgsClass = new Class[] { Config.class };

    // try {
    try {
      testClass = Class.forName(className);
      @SuppressWarnings("rawtypes")
      Constructor constructor = testClass.getConstructor(intArgsClass);
      int checkPoints[] = (util.getDurationCheckpoints() == null) ? new int[] { util.getMaxDuration() } : util
          .getDurationCheckpoints();
      TreeMap<Integer, TupleEntry> logEntries = new TreeMap<Integer, TupleEntry>();
      Object[] arguments = new Object[] { util.getConfig() };
      for (int i = 0; i < checkPoints.length; i++) {
        int dMax = checkPoints[i];
        Isochrone algorithm = (Isochrone) ReflectionUtils.createObject(constructor, arguments);
        if (util.getNodesOfInterest() != null) {
          algorithm.computeIsochrone(util.getNodesOfInterest(), dMax, util.getSpeed(), util.getTime());
        } else {
          algorithm.computeIsochrone(util.getLocations(), dMax, util.getSpeed(),
              util.getTime());
        }
        TupleEntry entry = new TupleEntry(dMax, algorithm.getStatistic().getLoadedContinuousEdges(), algorithm.getStatistic().getLoadedDiscreteEdges());
        logEntries.put(dMax, entry);
      }
      logInFile(logEntries);
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

  protected void logInFile(Map<Integer, TupleEntry> logEntries) throws IOException {
    StringBuilder bf = new StringBuilder();
    bf.append("#Duration \t |Fetched Tuples| \t |Fetched Ped Tuples| \t |Fetched Transport Tuples|\n");
    for (TupleEntry logEntry : logEntries.values()) {
      bf.append(logEntry.getDuration() + "\t");
      bf.append(logEntry.getLoadedTuples() + "\t");
      bf.append(logEntry.getLoadedTuplesPedLinks() + "\t");
      bf.append(logEntry.getLoadedTuplesTransportLinks() + "\n");
    }

    File file = util.getOutputFile();
    file.createNewFile();
    Writer output = new BufferedWriter(new FileWriter(file));
    output.write(bf.toString());
    output.close();
    System.out.println(bf);
    System.out
        .println("Memory-Count-Loaded tupels experiment has been written into the file " + file.getAbsolutePath());
  }
}
