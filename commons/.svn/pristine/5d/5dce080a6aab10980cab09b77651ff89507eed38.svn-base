package isochrones.db;

import isochrones.network.NWMode;
import isochrones.utils.Config;

public class CommonQueryConstants {
  
  /**
   * <p>
   * Method getContinuousLinkQueryString
   * </p>
   * 
   * @param config
   * @return
   */
  public static String getContinuousLinkQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT SOURCE, SOURCE_C_OUTDEGREE, TARGET, LENGTH FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    } else {
      return "SELECT TARGET, TARGET_C_INDEGREE, TARGET, LENGTH FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    }
  }

  /**
   * <p>
   * Method getLinkQueryString
   * </p>
   * 
   * @param config
   * @return
   */
  public static String getLinkQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT SOURCE, SOURCE_OUTDEGREE, TARGET, LENGTH FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    } else {
      return "SELECT TARGET, TARGET_INDEGREE, TARGET, LENGTH FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    }
  }
}
