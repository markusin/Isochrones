package isochrones.mrnex.utils;

import isochrones.launchers.experiments.AbstractMemoryDurationExperiment;
import isochrones.utils.MemoryDurationUtil;

/**
 * @author Markus Innerebner
 */
public class MemoryDurationExperiment extends AbstractMemoryDurationExperiment {
  
  public MemoryDurationExperiment(MemoryDurationUtil util) {
    super(util);
  }

  public static void main(String[] args) {
    MemoryDurationUtil util = new MemoryDurationUtil(args, '=');
    AbstractMemoryDurationExperiment experiment = new MemoryDurationExperiment(util);
    System.out.println("Classname:" + util.getAlgorithmClassName());
    experiment.compute(util.getAlgorithmClassName());
  }
}