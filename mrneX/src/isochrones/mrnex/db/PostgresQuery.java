/**
 * 
 */
package isochrones.mrnex.db;

import isochrones.db.AbstractPostgresQuery;
import isochrones.db.DBResult;
import isochrones.mrnex.network.node.ANode;
import isochrones.network.GeoPoint;
import isochrones.network.GeoQueryPoint;
import isochrones.network.NWMode;
import isochrones.utils.Config;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Set;

/**
 * <p>
 * The <code>PostgresQuery</code> class
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
public class PostgresQuery extends AbstractPostgresQuery implements IQuery {

  int insertOrderSequence = 1;
  private String QUERY_GET_COORDINATES_FROM_Q, QUERY_GET_COORDINATES_FROM_NODE, QUERY_GET_COORD_DENSITY_FROM_NODE;
  private String QUERY_GET_CONTINUOUSEDGES_IN_QRANGE, QUERY_GET_EDGES_IN_QRANGE,
      QUERY_GET_CONTINUOUSEDGES_IN_NODERANGE, QUERY_GET_EDGES_IN_NODERANGE;

  public String QUERY_GET_COORDINATES, QUERY_GET_DENSITY, QUERY_GET_RANGE;
  public String QUERY_GET_CONTINUOUS_NODE, QUERY_GET_NODE;
  public String QUERY_GETLATESTDEPARTURETIME_EXACT_WITHLOCATION, QUERY_GETLATESTDEPARTURETIME_HOMO_WITHLOCATION,
      QUERY_GETLATESTDEPARTURETIME_HETERO_WITHLOCATION;

  public String QUERY_LOG_LOADED_IER_QPOINT, QUERY_GET_EDGES_LIMITED_SIZE;

  public String QUERY_GET_LINK, QUERY_GET_CONTINUOUS_LINK, QUERY_ADJACENT_DISCRETE_LINKS,
      QUERY_ADJACENT_DISCRETE_LINKS_GEO;

  public PostgresQuery() {
    super(null);
  }

  public PostgresQuery(Config config) {
    super(config);
    initConstants();
  }

  @Override
  protected void initConstants() {
    super.initConstants();
    QUERY_GET_COORDINATES_FROM_Q = "SELECT ST_X(G.GEO) X, ST_Y(G.GEO) Y FROM (SELECT ST_LINE_INTERPOLATE_POINT(GEOMETRY,?/LENGTH) GEO FROM "
        + config.getEdgeTable() + " WHERE ID=?) G ";

    QUERY_GET_COORDINATES_FROM_NODE = "SELECT ID,ST_X(GEOMETRY) X, ST_Y(GEOMETRY) Y FROM " + config.getVertexTable()
        + " WHERE ID=?";
    
    QUERY_GET_COORD_DENSITY_FROM_NODE = "SELECT N.ID,ST_X(N.GEOMETRY) X, ST_Y(N.GEOMETRY) Y, "
        + "(SELECT MAX(e_dist) FROM " + config.getProperty("tbl.vertex.density") 
        + " WHERE ID=N.id AND DENSITY<=?) E_DIST FROM "+ config.getVertexTable()
        + " N WHERE N.ID=?";

    QUERY_GET_CONTINUOUSEDGES_IN_QRANGE = "SELECT L.ID, L.SOURCE, L.TARGET, L.LENGTH,"
        + (config.isIncoming() ? "L.SOURCE_C_OUTDEGREE" : "L.TARGET_C_INDEGREE") + " DEGREE FROM "
        + config.getEdgeTable() + " L, " + config.getVertexTable() + " N " + " WHERE L.EDGE_MODE=" + NWMode.CONTINUOUS
        + " AND L.TARGET=N.ID AND ST_DWITHIN(N.GEOMETRY,ST_PointFromText(?,?),?)='TRUE'";

    /*
     * QUERY_GET_EDGES_IN_QRANGE =
     * "SELECT L.ID, L.SOURCE,L.SOURCE_MODE,L.TARGET,L.TARGET_MODE,L.LENGTH,L.EDGE_MODE,L.ROUTE_ID,L.SOURCE_OUTDEGREE FROM "
     * + config.getEdgeTable() + " L, " + config.getVertexTable() + " N " +
     * " WHERE L.TARGET=N.ID AND ST_DWITHIN(N.GEOMETRY,ST_PointFromText(?,?),?)='TRUE'";
     */

    QUERY_GET_EDGES_IN_QRANGE = "SELECT L.ID, L.SOURCE,L.SOURCE_MODE,L.TARGET,L.TARGET_MODE,L.LENGTH,L.EDGE_MODE,L.ROUTE_ID,"
        + (config.isIncoming() ? "L.SOURCE_OUTDEGREE" : "L.TARGET_INDEGREE")
        + " DEGREE FROM "
        + config.getEdgeTable()
        + " L, "
        + config.getVertexTable()
        + " N "
        + " WHERE L."
        + (config.isIncoming() ? "TARGET" : "SOURCE")
        + "=N.ID" + " AND ST_DWITHIN(ST_PointFromText(?,?),N.GEOMETRY,?)='TRUE'";

    QUERY_GET_CONTINUOUSEDGES_IN_NODERANGE = "SELECT L.ID,L.SOURCE,L.TARGET,L.LENGTH,"
        + (config.isIncoming() ? "L.SOURCE_C_OUTDEGREE" : "L.TARGET_C_INDEGREE") + " DEGREE FROM "
        + config.getEdgeTable() + " L, " + config.getVertexTable() + " N "
        + "WHERE L.EDGE_MODE=" + NWMode.CONTINUOUS + " AND L." + (config.isIncoming() ? "TARGET" : "SOURCE") + "=N.ID"
        + " AND ST_DWITHIN(ST_PointFromText(?,?),N.GEOMETRY,?)='TRUE'";

    QUERY_GET_EDGES_IN_NODERANGE = "SELECT L.ID,L.SOURCE,L.TARGET,L.LENGTH,L.EDGE_MODE,L.ROUTE_ID,"
        + (config.isIncoming() ? "L.SOURCE_OUTDEGREE" : "L.TARGET_INDEGREE")
        + " DEGREE FROM "
        + config.getEdgeTable() + " L, "
        + config.getVertexTable() + " N "
        + "WHERE L."
        + (config.isIncoming() ? "TARGET" : "SOURCE")
        + "=N.ID"
        + " AND ST_DWITHIN(ST_PointFromText(?,?),N.GEOMETRY,?)";
    
    QUERY_GET_EDGES_LIMITED_SIZE = "SELECT L.ID,L.SOURCE,L.TARGET,L.LENGTH,L.EDGE_MODE,L.ROUTE_ID,"
        + (config.isIncoming() ? "L.SOURCE_OUTDEGREE" : "L.TARGET_INDEGREE") + " DEGREE FROM "
        + config.getEdgeTable() + " L, " + config.getVertexTable() + " N " 
        + "WHERE L."
        + (config.isIncoming() ? "TARGET" : "SOURCE")
        + "=N.ID"
        + " AND ST_DWITHIN(ST_PointFromText(?,?),N.GEOMETRY,(" +
        " SELECT MAX(DISTANCE) E_DIST FROM " + config.getProperty("tbl.vertex.density") + " WHERE ID=? AND DENSITY<=? ))";

    QUERY_GET_COORDINATES = "SELECT ID, ST_X(GEOMETRY) X, ST_Y(GEOMETRY) FROM " + config.getVertexTable()
        + " WHERE ID IN(%S)";

    QUERY_ADJACENT_DISCRETE_LINKS = QueryConstants.getAdjacentDiscreteLinksQueryString(config);
    QUERY_ADJACENT_DISCRETE_LINKS_GEO = QueryConstants.getAdjacentDiscreteLinksGeoQueryString(config);
    QUERY_GET_CONTINUOUS_NODE = QueryConstants.getContinuousNodeQueryString(config);
    QUERY_GET_NODE = "SELECT " + (config.isIncoming() ? "OUTDEGREE" : "INDEGREE") +
        " DEGREE, ST_X(GEOMETRY) X, ST_Y(GEOMETRY) Y FROM " + config.getVertexTable() + " WHERE ID=?"; 

    // QUERY_GET_DENSITY = "SELECT * FROM ( SELECT DENSITY,E_DISTANCE FROM "
    // + config.getProperty("tbl.vertex.density") + " WHERE ID=? " +
    // "AND DISTANCE<=? ORDER BY DENSITY DESC) X LIMIT 1";

    QUERY_GET_DENSITY = QueryConstants.getVertexDensity(config);

    QUERY_GET_RANGE =
    		"select D0.DENSITY s0, D0.E_DIST ed0, D1.DENSITY s1, D1.E_DIST ed1 from "
        + "(SELECT ID,DENSITY,E_DIST FROM " + config.getProperty("tbl.vertex.density") + " WHERE ID=? AND DENSITY<? ORDER BY DENSITY DESC LIMIT 1) D0 FULL JOIN"
        + "(SELECT ID,DENSITY,E_DIST FROM " + config.getProperty("tbl.vertex.density") + " WHERE ID=? AND DENSITY>=? ORDER BY DENSITY ASC LIMIT 1) D1 ON D0.ID=D1.ID " ;

    QUERY_GETLATESTDEPARTURETIME_EXACT_WITHLOCATION = QueryConstants
        .getLatestDepartureTimeExactWithSourceLocation(config);
    QUERY_GETLATESTDEPARTURETIME_HOMO_WITHLOCATION = QueryConstants
        .getLatestDepartureTimeHomoWithSourceLocation(config);
    QUERY_GETLATESTDEPARTURETIME_HETERO_WITHLOCATION = QueryConstants
        .getLatestDepartureTimeHeteroWithSourceLocation(config);
    QUERY_LOG_LOADED_IER_QPOINT = "INSERT INTO "
        + config.getProperty("tbl.log_circles")
        + "(NODE_ID,RADIUS,MAX_RADIUS,\"GEOMETRY\",INSERT_SEQ) SELECT ?,?,?,ST_Multi(ST_Buffer(ST_PointFromText(?,?),?,'quad_segs=8')),?";
    /*
    QUERY_E_NN = "SELECT L.ID,L.SOURCE,L.TARGET,L.LENGTH,L.EDGE_MODE,L.ROUTE_ID,"
        + (config.isIncoming() ? "L.SOURCE_OUTDEGREE" : "L.TARGET_INDEGREE")
        + " DEGREE, ST_Distance(N.GEOMETRY,Q.GEOMETRY) E_DIST FROM " + config.getEdgeTable() + " L, "
        + config.getVertexTable() + " N, " + "( SELECT ST_PointFromText(?,?) AS GEOMETRY) Q WHERE L."
        + (config.isIncoming() ? "TARGET" : "SOURCE") + "=N.ID "
        + " AND ST_DWITHIN(N.GEOMETRY,Q.GEOMETRY," +
        " SELECT MAX(DISTANCE) E_DIST FROM " + config.getProperty("tbl.vertex.density") + " WHERE ID=? AND DENSITY<=? )";
        */
  }

  @Override
  public DBResult getContinuousLink(int linkId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QueryConstants.getContinuousLinkQueryString(config));
    statement.setInt(1, linkId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLink(int linkId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QueryConstants.getLinkQueryString(config));
    statement.setInt(1, linkId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getAdjacentLinks(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QueryConstants.getAdjacentLinksQueryString(config));
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getAdjacentContinuousLinks(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QueryConstants
        .getAdjacentContinuousLinksQueryString(config));
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getAdjacentDiscreteLinks(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_ADJACENT_DISCRETE_LINKS);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getAdjacentDiscreteLinksGeo(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_ADJACENT_DISCRETE_LINKS_GEO);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getContinuousNode(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_CONTINUOUS_NODE);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  /*
   * (non-Javadoc)
   * @see isochrones.db.IsochroneQuery#getNode(int)
   */
  @Override
  public DBResult getNode(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_NODE);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getContinuousLinksInRange(GeoQueryPoint q, double range) throws SQLException {
    String pointString = "POINT(" + q.getX() + " " + q.getY() + ")";
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_CONTINUOUSEDGES_IN_QRANGE);
    statement.setString(1, pointString);
    statement.setInt(2, config.getServerSRID());
    statement.setDouble(3, range);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLinksInRange(GeoQueryPoint q, double range) throws SQLException {
    String pointString = "POINT( " + q.getX() + " " + q.getY() + ")";
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_EDGES_IN_QRANGE);
    statement.setString(1, pointString);
    statement.setInt(2, config.getServerSRID());
    statement.setDouble(3, range);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLinksInRange(GeoPoint q, double range, Collection<ANode> loadedNodes, String[] aois)
      throws SQLException {
    if (loadedNodes.isEmpty()) {
      return getLinksInRange(q, range);
    }

    ANode[] loadedAreas = loadedNodes.toArray(new ANode[0]);
    String disjunctAreaString = "st_difference(st_buffer(st_pointfromtext('POINT(" + q.getX() + " " + q.getY() + ")',"
        + config.getServerSRID() + ")," + range + ")," + subQuery(loadedAreas, loadedAreas.length - 1, config.getServerSRID()) + ")";
    aois[0] = disjunctAreaString;
    
    String query = "SELECT L.ID,L.SOURCE,L.SOURCE_MODE,L.TARGET,L.TARGET_MODE,L.LENGTH,L.EDGE_MODE,L.ROUTE_ID,"
        + (config.isIncoming() ? "L.SOURCE_OUTDEGREE" : "L.TARGET_INDEGREE") + " DEGREE FROM " + config.getEdgeTable()
        + " L, " + config.getVertexTable() + " N " + "WHERE L." + (config.isIncoming() ? "TARGET" : "SOURCE")
        + "=N.ID AND st_intersects(N.GEOMETRY," + disjunctAreaString + ")";
    Statement stmt = connection.createStatement();
    return new DBResult(stmt, stmt.executeQuery(query));
  }

  @Override
  public DBResult getContinuousLinksInRange(int nodeId, double range) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_CONTINUOUSEDGES_IN_NODERANGE);
    statement.setInt(1, nodeId);
    statement.setDouble(2, range);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLinksInRange(GeoPoint p, double range) throws SQLException {
    String pointString = "POINT( " + p.getX() + " " + p.getY() + ")";
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_EDGES_IN_NODERANGE);
    statement.setString(1, pointString);
    statement.setInt(2, config.getServerSRID());
    statement.setDouble(3, range);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getCoordinates(GeoQueryPoint q) throws SQLException {
    if (q.isNode()) {
      return getCoordinate(q.getId());
    } else {
      PreparedStatement statement = connection.prepareStatement(QUERY_GET_COORDINATES_FROM_Q);
      statement.setDouble(1, q.getStartOffset());
      statement.setDouble(2, q.getId());
      return new DBResult(statement, statement.executeQuery());
    }
  }

  @Override
  public void logLoadedIERCircle(int nodeId, double maxRadius, double loadedRange) {

    String sql = "INSERT INTO "
        + config.getProperty("tbl.log_circles")
        + "(NODE_ID,RADIUS,MAX_RADIUS,\"GEOMETRY\",INSERT_SEQ) SELECT N.ID,?,?,ST_Multi(ST_Buffer(N.geometry,?,'quad_segs=8')),"
        + insertOrderSequence++ + " from " + config.getVertexTable() + " N WHERE N.id=?";
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(sql);
      statement.setDouble(1, loadedRange);
      statement.setDouble(2, maxRadius);
      statement.setDouble(3, loadedRange);
      statement.setInt(4, nodeId);
      statement.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }

    }
  }

  @Override
  public void logLoadedIERCircle(GeoQueryPoint q, double maxRadius, double loadedRange) {
    String pointStr = "POINT(" + q.getX() + " " + q.getY() + ")";
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(QUERY_LOG_LOADED_IER_QPOINT);
      statement.setInt(1, q.getId());
      statement.setDouble(2, loadedRange);
      statement.setDouble(3, maxRadius);
      statement.setString(4, pointStr);
      statement.setInt(5, config.getServerSRID());
      statement.setDouble(6, loadedRange);
      statement.setInt(7, insertOrderSequence++);
      statement.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void logLoadedIERArea(int id, String[] aois, double range) throws SQLException {
    String pointStr = "INSERT INTO " + config.getProperty("tbl.log_circles")
        + "(NODE_ID,RADIUS,MAX_RADIUS,\"GEOMETRY\",INSERT_SEQ) SELECT ?,?,?,ST_Multi(" + aois[0] + "),?";
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(pointStr);
      statement.setInt(1, id);
      statement.setDouble(2, range);
      statement.setDouble(3, range);
      statement.setInt(4, insertOrderSequence++);
      statement.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      System.err.println("problems with inserting node with id: " + id);
      connection.rollback();
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }

  }

  @Override
  public void logLoadedIERNode(int nodeId, double distance, double remainingDistance) {
    String sql = QueryConstants.getLogLoadedNodes(config);
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(sql);
      statement.setInt(1, nodeId);
      statement.setDouble(2, distance);
      statement.setDouble(3, remainingDistance);
      statement.setInt(4, nodeId);
      statement.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (statement != null) {
        try {
          statement.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public DBResult getCoordinates(Integer[] nodeIds) throws SQLException {
    if (nodeIds.length > 1) {
      String sql = String.format(QUERY_GET_COORDINATES, preparePlaceHolders(nodeIds.length));
      PreparedStatement statement = connection.prepareStatement(sql);
      setValues(1, statement, nodeIds);
      return new DBResult(statement, statement.executeQuery());
    } else {
      return getCoordinate(nodeIds[0]);
    }
  }

  @Override
  public DBResult getCoordinate(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_COORDINATES_FROM_NODE);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }
  
  @Override
  public DBResult getCoordinate(int nodeId, int density) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_COORD_DENSITY_FROM_NODE);
    statement.setInt(1, density);
    statement.setInt(2, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getDensity(int nodeId, double radius) throws SQLException {
    // System.out.println("Density lookup for node:" + nodeId) ;
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_DENSITY);
    statement.setInt(1, nodeId);
    statement.setDouble(2, radius > 5400 ? 5400 : radius);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getRange(int nodeId, int density) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_RANGE);
    statement.setInt(1, nodeId);
    statement.setDouble(2, density);
    statement.setInt(3, nodeId);
    statement.setDouble(4, density);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLatestDepartureTimeWithSourceLocation(int source, int target, short routeId,
                                                           Set<Integer> businessDays, long earliestTime, long latestTime)
      throws SQLException {
    String sql = String.format(QUERY_GETLATESTDEPARTURETIME_EXACT_WITHLOCATION,
        preparePlaceHolders(businessDays.size()));
    PreparedStatement statement = connection.prepareStatement(sql);
    statement = connection.prepareStatement(sql);
    statement.setInt(1, target);
    statement.setInt(2, source);
    statement.setInt(3, routeId);
    statement.setLong(4, earliestTime);
    statement.setLong(5, latestTime);
    int idx = setValues(6, statement, businessDays.toArray());
    statement.setLong(idx, earliestTime);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLatestDepartureTimeWithSourceLocation(int source, int target, Set<Integer> businessDays,
                                                           long earliestTime, long latestTime) throws SQLException {
    String sql = String
        .format(QUERY_GETLATESTDEPARTURETIME_HOMO_WITHLOCATION, preparePlaceHolders(businessDays.size()));
    PreparedStatement statement = connection.prepareStatement(sql);
    statement = connection.prepareStatement(sql);
    statement.setInt(1, target);
    statement.setInt(2, source);
    statement.setLong(3, earliestTime);
    statement.setLong(4, latestTime);
    int idx = setValues(5, statement, businessDays.toArray());
    statement.setLong(idx, earliestTime);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLatestDepartureTimeWithSourceLocation(int target, Set<Integer> businessDays, long earliestTime,
                                                           long latestTime) throws SQLException {
    String sql = String.format(QUERY_GETLATESTDEPARTURETIME_HETERO_WITHLOCATION,
        preparePlaceHolders(businessDays.size()));
    PreparedStatement statement = connection.prepareStatement(sql);
    statement = connection.prepareStatement(sql);
    statement.setInt(1, target);
    statement.setLong(2, earliestTime);
    statement.setLong(3, latestTime);
    int idx = setValues(4, statement, businessDays.toArray());
    statement.setLong(idx, earliestTime);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLinksInLimitedRange(int id,GeoPoint cordinate, int nodeSize) throws SQLException {
    String pointString = "POINT( " + cordinate.getX() + " " + cordinate.getY() + ")";
    PreparedStatement pstmt = connection.prepareStatement(QUERY_GET_EDGES_LIMITED_SIZE);
    pstmt.setString(1, pointString);
    pstmt.setInt(2, config.getServerSRID());
    pstmt.setInt(3, id);
    pstmt.setInt(4, nodeSize);
    return new DBResult(pstmt, pstmt.executeQuery());
  }

  /**
   * <p>
   * Method subQuery
   * </p>
   * 
   * @param q
   * @param range
   * @param loaded
   * @param idx
   * @return
   */
  private String subQuery(ANode[] loaded, int idx, int srid) {
    if (idx == 0) {
      return "st_buffer(st_pointfromtext('POINT(" + loaded[idx].getCoordinate().getX() + " "
          + loaded[idx].getCoordinate().getY() + ")'," + srid + ")," + loaded[idx].getRadius() + ")";
    } else {
      return "st_union(" + "st_buffer(st_pointfromtext('POINT(" + loaded[idx].getCoordinate().getX() + " "
          + loaded[idx].getCoordinate().getY() + ")'," + srid + ")," + loaded[idx].getRadius() + "),"
          + subQuery(loaded, idx - 1, srid) + ")";
    }
  }

  private String subQuery2(GeoPoint q, double range, ANode[] loaded, int idx) {
    int srid = config.getServerSRID();

    if (idx == 0) {
      ANode loadedNode = loaded[idx - 1];
      return "st_difference(st_buffer(st_pointfromtext('POINT(" + q.getX() + " " + q.getY() + ")'," + srid + "),"
          + range + ")," + "st_buffer(st_pointfromtext('POINT(" + loadedNode.getCoordinate().getX() + " "
          + loadedNode.getCoordinate().getY() + ")'," + srid + ")," + loadedNode.getCoordinate() + "))";
    } else {
      return "st_intersection(" + subQuery2(q, range, loaded, idx - 1) + "," + "st_difference("
          + "st_buffer(st_pointfromtext('POINT(" + q.getX() + " " + q.getY() + ")'," + srid + ")," + range + "),"
          + "st_buffer(st_pointfromtext('POINT(" + loaded[idx - 1].getCoordinate().getX() + " "
          + loaded[idx - 1].getCoordinate().getY() + ")'," + srid + ")," + loaded[idx - 1].getCoordinate() + ")))";
    }
  }

  /*
  public static void main(String[] args) {

    PostgresQuery query = new PostgresQuery();

    GeoPoint q = new GeoPoint(680386.8879388, 5152170.83093339);
    double radius = 9000;

    GeoNode n1 = new GeoNode(1);
    n1.setLocation(new GeoPoint(688402.843016069, 5157054.21005474));
    n1.setLoadedRange(3000);

    GeoNode n2 = new GeoNode(2);
    n2.setLocation(new GeoPoint(674802.141078014, 5161560.18713918));
    n2.setLoadedRange(5860);

    GeoNode n3 = new GeoNode(3);
    n3.setLocation(new GeoPoint(680910.532411225, 5142908.46168964));
    n3.setLoadedRange(5000);

    GeoNode[] nodes = new GeoNode[3];
    nodes[0] = n1;
    nodes[1] = n2;
    nodes[2] = n3;

    String s = "st_difference(st_buffer(st_pointfromtext('POINT(" + q.getX() + " " + q.getY() + ")'," + 82344 + "),"
        + radius + ")," + query.subQuery(nodes, nodes.length - 1, 82344) + ")";
    System.out.println(s);

  }
  */
}
