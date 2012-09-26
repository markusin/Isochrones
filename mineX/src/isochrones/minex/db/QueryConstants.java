package isochrones.minex.db;

import isochrones.network.NWMode;
import isochrones.utils.Config;

/**
 * The <code>IQuery</code> interface provides only constant query strings that can be used in both db vendors.
 * 
 * @author Markus Innerebner
 * @version 3.0
 */
public class QueryConstants {
  
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
      return "SELECT SOURCE,TARGET,SOURCE_C_OUTDEGREE NODE_DEGREE,LENGTH FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    } else {
      return "SELECT SOURCE,TARGET,TARGET_C_INDEGREE NODE_DEGREE, LENGTH FROM " + config.getEdgeTable()
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
      return "SELECT SOURCE,TARGET,SOURCE_OUTDEGREE NODE_DEGREE,LENGTH FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    } else {
      return "SELECT TARGET,TARGET,TARGET_INDEGREE NODE_DEGREE, LENGTH FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    }
  }
  
  /**
   * <p>
   * Method getContinuousNodeQueryString
   * </p>
   * 
   * @param config
   * @return
   */
  public static final String getContinuousNodeQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT SOURCE_C_OUTDEGREE NODE_DEGREE FROM " + config.getEdgeTable() + " WHERE SOURCE=? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    } else {
      return "SELECT TARGET_C_INDEGREE  NODE_DEGREE FROM " + config.getEdgeTable() + " WHERE TARGET=? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    }
  }

  /**
   * <p>
   * Method getNodeQueryString
   * </p>
   * 
   * @param config
   * @return
   */
  public static final String getNodeQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT SOURCE_OUTDEGREE NODE_DEGREE FROM " + config.getEdgeTable() + " WHERE SOURCE=?";
    } else {
      return "SELECT TARGET_INDEGREE  NODE_DEGREE FROM " + config.getEdgeTable() + " WHERE TARGET=?";
    }
  }

  /**
   * <p>
   * Method getAdjacentLinksQueryString
   * </p>
   * 
   * @param config
   * @return
   */
  public static final String getAdjacentLinksQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT ID,SOURCE NODE_ID,SOURCE_OUTDEGREE NODE_DEGREE,LENGTH,EDGE_MODE,ROUTE_ID FROM " + config.getEdgeTable() + " WHERE TARGET=?";
    } else {
      return "SELECT ID,TARGET NODE_ID,TARGET_INDEGREE NODE_DEGREE, LENGTH,EDGE_MODE,ROUTE_ID FROM " + config.getEdgeTable() + " WHERE SOURCE=?";
    }
  }

  /**
   * <p>
   * Method getAdjacentContinuousLinksQueryString
   * </p>
   * 
   * @param config
   * @return
   */
  public static final String getAdjacentContinuousLinksQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT ID,SOURCE NODE_ID,SOURCE_C_OUTDEGREE NODE_DEGREE,LENGTH FROM " + config.getEdgeTable() + " WHERE TARGET=? AND EDGE_MODE="
          + NWMode.CONTINUOUS;
    } else {
      return "SELECT ID,TARGET NODE_ID, TARGET_C_INDEGREE NODE_DEGREE,LENGTH FROM " + config.getEdgeTable() + " WHERE TARGET=? AND EDGE_MODE="
          + NWMode.CONTINUOUS;
    }
  }

}
