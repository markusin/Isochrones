package isochrones.web.test;

import isochrones.algorithm.TableEntry;
import isochrones.algorithm.TableType;
import isochrones.web.config.Config;
import isochrones.web.coverage.ReachabilityTool;
import isochrones.web.utils.DBUtility;
import isochrones.web.utils.WebLauncherUtil;

public class CoverageTest {

  /**
   * <p>
   * Method main
   * </p>
   * 
   * @param args
   */
  public static void main(String[] args) {
    long bufferDistance = 30;
    WebLauncherUtil launcherUtil = new WebLauncherUtil(args, '=');
    Config config = launcherUtil.getConfig();
    ReachabilityTool tool = new ReachabilityTool(config);
    TableEntry tableEntry = new TableEntry(config.getProperty("tbl.isoArea"), config.getProperty("idx.isoArea"), TableType.POLYGON_BUFFER);
    DBUtility.truncateTable(config.getConnection(), config.getDestinationAreaBufferTableEntry().getTableName());
    tool.createIsoAreaSBA(tableEntry, (int) launcherUtil.getMaxDuration(), bufferDistance, launcherUtil.getSpeed());
  }

}
