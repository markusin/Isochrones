/**
 * 
 */
package isochrones.db;

import isochrones.network.link.ILink;
import isochrones.network.node.Entity;
import isochrones.utils.Config;
import isochrones.utils.DBUtility;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

/**
 * <p>
 * The <code>CommonQuery</code> class
 * </p>
 * <p>
 * Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a>
 * </p>
 * <p>
 * Domenikanerplatz - Bozen, Italy.
 * </p>
 * <p>
 * </p>
 * 
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public abstract class IsochroneQuery {

  protected Config config;
  protected Connection connection;
  protected static String QUERY_EARLIEST_ARRIVAL_TIME_HOMO, QUERY_EARLIEST_ARRIVAL_TIMES_HETERO;
  protected static String QUERY_LATEST_DEPARTURE_TIME_HOMO, QUERY_LATEST_DEPARTURE_TIMES_HETERO;
  protected static String QUERY_LOG_VERTICES;
  protected static String QUERY_LOAD_ENTITIES, QUERY_INSERT_ENTITIES;

  protected static final Logger LOGGER = Logger.getLogger(IsochroneQuery.class.getPackage().getName());

  /**
   * <p>
   * Constructs a(n) <code>IsochroneQuery</code> object.
   * </p>
   * 
   * @param database the used database vendor (supported Oracle or PostgreSQL)
   */
  public IsochroneQuery(Config config) {
    this.config = config;
    this.connection = config!= null ? config.getConnection() : null;
    // initConstants();
  }

  protected void initConstants() {

    QUERY_LATEST_DEPARTURE_TIME_HOMO = "SELECT MAX(TIME_D) CLOSTEST_TIME FROM ("
        + " SELECT TIME_D FROM "
        + config.getScheduleTable()
        + " WHERE SOURCE = ? AND  TARGET = ? AND ROUTE_ID IN (%S) AND TIME_A>=? AND TIME_A<=? AND SERVICE_ID IN (%S)) C WHERE TIME_D >= ?";

    QUERY_EARLIEST_ARRIVAL_TIME_HOMO = "SELECT MIN(TIME_A) CLOSTEST_TIME FROM (" + " SELECT TIME_A FROM "
        + config.getScheduleTable()
        + " WHERE SOURCE = ? AND TARGET = ? AND ROUTE_ID IN (%S) AND TIME_D>=? AND TIME_D<=?"
        + " AND SERVICE_ID IN (%S)) C WHERE TIME_A <= ?";

    QUERY_LATEST_DEPARTURE_TIMES_HETERO = "SELECT SOURCE, MAX(TIME_D) CLOSTEST_TIME FROM (" + " SELECT SOURCE,TIME_D FROM "
        + config.getScheduleTable() + " WHERE TARGET = ? AND ROUTE_ID IN (%S) AND TIME_A>=? AND TIME_A<=?"
        + " AND SERVICE_ID IN (%S)) C WHERE TIME_D >= ? GROUP BY SOURCE";

    QUERY_EARLIEST_ARRIVAL_TIMES_HETERO = "SELECT TARGET, MIN(TIME_A) CLOSTEST_TIME FROM (" + " SELECT TARGET,TIME_A FROM "
        + config.getScheduleTable() + " WHERE SOURCE = ? AND ROUTE_ID IN (%S) AND TIME_D>=? AND TIME_D<=?"
        + " AND SERVICE_ID IN (%S)) C WHERE TIME_A <= ? GROUP BY SOURCE";

    QUERY_LOG_VERTICES = "INSERT INTO " + config.getProperty("tbl.log_vertex") + " (ID,DISTANCE) VALUES (?,?)";

    QUERY_LOAD_ENTITIES = "SELECT EDGE_ID,ENTITY_ID,START_OFFSET FROM " + config.getProperty("tbl.entities.mapping");

    QUERY_INSERT_ENTITIES = "INSERT INTO " + config.getDestinationEntityTableEntry().getTableName()
        + " (\"ENTITY_ID\",\"DISTANCE\",\"K_SEQ\",\"GEOMETRY\") (SELECT ENTITY_ID,?,?,GEOMETRY FROM " + config.getProperty("tbl.entities") 
        + " WHERE ENTITY_ID=?)";

  }

  /**
   * <p>
   * Method getWeekDay
   * </p>
   * Returns the week-day converted from integer to string.
   * 
   * @param day
   * @return
   */
  protected final String getWeekDay(Calendar day) {
    switch (day.get(Calendar.DAY_OF_WEEK)) {
      case Calendar.SUNDAY:
        return "SUNDAY";
      case Calendar.MONDAY:
        return "MONDAY";
      case Calendar.TUESDAY:
        return "TUESDAY";
      case Calendar.WEDNESDAY:
        return "WEDNESDAY";
      case Calendar.THURSDAY:
        return "THURSDAY";
      case Calendar.FRIDAY:
        return "FRIDAY";
      case Calendar.SATURDAY:
        return "SATURDAY";
      default:
        return "MONDAY";
    }
  }

  /**
   * <p>
   * Method preparePlaceHolders
   * </p>
   * 
   * @param length
   * @return the string with all the placeholders
   */
  protected final String preparePlaceHolders(int length) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length;) {
      builder.append("?");
      if (++i < length) {
        builder.append(",");
      }
    }
    return builder.toString();
  }

  /**
   * <p>
   * Method setValues
   * </p>
   * 
   * @param startIdx
   * @param preparedStatement
   * @param values
   * @throws SQLException
   * @return int the last set index
   */
  protected final int setValues(int startIdx, PreparedStatement preparedStatement, Object... values)
      throws SQLException {
    for (int i = 0; i < values.length; i++) {
      preparedStatement.setObject(startIdx + i, values[i]);
    }
    return startIdx + values.length;
  }

  /**
   * <p>
   * Method getDateCodes
   * </p>
   * returns the service id(s) of the given day The business day is stored in the column: SERVICE_ID
   * 
   * @param day
   * @return a DB result set with at least one service id
   * @throws SQLException
   */
  public final DBResult getDateCodes(Calendar day) throws SQLException {
    PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT SERVICE_ID " + "FROM "
        + config.getDaymarkerTable() + " WHERE " + getWeekDay(day) + " ='1'"
        + " AND START_DATE<=? AND END_DATE>=? ORDER BY SERVICE_ID");
    Date date = new Date(day.getTimeInMillis());
    statement.setDate(1, date);
    statement.setDate(2, date);
    return new DBResult(statement, statement.executeQuery());
  }

  /**
   * 
   /** Returns a <code>DBResult</code> containing the properties of a single link.
   * <p>
   * 
   * @param linkId
   * @return
   * @throws SQLException
   */
  public abstract DBResult getContinuousLink(int linkId) throws SQLException;

  /**
   * Returns a <code>DBResult</code> containing the properties of a single pedestrian link.
   * <p>
   * 
   * @param linkId the ID that uniquely identifies the pedestrian link
   * @return the query result in form of a <code>DBResult</code> object
   * @throws java.sql.SQLException
   */
  public abstract DBResult getLink(int linkId) throws SQLException;

  /**
   * <p>
   * Method getContinuousNode
   * </p>
   * 
   * @param nodeId
   * @return
   * @throws SQLException
   */
  public abstract DBResult getContinuousNode(int nodeId) throws SQLException;

  /**
   * <p>
   * Method getNode
   * </p>
   * 
   * @param nodeId
   * @return
   * @throws SQLException
   */
  public abstract DBResult getNode(int nodeId) throws SQLException;

  /**
   * <p>
   * Method getIncidentContinuousLinks
   * </p>
   * return the result object with the adjacent continuous links. Is invoked only in continuous mode
   * 
   * @param nodeId the id of the node on which to find the adjacent links
   * @return
   * @throws SQLException
   */
  public abstract DBResult getAdjacentContinuousLinks(int nodeId) throws SQLException;

  /**
   * <p>
   * Method getIncidentLinks
   * </p>
   * returns the result object with all incident links
   * 
   * @param nodeId the id of the node on which to find the adjacent links
   * @return
   * @throws SQLException
   */
  public abstract DBResult getAdjacentLinks(int nodeId) throws SQLException;

  /**
   * <p>
   * Method storeLinks
   * </p>
   * 
   * @param links the set of links to be stored
   */
  public abstract void storeLinks(Collection<ILink> links);

  /**
   * <p>
   * Method storeEntity
   * </p>
   * 
   * @param entities
   * @param destTable
   * @param sourceTable
   */
  public abstract void storeEntities(Collection<Entity> entities);

  /**
   * <p>
   * Method clearResultTable
   * </p>
   * 
   * @param tableName
   */
  public void clearResultTable() {
    DBUtility.truncateTable(connection, config.getDestinationEdgeTableEntry().getTableName());
    if (config.isKNN()) {
      DBUtility.truncateTable(connection, config.getDestinationEntityTableEntry().getTableName());
    }
  }

  /**
   * 
   * <p>Method controlDestinationIndex</p>
   * @param disabled
   */
  public void controlDestinationIndex(boolean disabled) {
    DBUtility.controlIndex(connection, config.getDbVendor(), config.getDestinationEdgeTableEntry(), disabled);
    if (config.isKNN()) {
      DBUtility.controlIndex(connection, config.getDbVendor(), config.getDestinationEntityTableEntry(), disabled);
    }
  }

  /**
   * <p>
   * Method controlDestinationEdgeIndex
   * </p>
   * 
   * @param disabled
   */
  public void controlDestinationEdgeIndex(boolean disabled) {
    DBUtility.controlIndex(connection, config.getDbVendor(), config.getDestinationEdgeTableEntry(), disabled);
  }

  /**
   * <p>
   * Method getLatestDepartureTime
   * </p>
   * computes the latest departure time for a specific edge (means source and target vertex as well the route id are
   * known). This query is the most selective.
   * 
   * @param source
   * @param target
   * @param routeIds
   * @param businessDay
   * @param targetTime the time at which the target vertex is visited
   * @param earliestSourceTime
   * @return
   * @throws SQLException
   */
  public DBResult getLatestDepartureTime(int sourceId, int targetId, Set<Short> routeIds, Set<Integer> businessDay,
                                         long fromTime, long toTime) throws SQLException {
    String sql = String.format(QUERY_LATEST_DEPARTURE_TIME_HOMO, preparePlaceHolders(routeIds.size()),
        preparePlaceHolders(businessDay.size()));
    PreparedStatement statement = connection.prepareStatement(sql);
    statement = connection.prepareStatement(sql);
    statement.setInt(1, sourceId);
    statement.setInt(2, targetId);
    int idx = setValues(3, statement, routeIds.toArray());
    statement.setLong(idx++, fromTime);
    statement.setLong(idx++, toTime);
    idx = setValues(idx, statement, businessDay.toArray());
    statement.setLong(idx, fromTime);
    return new DBResult(statement, statement.executeQuery());
  }

  /**
   * <p>
   * Method getEarliestArrivalTime
   * </p>
   * computes the earliest arrival time for a specific edge (means source and target vertex as well the route id are
   * known). This query is the most selective.
   * 
   * @param source
   * @param target
   * @param routeIds
   * @param businessDay
   * @param fromTime the time at which the source vertex is visited
   * @param toTime the latest possible time at which the target vertex can be visited
   * @return
   * @throws SQLException
   */
  public DBResult getEarliestArrivalTime(int sourceId, int targetId, Set<Short> routeIds, Set<Integer> businessDay,
                                         long fromTime, long toTime) throws SQLException {
    String sql = String.format(QUERY_EARLIEST_ARRIVAL_TIME_HOMO, preparePlaceHolders(routeIds.size()),
        preparePlaceHolders(businessDay.size()));
    PreparedStatement statement = connection.prepareStatement(sql);
    statement = connection.prepareStatement(sql);
    statement.setInt(1, sourceId);
    statement.setInt(2, targetId);
    int idx = setValues(3, statement, routeIds.toArray());
    statement.setLong(idx++, fromTime);
    statement.setLong(idx++, toTime);
    idx = setValues(idx, statement, businessDay.toArray());
    statement.setLong(idx, toTime);
    return new DBResult(statement, statement.executeQuery());
  }

  /**
   * <p>
   * Method getLatestDepartureTimes
   * </p>
   * computes the latest departure time for a specific target node having more than one incoming node. The query does a
   * group by over the in nodes and returns the closest (smaller) departure time for each incoming node.
   * 
   * @param targetId
   * @param routeIds
   * @param businessDay
   * @param fromTime
   * @param toTime
   * @return
   * @throws SQLException
   */
  public DBResult getLatestDepartureTimes(int targetId, Set<Short> routeIds, Set<Integer> businessDay, long fromTime,
                                          long toTime) throws SQLException {
    String sql = String.format(QUERY_LATEST_DEPARTURE_TIMES_HETERO, preparePlaceHolders(routeIds.size()),
        preparePlaceHolders(businessDay.size()));
    PreparedStatement statement = connection.prepareStatement(sql);
    statement = connection.prepareStatement(sql);
    statement.setInt(1, targetId);
    int idx = setValues(2, statement, routeIds.toArray());
    statement.setLong(idx++, fromTime);
    statement.setLong(idx++, toTime);
    idx = setValues(idx, statement, businessDay.toArray());
    statement.setLong(idx, fromTime);
    return new DBResult(statement, statement.executeQuery());
  }

  /**
   * <p>
   * Method getEarliestArrivalTimes
   * </p>
   * computes the latest departure time for a specific target node having more than one incoming node. The query does a
   * group by over the in nodes and returns the closest (smaller) departure time for each incoming node.
   * 
   * @param sourceId
   * @param routeIds
   * @param businessDay
   * @param fromTime
   * @param toTime
   * @return
   * @throws SQLException
   */
  public DBResult getEarliestArrivalTimes(int sourceId, Set<Short> routeIds, Set<Integer> businessDay, long fromTime,
                                          long toTime) throws SQLException {
    String sql = String.format(QUERY_EARLIEST_ARRIVAL_TIMES_HETERO, preparePlaceHolders(routeIds.size()),
        preparePlaceHolders(businessDay.size()));
    PreparedStatement statement = connection.prepareStatement(sql);
    statement = connection.prepareStatement(sql);
    statement.setInt(1, sourceId);
    int idx = setValues(2, statement, routeIds.toArray());
    statement.setLong(idx++, fromTime);
    statement.setLong(idx++, toTime);
    idx = setValues(idx, statement, businessDay.toArray());
    statement.setLong(idx, toTime);
    return new DBResult(statement, statement.executeQuery());
  }

  /**
   * <p>
   * Method logVertices
   * </p>
   * 
   * @param vertexId
   * @param distance
   * @return
   */
  public abstract void logVertices(int vertexId, double distance);

  /**
   * <p>
   * Method loadEntities
   * </p>
   * 
   * @return
   * @throws SQLException
   */
  public DBResult loadEntities() throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_LOAD_ENTITIES);
    return new DBResult(statement, statement.executeQuery());
  }

}
