package isochrones.minex.utils;

import isochrones.launchers.experiments.AbstractRuntimeDurationExperiment;
import isochrones.minex.algorithm.test.RuntimeDurationTest;
import isochrones.utils.RuntimeDurationUtil;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Markus Innerebner
 */
public class RuntimeDurationExperiment extends AbstractRuntimeDurationExperiment {

  public RuntimeDurationExperiment(RuntimeDurationUtil util) {
    super(util);
  }
  
  /**
   * 
   * <p>Method main</p>
   * @param args
   * @throws SQLException
   * @throws IOException
   */
  public static void main(String[] args) throws SQLException, IOException {
    RuntimeDurationUtil util = new RuntimeDurationUtil(args, '=');
    AbstractRuntimeDurationExperiment experiment = new RuntimeDurationExperiment(util);
    experiment.compute(RuntimeDurationTest.class.getCanonicalName());
  }
}