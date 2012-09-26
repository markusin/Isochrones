package isochrones.launchers.experiments;

import isochrones.algorithm.test.IRuntimeDurationTestDetailed;
import isochrones.utils.AggregateUtil;
import isochrones.utils.Config;
import isochrones.utils.DBUtility;
import isochrones.utils.IOUtility;
import isochrones.utils.ReflectionUtils;
import isochrones.utils.RuntimeDurationUtil;
import isochrones.utils.entries.Detail;
import isochrones.utils.entries.RuntimeDetailedEntry;

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

public abstract class AbstractRuntimeDurationDetailedExperiment {

  private RuntimeDurationUtil util;
  private static final NumberFormat formatter = new DecimalFormat("#0.00");
  List<SortedMap<Integer, RuntimeDetailedEntry>> runTimes = new ArrayList<SortedMap<Integer, RuntimeDetailedEntry>>();

  Map<Integer, RuntimeDetailedEntry> durationEntry = new HashMap<Integer, RuntimeDetailedEntry>();
  public int totalSize;

  public AbstractRuntimeDurationDetailedExperiment(RuntimeDurationUtil util) {
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
    if (util.printHeader()) {
      System.out.println(util.printInfo());
    }

    @SuppressWarnings("rawtypes")
    Class testClass;
    @SuppressWarnings("rawtypes")
    Class[] intArgsClass = new Class[] { Config.class };
    Object[] arguments = new Object[] { util.getConfig() };

    try {
      testClass = Class.forName(className);
      @SuppressWarnings("rawtypes")
      Constructor constructor = testClass.getConstructor(intArgsClass);
      int i = 0;
      while (i < util.getFrequency()) {
        SortedMap<Integer, RuntimeDetailedEntry> runtimeLogEntries = new TreeMap<Integer, RuntimeDetailedEntry>();
        int breakPoints[] = util.isSingleBreakpoint() ? util.getDurationCheckpoints() : new int[] { util
            .getDurationCheckpoints()[util.getDurationCheckpoints().length - 1] };
        System.out.println("Iteration " + i + " with " + breakPoints.length + " duration checkpoints");
        for (int j = 0; j < breakPoints.length; j++) {
          int dMax = breakPoints[j];
          IRuntimeDurationTestDetailed algorithm = (IRuntimeDurationTestDetailed) ReflectionUtils.createObject(
              constructor, arguments);
          if (totalSize == 0) {
            if (algorithm == null) {
              System.err.println("Algorithm is null!!");
            }
            totalSize = DBUtility.getTotalRows(algorithm.getConfig().getConnection(), algorithm.getConfig().getVertexTable());
          }
          if (util.getNodesOfInterest() != null) {
            algorithm.computeIsochrone(util.getNodesOfInterest(), dMax, util.getSpeed(), util.getTime(),
                util.getDurationCheckpoints());
          } else {
            algorithm.computeIsochrone(util.getLocations(), dMax, util.getSpeed(), util.getTime(),
                util.getDurationCheckpoints());
          }
          for (int duration : algorithm.getLogEntries().keySet()) {
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

  public void log(SortedMap<Integer, RuntimeDetailedEntry> runTime) {
    runTimes.add(runTime);

    for (Integer duration : runTime.keySet()) {
      if (!durationEntry.containsKey(duration)) {
        durationEntry.put(duration, runTime.get(duration));
      }
    }
  }

  protected void logInFile(File file, boolean appendOutput) throws IOException {
    SortedMap<Integer, RuntimeDetailedEntry> medians = AggregateUtil.computeAverageDetailed(runTimes);
    StringBuilder cBuf = new StringBuilder();
    if (!appendOutput) {
      cBuf.append("#dMax").append("\t").append("t").append("\t").append("t_db").append("\t");
      cBuf.append("t_net").append("\t").append("#net").append("\t").append("\u00D8_net").append("\t");
      cBuf.append("t_r").append("\t").append("#r").append("\t").append("\u00D8_r").append("\t").append("#|E| r")
          .append("\t");
      cBuf.append("t_p").append("\t").append("#p").append("\t").append("\u00D8_p").append("\t").append("#|E| p")
          .append("\t");
      cBuf.append("t_pre").append("\t").append("#pre").append("\t").append("\u00D8_pre").append("\t");
      cBuf.append("t_loc").append("\t").append("#loc").append("\t").append("\u00D8_loc").append("\t");
      cBuf.append("t_dens").append("\t").append("#dens").append("\t").append("\u00D8_dens").append("\t");
      cBuf.append("t_sch").append("\t").append("#sch").append("\t").append("\u00D8_sch").append("\t");
      cBuf.append("t_sch_ho").append("\t").append("#sch_ho").append("\t").append("\u00D8_sch_ho").append("\t");
      cBuf.append("t_sch_he").append("\t").append("#sch_he").append("\t").append("\u00D8_sch_he").append("\t");
      cBuf.append("trMin").append("\t").append("trMax").append("\n");
    }
    for (int duration : medians.keySet()) {
      cBuf.append(duration).append("\t");
      RuntimeDetailedEntry entry = medians.get(duration);
      double runtime;
      int lookups;

      cBuf.append(formatter.format(entry.getRuntime(Detail.TOTAL))).append("\t");
      cBuf.append(formatter.format(entry.getRuntime(Detail.TOTAL_DB))).append("\t");

      runtime = entry.getRuntime(Detail.NETWORK);
      lookups = entry.getLookup(Detail.NETWORK);
      cBuf.append(formatter.format(runtime)).append("\t");
      cBuf.append(lookups).append("\t");
      cBuf.append(formatter.format(AggregateUtil.singleAverage(runtime, lookups))).append("\t");

      runtime = entry.getRuntime(Detail.RANGEQUERY);
      lookups = entry.getLookup(Detail.RANGEQUERY);
      cBuf.append(formatter.format(runtime)).append("\t");
      cBuf.append(lookups).append("\t");
      cBuf.append(formatter.format(AggregateUtil.singleAverage(runtime, lookups))).append("\t");
      cBuf.append(entry.getLoaded(Detail.RANGEQUERY)).append("\t");

      runtime = entry.getRuntime(Detail.POINTQUERY);
      lookups = entry.getLookup(Detail.POINTQUERY);
      cBuf.append(formatter.format(runtime)).append("\t");
      cBuf.append(lookups).append("\t");
      cBuf.append(formatter.format(AggregateUtil.singleAverage(runtime, lookups))).append("\t");
      cBuf.append(entry.getLoaded(Detail.POINTQUERY)).append("\t");

      runtime = entry.getRuntime(Detail.POINTQUERY_PRE);
      lookups = entry.getLookup(Detail.POINTQUERY_PRE);
      cBuf.append(formatter.format(runtime)).append("\t");
      cBuf.append(lookups).append("\t");
      cBuf.append(formatter.format(AggregateUtil.singleAverage(runtime, lookups))).append("\t");

      runtime = entry.getRuntime(Detail.LOCATIONQUERY);
      lookups = entry.getLookup(Detail.LOCATIONQUERY);
      cBuf.append(formatter.format(runtime)).append("\t");
      cBuf.append(lookups).append("\t");
      cBuf.append(formatter.format(AggregateUtil.singleAverage(runtime, lookups))).append("\t");

      runtime = entry.getRuntime(Detail.DENSITYQUERY);
      lookups = entry.getLookup(Detail.DENSITYQUERY);
      cBuf.append(formatter.format(runtime)).append("\t");
      cBuf.append(lookups).append("\t");
      cBuf.append(formatter.format(AggregateUtil.singleAverage(runtime, lookups))).append("\t");

      runtime = entry.getRuntime(Detail.SCHEDULE);
      lookups = entry.getLookup(Detail.SCHEDULE);
      cBuf.append(formatter.format(runtime)).append("\t");
      cBuf.append(lookups).append("\t");
      cBuf.append(formatter.format(AggregateUtil.singleAverage(runtime, lookups))).append("\t");

      runtime = entry.getRuntime(Detail.SCHEDULEHOMO);
      lookups = entry.getLookup(Detail.SCHEDULEHOMO);
      cBuf.append(formatter.format(runtime)).append("\t");
      cBuf.append(lookups).append("\t");
      cBuf.append(formatter.format(AggregateUtil.singleAverage(runtime, lookups))).append("\t");

      runtime = entry.getRuntime(Detail.SCHEDULEHETERO);
      lookups = entry.getLookup(Detail.SCHEDULEHETERO);
      cBuf.append(formatter.format(runtime)).append("\t");
      cBuf.append(lookups).append("\t");
      cBuf.append(formatter.format(AggregateUtil.singleAverage(runtime, lookups))).append("\t");
      cBuf.append(formatter.format(entry.getRuntime(Detail.RANGEQUERY_MIN))).append("\t");
      cBuf.append(formatter.format(entry.getRuntime(Detail.RANGEQUERY_MAX))).append("\t");
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
