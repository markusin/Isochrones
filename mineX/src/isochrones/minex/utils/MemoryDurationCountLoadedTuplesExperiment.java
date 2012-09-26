package isochrones.minex.utils;

import isochrones.launchers.experiments.AbstractMemoryDurationCountLoadedTuplesExperiment;
import isochrones.minex.algorithm.MineX;
import isochrones.utils.MemoryDurationUtil;

/**
 * @author Markus Innerebner
 */
public class MemoryDurationCountLoadedTuplesExperiment extends AbstractMemoryDurationCountLoadedTuplesExperiment {
    
    public MemoryDurationCountLoadedTuplesExperiment(MemoryDurationUtil util) {
      super(util);
    }

    public static void main(String[] args) {
      MemoryDurationUtil util = new MemoryDurationUtil(args, '=');
      AbstractMemoryDurationCountLoadedTuplesExperiment experiment = new MemoryDurationCountLoadedTuplesExperiment(util);
      experiment.compute(MineX.class.getName());
    }
    
}