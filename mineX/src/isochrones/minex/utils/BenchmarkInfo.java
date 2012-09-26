/**
 * 
 */
package isochrones.minex.utils;

import isochrones.minex.algorithm.test.TraceTest.ExperimentEntry;
import isochrones.utils.BenchmarkUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * <p>The <code>BenchmarkInfo</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class BenchmarkInfo {
  //an ordered hashset with the durations
  SortedMap<Integer, SortedSet<LinkExperiment>> experiments = new TreeMap<Integer, SortedSet<LinkExperiment>>();
  
  public void addExperiment(int linkId, SortedMap<Integer, ExperimentEntry> exps) {
    for (Entry<Integer, ExperimentEntry> entry : exps.entrySet()) {
      SortedSet<LinkExperiment> experiment;
      int duration = entry.getKey();
      if(experiments.containsKey(duration)){
        experiment = experiments.get(duration);
      } else {
        experiment = new TreeSet<LinkExperiment>();
      }
      experiment.add(new LinkExperiment(linkId, entry.getValue()));
      experiments.put(duration, experiment);        
    }
  }
  
  public void logInFiles(BenchmarkUtil util) {
    
    String logDir = util.getOutputDir() + System.getProperty("file.separator");
    
    String filePraefix = util.getDataset() + "_" + util.getAlgorithmName() + 
    "_" + util.getTimeAsString() + util.getMode();
    
    File  speedFile = new File(logDir + filePraefix + "-speed.dat");
    File netMemoryFile = new File(logDir + filePraefix + "-memory.dat");
    
    FileWriter fWriter;
    try {
      
      new File(logDir).mkdir();
      
      fWriter = new FileWriter(speedFile);
      BufferedWriter out = new BufferedWriter(fWriter);
      StringBuffer runtimeBuf = new StringBuffer();
      StringBuffer memoryBuf = new StringBuffer();
      runtimeBuf.append("#dMax");
      for(LinkExperiment le : experiments.get(experiments.firstKey())) {
        runtimeBuf.append("\t").append(le.getLinkId());      
      }
      runtimeBuf.append("\n");
      memoryBuf.append(runtimeBuf);
      for (int duration : experiments.keySet()) {
        SortedSet<LinkExperiment> linkExps = experiments.get(duration);
        runtimeBuf.append(duration);
        memoryBuf.append(duration);
        for (LinkExperiment linkExperiment : linkExps) {
          ExperimentEntry exp = linkExperiment.getExperiment();
          runtimeBuf.append("\t").append(exp.getRunTime());
          memoryBuf.append("\t").append(exp.getMemorySize());
        }
        runtimeBuf.append("\n");
        memoryBuf.append("\n");
      }
      out.write(runtimeBuf.toString());
      out.close();
      fWriter.close();
      
      fWriter = new FileWriter(netMemoryFile);
      out = new BufferedWriter(fWriter);
      out.write(memoryBuf.toString());
      out.close();
      fWriter.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    System.out.println("Runtime output file:" + speedFile.getAbsoluteFile().toString());
    System.out.println("Memory output file:" +  netMemoryFile.getAbsoluteFile().toString());
/*
    
    System.out.print("#dMax");
    for(LinkExperiment le : experiments.get(experiments.firstKey())) {
      System.out.print("\t" + le.getLinkId());      
    }
    System.out.println();
    for (int duration : experiments.keySet()) {
      SortedSet<LinkExperiment> linkExps = experiments.get(duration);
      System.out.print(duration + "\t");
      for (LinkExperiment linkExperiment : linkExps) {
        ExperimentEntry exp = linkExperiment.getExperiment();
        System.out.print(exp.getRunTime() + "\t");
      }
      System.out.println();
    }
    */
  }

  class LinkExperiment implements Comparable<LinkExperiment>{
    int linkId;
    ExperimentEntry experiment;
    
    public LinkExperiment(int linkId, ExperimentEntry exp) {
      this.linkId = linkId;
      this.experiment = exp;
    }
    
    public int getLinkId() {
      return linkId;
    }
    
    public ExperimentEntry getExperiment() {
      return experiment;
    }
    
    @Override
    public int compareTo(LinkExperiment other) {
      int otherLinkId = other.getLinkId();
      if(otherLinkId<linkId) return 1;
      if(otherLinkId>linkId) return -1;
      return 0;
    }
    
  }
  
}
