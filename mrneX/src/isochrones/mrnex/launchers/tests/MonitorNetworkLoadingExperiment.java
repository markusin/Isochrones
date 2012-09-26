package isochrones.mrnex.launchers.tests;

import isochrones.mrnex.launchers.experiments.AbstractMonitorNetworkLoadingExperiment;
import isochrones.mrnex.utils.MonitorNetworkLoadingUtil;

import java.io.IOException;
import java.sql.SQLException;

public class MonitorNetworkLoadingExperiment extends AbstractMonitorNetworkLoadingExperiment {


  public MonitorNetworkLoadingExperiment(MonitorNetworkLoadingUtil util) {
    super(util);
  }
  
  /**
   * 
   * <p>Method main</p>
   * @param args
   * @throws SQLException
   * @throws IOException
   */
  public static void main(String[] args) {
    MonitorNetworkLoadingUtil util = new MonitorNetworkLoadingUtil(args, '=');
    AbstractMonitorNetworkLoadingExperiment experiment = new MonitorNetworkLoadingExperiment(util);
    experiment.compute(util.getAlgorithmClassName());
  }
}
