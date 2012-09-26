package isochrones.web.db;

import isochrones.network.NWMode;
import isochrones.web.config.Config;

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
      return "SELECT SOURCE,TARGET,TARGET_INDEGREE  NODE_DEGREE, LENGTH FROM " + config.getEdgeTable()
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
      return "SELECT SOURCE_C_OUTDEGREE NODE_DEGREE FROM " + config.getEdgeTable() + " WHERE SOURCE=?";
    } else {
      return "SELECT TARGET_C_INDEGREE  NODE_DEGREE FROM " + config.getEdgeTable() + " WHERE TARGET=?";
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
      return "SELECT ID,TARGET NODE_ID, TARGET_C_INDEGREE NODE_DEGREE,LENGTH FROM " + config.getEdgeTable() + " WHERE SOURCE=? AND EDGE_MODE="
          + NWMode.CONTINUOUS;
    }
  }

  /**
   * <p>
   * Method getNodeRouteAnnotations
   * </p>
   * 
   * @param config
   * @return
   */
  public static final String getNodeRoutesAnnotations(Config config) {
    return "SELECT R.ROUTE_SHORT_NAME,R.ROUTE_TYPE FROM " + config.getRouteTable() + " R, "
        + config.getProperty("result.route_vertices") + " V" + " WHERE R.ID=V.ROUTE_ID";
  }

  /**
   * <p>
   * Method getInsertVertexAnnotationString
   * </p>
   * 
   * @param config
   * @return
   */
  public static String getInsertVertexAnnotationString(Config config) {
    return "INSERT INTO " + config.getDestinationVertexAnnotatedTableEntry().getTableName()
        + " (\"ID\", \"ROUTE_ID\",\"TIME_A\",\"TIME_D\") VALUES (?,?,?,?)";
  }

  /**
   * <p>
   * Method getInsertVertexString
   * </p>
   * 
   * @param config
   * @return
   */
  public static String getInsertVertexString(Config config) {
    return "INSERT INTO " + config.getDestinationVertexTableEntry().getTableName()
        + " (\"ID\",\"DISTANCE\",\"ROUTE_ID\",\"STATE\",\"GEOMETRY\") (SELECT ?,?,?,?,GEOMETRY FROM " + config.getVertexTable()
        + " WHERE ID=?)";
  }

  public static String getLatestDepartureTimeExactString(Config config) {
    return "SELECT MAX(TIME_D) TIME_D, MAX(TIME_A) TIME_A FROM (" + " SELECT TIME_D, TIME_A FROM "
        + config.getScheduleTable() + " WHERE SOURCE = ? AND TARGET = ? AND ROUTE_ID IN (%S) AND TIME_A>=? AND TIME_A<=?"
        + " AND SERVICE_ID IN (%S)) C WHERE TIME_D >= ?";
  }
  
  public static String getEarliestArrivalTimeExactString(Config config) {
    return "SELECT MIN(TIME_D) TIME_D, MIN(TIME_A) TIME_A FROM (" + " SELECT TIME_D, TIME_A FROM "
        + config.getScheduleTable() + " WHERE SOURCE = ? AND TARGET = ? AND ROUTE_ID IN (%S) AND TIME_D>=? AND TIME_D<=?"
        + " AND SERVICE_ID IN (%S)) C WHERE TIME_A <= ?";
  }

  public static String getLatestDepartureTimeHomoString(Config config) {
    return "SELECT MAX(TIME_D) TIME_D, MAX(TIME_A) TIME_A, ROUTE_ID FROM (" + " SELECT TIME_D, TIME_A, ROUTE_ID FROM "
        + config.getScheduleTable() + " WHERE SOURCE = ? AND TARGET = ? AND ROUTE_ID IN (%S) AND TIME_A>=? AND TIME_A<=?"
        + " AND SERVICE_ID IN (%S)) C WHERE TIME_D >= ?" + " GROUP BY ROUTE_ID";
  }
  
  public static String getEarliestArrivalTimeHomoString(Config config) {
    return "SELECT MIN(TIME_D) TIME_D, MIN(TIME_A) TIME_A, ROUTE_ID FROM (" + " SELECT TIME_D, TIME_A, ROUTE_ID FROM "
        + config.getScheduleTable() + " WHERE SOURCE = ? AND TARGET = ? AND ROUTE_ID IN (%S) AND TIME_D>=? AND TIME_D<=?"
        + " AND SERVICE_ID IN (%S)) C WHERE TIME_A <= ?" + " GROUP BY ROUTE_ID";
  }

  public static String getLatestDepartureTimeHeteroString(Config config) {
    return "SELECT SOURCE, MAX(TIME_D) TIME_D, MAX(TIME_A) TIME_A, ROUTE_ID FROM ("
        + " SELECT SOURCE,TIME_D,TIME_A,ROUTE_ID FROM " + config.getScheduleTable()
        + " WHERE TARGET = ? AND ROUTE_ID IN (%S) AND TIME_A>=? AND TIME_A<=?"
        + " AND SERVICE_ID IN (%S)) C WHERE TIME_D >= ? GROUP BY SOURCE, ROUTE_ID";
  }
  
  public static String getEarliestArrivalTimeHeteroString(Config config) {
    return "SELECT TARGET, MIN(TIME_D) TIME_D, MIN(TIME_A) TIME_A, ROUTE_ID FROM ("
        + " SELECT TARGET,TIME_D,TIME_A,ROUTE_ID FROM " + config.getScheduleTable()
        + " WHERE SOURCE = ? AND ROUTE_ID IN (%S) AND TIME_D>=? AND TIME_D<=?"
        + " AND SERVICE_ID IN (%S)) C WHERE TIME_A <= ? GROUP BY TARGET, ROUTE_ID";
  }
  
  public static String getVertexAnnotations(Config config) {
    return "SELECT TO_TIMESTAMP(V.TIME_A,'SSSSS') ARRIVAL_TIME, TO_TIMESTAMP(V.TIME_D,'SSSSS') DEPARTURE_TIME, R.ROUTE_SHORT_NAME, R.ROUTE_TYPE FROM " 
        + config.getDestinationVertexAnnotatedTableEntry().getTableName() + " V, " + config.getRouteTable() 
        + " R WHERE V.ROUTE_ID=R.ROUTE_ID";
    
  }

  public static String getIsoEdges(Config config) {
    return "SELECT E.\"ID\", E.\"SOURCE\", V.\"DISTANCE\" DISTANCE, E.\"TARGET\", E.\"OFFSET\", E.\"LENGTH\",E.\"GEOMETRY\" FROM "
        + config.getDestinationEdgeTableEntry().getTableName() + " E, "
        + config.getDestinationVertexTableEntry().getTableName() + " V "
        // + " WHERE E.SOURCE = V.ID ORDER BY SOURCE_DISTANCE DESC";
        + " WHERE E.\"SOURCE\" = V.\"ID\" ORDER BY \"OFFSET\" " + (config.isIncoming() ? "DESC" : "ASC");
  }
  

}
