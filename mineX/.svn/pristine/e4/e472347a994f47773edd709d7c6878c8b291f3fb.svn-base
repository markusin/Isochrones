package isochrones.minex.utils;

import isochrones.launchers.experiments.AbstractMemoryDurationExperiment;
import isochrones.minex.algorithm.test.MemoryDurationTest;
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
    experiment.compute(MemoryDurationTest.class.getName());
  }
}
