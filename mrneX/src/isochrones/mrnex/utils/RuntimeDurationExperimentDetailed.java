package isochrones.mrnex.utils;

import isochrones.launchers.experiments.AbstractRuntimeDurationDetailedExperiment;
import isochrones.utils.RuntimeDurationUtil;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author Markus Innerebner
 */
public class RuntimeDurationExperimentDetailed extends AbstractRuntimeDurationDetailedExperiment {

  public RuntimeDurationExperimentDetailed(RuntimeDurationUtil util) {
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
    RuntimeDurationUtil util = new RuntimeDurationUtil(args, '=');
    AbstractRuntimeDurationDetailedExperiment experiment = new RuntimeDurationExperimentDetailed(util);
    experiment.compute(util.getAlgorithmClassName());
  }

}