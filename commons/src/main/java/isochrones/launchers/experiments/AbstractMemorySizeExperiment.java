/**
 * 
 */
package isochrones.launchers.experiments;

import isochrones.algorithm.test.IMemorySizeTest;
import isochrones.utils.Config;
import isochrones.utils.MemoryEntry;
import isochrones.utils.MemorySizeUtil;
import isochrones.utils.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractMemorySizeExperiment {

  private MemorySizeUtil util;
  NumberFormat formatter = new DecimalFormat("#0.00");

  public AbstractMemorySizeExperiment(MemorySizeUtil util) {
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
    Class[] intArgsClass = new Class[] { Config.class, int[].class };

    // try {
    try {
      testClass = Class.forName(className);
      @SuppressWarnings("rawtypes")
      Constructor constructor = testClass.getConstructor(intArgsClass);
      int checkPoints[] = (util.getDurationCheckpoints() == null) ? new int[] { util.getMaxDuration() } : util
          .getDurationCheckpoints();
      List<MemoryEntry> logEntries = new ArrayList<MemoryEntry>();
      int nodeSizeInBytes = Integer.MIN_VALUE;
      for (int i = 0; i < checkPoints.length; i++) {
        Object[] arguments;
        int dMax = checkPoints[i];
        if (checkPoints.length == 1) {
          arguments = new Object[] { util.getConfig(), util.getSizeCheckpoints() };
        } else {
          arguments = new Object[] { util.getConfig(), new int[] { util.getSizeCheckpoints()[i] } };
        }

        IMemorySizeTest algorithm = (IMemorySizeTest) ReflectionUtils.createObject(constructor, arguments);
        if (i == 0) {
          nodeSizeInBytes = algorithm.getNodeSizeInBytes();
        }
        if (util.getNodesOfInterest() != null) {
          algorithm.computeIsochrone(util.getNodesOfInterest(), dMax, util.getSpeed(), util.getTime());
        } else {
          algorithm.computeIsochrone(util.getLocations(), dMax, util.getSpeed(),util.getTime());
        }
        for (MemoryEntry entry : algorithm.getLogEntries()) {
          logEntries.add(entry);
        }
      }
      logInFile(util.getOutputFile(), logEntries, nodeSizeInBytes);
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

  protected void logInFile(File file, List<MemoryEntry> logEntries, int nodeSizeInBytes) throws IOException {
    StringBuilder bF = new StringBuilder();
    bF.append("#|V|iso \t |V|+|E| \t |V| \t |E_cont| \t |E_disc| \t Duration \n");
    // bF.append("#|V|_iso \t |V|_mm \t |E|_mm \t |V|_mm + |E|_mm \t |KB|_V iso \t |KB|_V mm \t |KB|_E mm \t |KB|_V mm + E mm \t duration \n");
    
    for (MemoryEntry logEntry : logEntries) {
      int isoNodeSize = logEntry.getIsoNodeSize();
      int traceNodeSize = logEntry.getTraceNodeSize();
      int tracePedEdgeSize = logEntry.getTraceContinuousEdgeSize();
      int traceTransportEdgeSize = logEntry.getTraceDiscreteEdgeSize();

//      double isoNodeSizeInKB = (double)isoNodeSize * nodeSizeInBytes / 1024;
//      double traceNodeSizeInKB = (double) traceNodeSize * nodeSizeInBytes / 1024;
//      double tracePedLinkSizeInKB = (double) tracePedEdgeSize * ContinuousLink.sizeOf() / 1024;
//      double traceBusLinkSizeInKB = (double) traceTransportEdgeSize * DiscreteLink.sizeOf() / 1024;

      bF.append(isoNodeSize + "\t");
      bF.append(traceNodeSize + tracePedEdgeSize + traceTransportEdgeSize + "\t");
      bF.append(traceNodeSize + "\t");
      bF.append(tracePedEdgeSize + "\t");
      bF.append(traceTransportEdgeSize + "\t");

      //bF.append(formatter.format(isoNodeSizeInKB) + "\t");
      //bF.append(formatter.format(traceNodeSizeInKB) + "\t");
      //bF.append(formatter.format(tracePedLinkSizeInKB + traceBusLinkSizeInKB) + "\t");
      //bF.append(formatter.format(traceNodeSizeInKB + tracePedLinkSizeInKB + traceBusLinkSizeInKB) + "\t");
      
      bF.append(logEntry.getDuration() + "\n");
    }
    Writer output = new BufferedWriter(new FileWriter(file));
    output.write(bF.toString());
    output.close();
    System.out.println(bF);
    System.out.println("Memory size experiment has been written into the file " + file.getAbsolutePath());
  }
}
