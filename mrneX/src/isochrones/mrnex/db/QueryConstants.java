package isochrones.mrnex.db;

import isochrones.algorithm.Mode;
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
   * Method getAdjacentLinksQueryString
   * </p>
   * 
   * @param config
   * @return
   */
  public static final String getAdjacentLinksQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT ID,SOURCE NODE_ID,SOURCE_MODE NODE_MODE, SOURCE_OUTDEGREE DEGREE,LENGTH,EDGE_MODE,ROUTE_ID FROM " + config.getEdgeTable() + " WHERE TARGET=?";
    } else {
      return "SELECT ID,TARGET NODE_ID,TARGET_MODE NODE_MODE, TARGET_INDEGREE DEGREE,LENGTH,EDGE_MODE,ROUTE_ID FROM " + config.getEdgeTable() + " WHERE SOURCE=?";
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
      return "SELECT ID,SOURCE NODE_ID,SOURCE_MODE NODE_MODE," + (config.getMode().equals(Mode.UNIMODAL) ? "SOURCE_C_OUTDEGREE" : "SOURCE_OUTDEGREE") + " DEGREE,LENGTH FROM " + config.getEdgeTable() + " WHERE TARGET=? AND EDGE_MODE="
          + NWMode.CONTINUOUS;
    } else {
      return "SELECT ID,TARGET NODE_ID,TARGET_MODE NODE_MODE," + (config.getMode().equals(Mode.UNIMODAL) ? "TARGET_C_INDEGREE" : "TARGET_INDEGREE") + " DEGREE,LENGTH FROM " + config.getEdgeTable() + " WHERE SOURCE=? AND EDGE_MODE="
          + NWMode.CONTINUOUS;
    }
  }
  
  /**
   * 
   * <p>Method getAdjacentDiscreteLinksQueryString</p>
   * @param config
   * @return
   */
  public static final String getAdjacentDiscreteLinksQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT ID, SOURCE NODE_ID,SOURCE_OUTDEGREE DEGREE FROM " + config.getEdgeTable() + " WHERE TARGET=? AND EDGE_MODE="
          + NWMode.DISCRETE;
    } else {
      return "SELECT ID, TARGET NODE_ID,TARGET_INDEGREE DEGREE FROM " + config.getEdgeTable() + " WHERE SOURCE=? AND EDGE_MODE="
          + NWMode.DISCRETE;
    }
  }
  
  /**
   * 
   * <p>Method getAdjacentDiscreteLinksGeoQueryString</p>
   * @param config
   * @return
   */
  public static String getAdjacentDiscreteLinksGeoQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT ID,SOURCE NODE_ID,ROUTE_ID,SOURCE_X NODE_X,SOURCE_Y NODE_Y,SOURCE_OUTDEGREE DEGREE FROM " + config.getEdgeTable() + " WHERE TARGET = ? AND EDGE_MODE=" + NWMode.DISCRETE;
    } else {
      return "SELECT ID,TARGET NODE_ID,ROUTE_ID,TARGET_X NODE_X,TARGET_Y NODE_Y,TARGET_INDEGREE DEGREE, FROM " + config.getEdgeTable() + " WHERE SOURCE = ? AND EDGE_MODE=" + NWMode.DISCRETE;
    }
  }

  
  /**
   * <p>
   * Method getContinuousIncidentLinkQueryString
   * </p>
   * 
   * @param config
   * @return
   */
  public static final String getContinuousLinkQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT SOURCE, SOURCE_MODE, TARGET, TARGET_MODE,LENGTH,SOURCE_C_OUTDEGREE DEGREE FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    } else {
      return "SELECT SOURCE, SOURCE_MODE, TARGET, TARGET_MODE, LENGTH,TARGET_C_INDEGREE DEGREE FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    }
  }

  /**
   * <p>
   * Method getIncidentLinkQueryString
   * </p>
   * 
   * @param config
   * @return
   */
  public static final String getLinkQueryString(Config config) {
    if(config.isIncoming()){
      return "SELECT SOURCE, SOURCE_MODE, TARGET, TARGET_MODE,LENGTH,SOURCE_OUTDEGREE DEGREE FROM " + config.getEdgeTable()
          + " WHERE ID = ? AND EDGE_MODE=" + NWMode.CONTINUOUS;
    } else {
      return "SELECT SOURCE, SOURCE_MODE, TARGET, TARGET_MODE, LENGTH,TARGET_INDEGREE DEGREE FROM " + config.getEdgeTable()
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
      return "SELECT C_OUTDEGREE DEGREE,\"mode\" FROM " + config.getVertexTable() + " WHERE ID=?";
    } else {
      return "SELECT TARGET_C_INDEGREE DEGREE,\"mode\" FROM " + config.getVertexTable() + " WHERE ID=?";
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
      return "SELECT OUTDEGREE DEGREE,\"mode\" FROM " + config.getVertexTable() + " WHERE ID=?";
    } else {
      return "SELECT INDEGREE DEGREE,\"mode\" FROM " + config.getVertexTable() + " WHERE ID=?";
    }
  }

  public static String getCoordinatesFromGeoPoint(Config config) {
    // TODO Auto-generated method stub
    return null;
  }
  
  public static String getCoordinatesFromNode(Config config) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * 
   * <p>Method getVertexDensity</p>
   * @param config
   * @return
   */
  public static String getVertexDensity(Config config) {
    return "SELECT MAX(DENSITY) DENSITY FROM " + config.getProperty("tbl.vertex.density") + " WHERE ID=? AND DISTANCE<=? ";
  }
  
  /**
   * 
   * <p>Method getRange</p>
   * @param config
   * @return
   */
  public static String getRange(Config config) {
    return "SELECT MAX(DISTANCE) E_DIST FROM " + config.getProperty("tbl.vertex.density") + " WHERE ID=? AND DENSITY<=? ";
  }
  
  /**
   * 
   * <p>Method getVertexDensity</p>
   * @param config
   * @return
   */
  public static String getVertexDensity_(Config config) {
    return "SELECT * FROM ( SELECT DENSITY,E_DISTANCE FROM " + config.getProperty("tbl.vertex.density") + " WHERE ID=? " +
    		"AND DISTANCE<=? ORDER BY DENSITY DESC) X LIMIT 1";
  }

  
  /**
   * 
   * <p>Method getLogLoadedNodes</p>
   * @param config
   * @return
   */
  public static String getLogLoadedNodes(Config config) {
    return "INSERT INTO " + config.getProperty("tbl.log_nodes") + " (NODE_ID,DISTANCE,REMAINING_DISTANCE,\"GEOMETRY\") " + " SELECT ?,?,?,N.GEOMETRY FROM " + config.getVertexTable() + " N WHERE N.ID=?"; 
  }

  /**
   * 
   * <p>Method getLatestDepartureTimeWithSourceLocation</p>
   * @param config
   * @return
   */
  public static String getLatestDepartureTimeExactWithSourceLocation(Config config) {
    return 
    "SELECT MAX(TIME_D) TIME_D, SOURCE_X, SOURCE_Y FROM ("
    + " SELECT TIME_D, ST_X(SOURCE_GEO) SOURCE_X, ST_Y(SOURCE_GEO) SOURCE_Y FROM " + config.getScheduleTable() 
    + " WHERE TARGET = ? AND SOURCE = ? AND ROUTE_ID=? AND TIME_A>=? AND TIME_A<=? AND SERVICE_ID IN (%S)) C WHERE TIME_D >=? GROUP BY SOURCE_X, SOURCE_Y";
  }
  
  public static String getLatestDepartureTimeHomoWithSourceLocation(Config config) {
    return 
    "SELECT MAX(TIME_D) TIME_D, SOURCE_X, SOURCE_Y FROM ("
    + " SELECT TIME_D, ST_X(SOURCE_GEO) SOURCE_X, ST_Y(SOURCE_GEO) SOURCE_Y FROM " + config.getScheduleTable() 
    + " WHERE TARGET = ? AND SOURCE = ? AND TIME_A>=? AND TIME_A<=? AND SERVICE_ID IN (%S)) C WHERE TIME_D >=? GROUP BY SOURCE_X, SOURCE_Y";
  }
  
  public static String getLatestDepartureTimeHeteroWithSourceLocation(Config config) {
    return 
    "SELECT MAX(TIME_D) TIME_D, SOURCE, SOURCE_X, SOURCE_Y FROM ("
    + " SELECT TIME_D, SOURCE, ST_X(SOURCE_GEO) SOURCE_X, ST_Y(SOURCE_GEO) SOURCE_Y FROM " + config.getScheduleTable() 
    + " WHERE TARGET = ? AND TIME_A>=? AND TIME_A<=? AND SERVICE_ID IN (%S)) C WHERE TIME_D >=? GROUP BY SOURCE, SOURCE_X, SOURCE_Y";
  }

}
