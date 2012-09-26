/**
 * 
 */
package isochrones.mrnex.db;

import isochrones.db.AbstractOracleQuery;
import isochrones.db.DBResult;
import isochrones.mrnex.network.node.ANode;
import isochrones.network.GeoPoint;
import isochrones.network.GeoQueryPoint;
import isochrones.network.NWMode;
import isochrones.utils.Config;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

/**
 * <p>
 * The <code>OracleQuery</code> class
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
public class OracleQuery extends AbstractOracleQuery implements IQuery {
  
  int insertOrderSequence = 1;
  private String QUERY_GET_COORDINATES_FROM_Q, QUERY_GET_COORDINATES_FROM_NODE;
  private String QUERY_GET_CONTINUOUSEDGES_IN_QRANGE, QUERY_GET_EDGES_IN_QRANGE,
      QUERY_GET_CONTINUOUSEDGES_IN_NODERANGE, QUERY_GET_EDGES_IN_NODERANGE;

  public String QUERY_GET_COORDINATES, QUERY_GET_DENSITY, QUERY_GET_RANGE;
  public String QUERY_GET_CONTINUOUS_NODE, QUERY_GET_NODE;

  public String QUERY_GET_LINK, QUERY_GET_CONTINUOUS_LINK, QUERY_GETADJACENT_CONTINUOUS_LINKS,
      QUERY_GETADJACENT_DISCRETE_LINKS, QUERY_GETADJACENT_LINKS, QUERY_GETADJACENT_DISCRETE_LINKS_GEO;

  public String QUERY_LOG_LOADED_IER_NODE, QUERY_LOG_LOADED_IER_QPOINT;
  private String QUERY_E_NN;

  /**
   * <p>
   * Constructs a(n) <code>OracleQuery</code> object.
   * </p>
   * 
   * @param database
   */
  public OracleQuery(Config config) {
    super(config);
    initConstants();
  }

  @Override
  protected void initConstants() {
    super.initConstants();
    QUERY_GET_COORDINATES_FROM_Q = "SELECT T.THE_GEO.SDO_POINT.X X,T.THE_GEO.SDO_POINT.Y Y FROM "
        + "(SELECT SDO_LRS.CONVERT_TO_STD_GEOM("
        + "SDO_LRS.LOCATE_PT(SDO_LRS.CONVERT_TO_LRS_GEOM(GEOMETRY),?)) THE_GEO FROM " + config.getEdgeTable()
        + " WHERE ID=?) T";

    QUERY_GET_COORDINATES_FROM_NODE = "SELECT N.GEOMETRY.SDO_POINT.X X,N.GEOMETRY.SDO_POINT.Y Y FROM "
        + config.getVertexTable() + " N WHERE N.ID=?";

    QUERY_GET_CONTINUOUSEDGES_IN_QRANGE = "SELECT L.ID, L.SOURCE, L.TARGET, L.LENGTH,"
        + (config.isIncoming() ? "L.SOURCE_COUTDEGREE" : "L.TARGET_CINDEGREE") + " DEGREE FROM "
        + config.getEdgeTable() + " L, " 
        + config.getVertexTable() + " N " + " WHERE L.EDGE_MODE=" + NWMode.CONTINUOUS
        + " AND L.TARGET=N.ID AND L.LINK_TYPE='PED' AND SDO_WITHIN_DISTANCE(N.GEOMETRY, SDO_GEOMETRY(?,?),?)='TRUE'";

    QUERY_GET_EDGES_IN_QRANGE = "SELECT L.ID,L.SOURCE,L.SOURCE_MODE,L.TARGET,L.TARGET_MODE,L.LENGTH,L.EDGE_MODE,L.ROUTE_ID,"
        + (config.isIncoming() ? "L.SOURCE_OUTDEGREE" : "L.TARGET_INDEGREE") + " DEGREE FROM "
        + config.getEdgeTable() + " L, "
        + config.getVertexTable() + " N "
        + " WHERE L.TARGET=N.ID AND SDO_WITHIN_DISTANCE(N.GEOMETRY,SDO_GEOMETRY(?,?),?)='TRUE'";

    QUERY_GET_CONTINUOUSEDGES_IN_NODERANGE = "SELECT L.ID, L.SOURCE, L.TARGET, L.LENGTH," 
        + (config.isIncoming() ? "L.SOURCE_OUTDEGREE" : "L.TARGET_INDEGREE") + " DEGREE FROM "
        + config.getEdgeTable() + " L, " 
        + config.getVertexTable() + " N " + config.getVertexTable() + " NT " + "WHERE L.EDGE_MODE="
        + NWMode.CONTINUOUS + " AND N.ID=? AND L.TARGET=NT.ID AND SDO_WITHIN_DISTANCE(NT.GEOMETRY,N.GEOMETRY,?)='TRUE'";

    QUERY_GET_EDGES_IN_NODERANGE = "SELECT L.ID, L.SOURCE, L.SRC_MODE, L.TARGET,L.TARGET_MODE, L.LENGTH, L.EDGE_MODE, L.ROUTE_ID,"
        + (config.isIncoming() ? "L.SOURCE_OUTDEGREE" : "L.TARGET_INDEGREE") + " DEGREE FROM "
        + config.getEdgeTable() + " L, "
        + config.getVertexTable() + " N "
        + config.getVertexTable() + " NT "
        + "WHERE N.ID=? AND L."
        + (config.isIncoming() ?  "TARGET" : "SOURCE") +"=NT.ID AND SDO_WITHIN_DISTANCE(NT.GEOMETRY,N.GEOMETRY,?)='TRUE'";

    QUERY_GET_COORDINATES = "SELECT ID, ST_X(GEOMETRY) X, ST_Y(GEOMETRY) FROM " + config.getVertexTable()
        + " WHERE ID IN(%S)";

    QUERY_GET_CONTINUOUS_LINK = QueryConstants.getContinuousLinkQueryString(config);
    QUERY_GET_LINK = QueryConstants.getLinkQueryString(config);
    QUERY_GETADJACENT_CONTINUOUS_LINKS = QueryConstants.getAdjacentContinuousLinksQueryString(config);
    QUERY_GETADJACENT_LINKS = QueryConstants.getAdjacentLinksQueryString(config);
    QUERY_GETADJACENT_DISCRETE_LINKS = QueryConstants.getAdjacentDiscreteLinksQueryString(config);
    QUERY_GETADJACENT_DISCRETE_LINKS_GEO = QueryConstants.getAdjacentDiscreteLinksGeoQueryString(config);
    QUERY_GET_CONTINUOUS_NODE = QueryConstants.getContinuousNodeQueryString(config);
    QUERY_GET_NODE = QueryConstants.getNodeQueryString(config);

    // QUERY_GET_DENSITY = "SELECT * FROM ( SELECT DENSITY,E_DISTANCE FROM "
    // + config.getProperty("tbl.vertex.density") + " WHERE ID=? " +
    // "AND DISTANCE<=? ORDER BY DENSITY DESC) X WHERE ROWNUM=1";

    QUERY_GET_DENSITY = QueryConstants.getVertexDensity(config);
    QUERY_GET_RANGE = QueryConstants.getRange(config);
    
    QUERY_LOG_LOADED_IER_NODE = "INSERT INTO " + config.getProperty("tbl.log_circles")
        + "(NODE_ID,RADIUS,MAX_RADIUS,GEOMETRY,INSERT_SEQ) SELECT N.ID,?,?," 
        + "SDO_UTIL.CIRCLE_POLYGON (N.GEOMETRY.SDO_POINT.X,N.GEOMETRY.SDO_POINT.Y, ?,4),? " 
        + config.getVertexTable() + " N WHERE N.id=?";
    
    QUERY_LOG_LOADED_IER_QPOINT = "INSERT INTO " + config.getProperty("tbl.log_circles")
        + "(NODE_ID,RADIUS,MAX_RADIUS,GEOMETRY,INSERT_SEQ) SELECT null,?,?," 
        + "SDO_UTIL.CIRCLE_POLYGON (P.GEOMETRY.SDO_POINT.X,P.GEOMETRY.SDO_POINT.Y, ?,4),? FROM "
        + "(SELECT SDO_GEOMETRY(?,?) GEOMETRY FROM DUAL) P";
    
    QUERY_E_NN = "SELECT M.ENTITY_ID,M.EDGE_ID,M.OFFSET,P.GEOMETRY,SDO_NN_DISTANCE(1) FROM " + config.getProperty("tbl.entities.mapping")
        + " M," + config.getProperty("tbl.entities")
        + " P WHERE M.ENTITY_ID=P.ENTITY_ID AND SDO_NN(P.GEOMETRY,SDO_GEOMETRY(:1,:2),:3,1)='TRUE'";
  }

  @Override
  public DBResult getContinuousLink(int linkId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_CONTINUOUS_LINK);
    statement.setInt(1, linkId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLink(int linkId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_LINK);
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
    PreparedStatement statement = connection.prepareStatement(QUERY_GETADJACENT_DISCRETE_LINKS);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getAdjacentDiscreteLinksGeo(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GETADJACENT_DISCRETE_LINKS_GEO);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getContinuousNode(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_CONTINUOUS_NODE);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getNode(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_NODE);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getCoordinates(GeoQueryPoint q) throws SQLException {
    if (q.isNode()) {
      PreparedStatement statement = connection.prepareStatement(QUERY_GET_COORDINATES_FROM_NODE);
      statement.setInt(1, q.getId());
      return new DBResult(statement, statement.executeQuery());
    } else {
      PreparedStatement statement = connection.prepareStatement(QUERY_GET_COORDINATES_FROM_Q);
      statement.setDouble(1, q.getStartOffset());
      statement.setDouble(2, q.getId());
      return new DBResult(statement, statement.executeQuery());
    }
  }

  @Override
  public DBResult getContinuousLinksInRange(GeoQueryPoint q, double range) throws SQLException {
    String pointString = "POINT(" + q.getX() + " " + q.getY() + ")";
    String distanceStr = "DISTANCE=" + range + ",UNIT=M";
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_CONTINUOUSEDGES_IN_QRANGE);
    statement.setString(1, pointString);
    statement.setInt(2, config.getServerSRID());
    statement.setString(3, distanceStr);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLinksInRange(GeoQueryPoint q, double range) throws SQLException {
    String pointString = "POINT(" + q.getX() + " " + q.getY() + ")";
    String distanceStr = "DISTANCE=" + range + ",UNIT=M";
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_EDGES_IN_QRANGE);
    statement.setString(1, pointString);
    statement.setInt(2, config.getServerSRID());
    statement.setString(3, distanceStr);
    return new DBResult(statement, statement.executeQuery());
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
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_EDGES_IN_NODERANGE);
    statement.setDouble(1, p.getX());
    statement.setDouble(2, p.getY());
    statement.setDouble(3, range);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public void logLoadedIERCircle(int nodeId, double maxRadius, double loadedRange) {

    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(QUERY_LOG_LOADED_IER_NODE);
      statement.setDouble(1, loadedRange);
      statement.setDouble(2, maxRadius);
      statement.setDouble(3, loadedRange);
      statement.setInt(4, insertOrderSequence++);
      statement.setInt(5, nodeId);
      statement.executeUpdate();
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
  public void logLoadedIERCircle(GeoQueryPoint q, double maxRadius, double loadedRange){
    String pointStr = "POINT(" + q.getX() + " " + q.getY() + ")";
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(QUERY_LOG_LOADED_IER_QPOINT);
      statement.setDouble(1, loadedRange);
      statement.setDouble(2, maxRadius);
      statement.setDouble(3, loadedRange);
      statement.setInt(4, insertOrderSequence++);
      statement.setString(5, pointStr);
      statement.setInt(4, config.getServerSRID());
      
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
    return null;
  }

  @Override
  public DBResult getCoordinate(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_COORDINATES_FROM_NODE);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getDensity(int nodeId, double radius) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_DENSITY);
    statement.setInt(1, nodeId);
    statement.setDouble(2, radius);
    return new DBResult(statement, statement.executeQuery());
  }
  
  @Override
  public DBResult getRange(int nodeId, int density) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_RANGE);
    statement.setInt(1, nodeId);
    statement.setDouble(2, density);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getLatestDepartureTimeWithSourceLocation(int source, int target, short routeId,
                                                           Set<Integer> businessDays, long earliestTime, long latestTime)
      throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DBResult getLatestDepartureTimeWithSourceLocation(int source, int target, Set<Integer> businessDays,
                                                           long earliestTime, long latestTime) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public DBResult getLatestDepartureTimeWithSourceLocation(int target, Set<Integer> businessDays, long earliestTime,
                                                           long latestTime) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public DBResult getLinksInLimitedRange(int id, GeoPoint cordinate, int nodeSize) throws SQLException {
    PreparedStatement pstmt = connection.prepareStatement(QUERY_E_NN);
    pstmt.setString(1, "POINT(" + cordinate.getX() + " " + cordinate.getY() + ")");
    pstmt.setInt(2, config.getServerSRID());
    pstmt.setInt(3, id);
    pstmt.setInt(4, nodeSize);
    return new DBResult(pstmt, pstmt.executeQuery());
  }

  @Override
  public DBResult getLinksInRange(GeoPoint q, double range, Collection<ANode> loadedNodes,String[] aois) throws SQLException {
    return null;
  }
  
  @Override
  public void logLoadedIERArea(int id, String[] aios, double range) throws SQLException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public DBResult getCoordinate(int nodeId, int density) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

}
