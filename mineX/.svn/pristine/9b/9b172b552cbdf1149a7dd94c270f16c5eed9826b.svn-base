package isochrones.minex.utils;

import isochrones.launchers.experiments.AbstractRuntimeSizeExperiment;
import isochrones.minex.algorithm.test.RuntimeSizeTest;
import isochrones.utils.RuntimeSizeUtil;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Markus Innerebner
 */
public class RuntimeSizeExperiment extends AbstractRuntimeSizeExperiment {

  public RuntimeSizeExperiment(RuntimeSizeUtil util) {
    super(util);
  }

  /**
   * <p>
   * Method main
   * </p>
   * 
   * @param args
   * @throws SQLException
   * @throws IOException
   */
  public static void main(String[] args) throws SQLException, IOException {
    RuntimeSizeUtil util = new RuntimeSizeUtil(args, '=');
    AbstractRuntimeSizeExperiment experiment = new RuntimeSizeExperiment(util);
    experiment.compute(RuntimeSizeTest.class.getName());
  }
}