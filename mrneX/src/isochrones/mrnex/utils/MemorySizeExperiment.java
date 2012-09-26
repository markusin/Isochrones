package isochrones.mrnex.utils;

import isochrones.launchers.experiments.AbstractMemorySizeExperiment;
import isochrones.utils.MemorySizeUtil;

/**
 * @author Markus Innerebner
 */
public class MemorySizeExperiment extends AbstractMemorySizeExperiment {
  
  public MemorySizeExperiment(MemorySizeUtil util)  {
    super(util);
  }
  
  public static void main(String[] args) {
    MemorySizeUtil util = new MemorySizeUtil(args, '=');
    AbstractMemorySizeExperiment experiment = new MemorySizeExperiment(util);
    experiment.compute(util.getAlgorithmClassName());
  } 
}
