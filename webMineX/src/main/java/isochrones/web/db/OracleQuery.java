/**
 * 
 */
package isochrones.web.db;

import isochrones.db.AbstractOracleQuery;
import isochrones.db.DBResult;
import isochrones.network.Offset;
import isochrones.web.config.Config;
import isochrones.web.coverage.IsoEdge;
import isochrones.web.geometry.AbstractLineString;
import isochrones.web.geometry.BBox;
import isochrones.web.geometry.ORALineString;
import isochrones.web.geometry.Point;
import isochrones.web.network.Schedule;
import isochrones.web.network.node.QueryPoint;
import isochrones.web.network.node.WebNode;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.jdbc.OraclePreparedStatement;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.ARRAY;
import oracle.sql.STRUCT;

/**
 * <p>
 * The <code>AbstractOracleQuery</code> class
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
public class OracleQuery extends AbstractOracleQuery implements IWebQuery {

  protected static final int ORA_ERROR_TABLE_NOT_EXIST = 942;
  protected static final int ORA_ERROR_INDEX_NOT_EXIST = 1418;

  private String QUERY_GET_NODEROUTES_ANNOTATION;
  private String QUERY_INSERT_VERTEX, QUERY_INSERT_VERTEXANNOTATION, QUERY_UPDATE_VERTICES;
  private String QUERY_GET_CONTINUOUS_LINK, QUERY_GET_LINK, QUERY_GET_CONTINUOUS_NODE, QUERY_GET_NODE,
      QUERY_GET_CONTINUOUS_ADJACENT_LINKS, QUERY_GET_ALL_ADJACENT_LINKS, QUERY_GET_VERTEX_ANNOTATION;

  /**
   * <p>
   * Constructs a(n) <code>OracleQuery</code> object.
   * </p>
   * 
   * @param config
   * @param initConstants
   */
  public OracleQuery(Config config, boolean initConstants) {
    super(config);
    if (initConstants) {
      initConstants();
    }
  }

  private Config getConfig() {
    return (Config) config;
  }

  protected void initConstants() {
    super.initConstants();
    QUERY_GET_CONTINUOUS_LINK = QueryConstants.getContinuousLinkQueryString(getConfig());
    QUERY_GET_LINK = QueryConstants.getLinkQueryString(getConfig());
    QUERY_GET_CONTINUOUS_NODE = QueryConstants.getContinuousNodeQueryString(getConfig());
    QUERY_GET_NODE = QueryConstants.getNodeQueryString(getConfig());
    QUERY_GET_CONTINUOUS_ADJACENT_LINKS = QueryConstants.getAdjacentContinuousLinksQueryString(getConfig());
    QUERY_GET_ALL_ADJACENT_LINKS = QueryConstants.getAdjacentLinksQueryString(getConfig());

    QUERY_INSERT_VERTEX = QueryConstants.getInsertVertexString(getConfig());
    QUERY_INSERT_VERTEXANNOTATION = QueryConstants.getInsertVertexAnnotationString(getConfig());
    QUERY_GET_NODEROUTES_ANNOTATION = QueryConstants.getNodeRoutesAnnotations(getConfig());

    QUERY_LATEST_DEPARTURE_TIME_HOMO = QueryConstants.getLatestDepartureTimeHomoString(getConfig());
    QUERY_LATEST_DEPARTURE_TIMES_HETERO = QueryConstants.getLatestDepartureTimeHeteroString(getConfig());

    QUERY_EARLIEST_ARRIVAL_TIME_HOMO = QueryConstants.getEarliestArrivalTimeHomoString(getConfig());
    QUERY_EARLIEST_ARRIVAL_TIMES_HETERO = QueryConstants.getEarliestArrivalTimeHeteroString(getConfig());

    QUERY_GET_VERTEX_ANNOTATION = "SELECT TO_TIMESTAMP(V.TIME_A,'SSSSS') ARRIVAL_TIME, TO_TIMESTAMP(V.TIME_D,'SSSSS') DEPARTURE_TIME, R.ROUTE_SHORT_NAME, R.ROUTE_TYPE FROM "
        + getConfig().getDestinationVertexAnnotatedTableEntry().getTableName()
        + " V, "
        + getConfig().getRouteTable()
        + " R WHERE V.ROUTE_ID=R.ROUTE_ID";

    QUERY_UPDATE_VERTICES = "UPDATE " + getConfig().getDestinationVertexTableEntry().getTableName()
        + " SET T_TYPE=(SELECT R.ROUTE_TYPE FROM " + getConfig().getRouteTable() + " R WHERE R.ROUTE_ID = "
        + getConfig().getDestinationVertexTableEntry().getTableName() + ".ROUTE_ID " + "AND "
        + getConfig().getDestinationVertexTableEntry().getTableName() + ".ROUTE_ID>=0)";

  }

  @Override
  public isochrones.web.config.Config getWebConfig() {
    return (isochrones.web.config.Config) config;
  }

  @Override
  public DBResult projectOnLinks(QueryPoint qPoint) throws SQLException {
    String point = "POINT(" + qPoint.getX() + " " + qPoint.getY() + ")";
    StringBuilder b = new StringBuilder();

    b.append("SELECT L.ID, ");
    b.append("SDO_LRS.GET_MEASURE(").append("SDO_LRS.PROJECT_PT(SDO_LRS.CONVERT_TO_LRS_GEOM(");
    b.append("L.GEOMETRY), POI.GEOMETRY)) OFFSET ");
    b.append("FROM ").append(config.getProperty("tbl.links")).append(" L, (");
    if (config.getClientSRID() == config.getServerSRID()) {
      b.append("SELECT SDO_GEOMETRY(:1,:2) ");
    } else {
      b.append("SELECT SDO_CS.TRANSFORM(SDO_GEOMETRY(:1,:2),:3) ");
    }
    b.append("GEOMETRY FROM DUAL) POI ");
    b.append(" WHERE L.EDGE_MODE=0 AND SDO_NN(L.GEOMETRY,POI.GEOMETRY,'SDO_NUM_RES=2', 1 )='TRUE'");

    PreparedStatement statement = connection.prepareStatement(b.toString());
    statement.setString(1, point);
    statement.setInt(2, config.getClientSRID());
    if (config.getClientSRID() != config.getServerSRID()) {
      statement.setInt(3, config.getServerSRID());
    }
    return new DBResult(statement, statement.executeQuery());
  }
  //@Override
  public void _storeVertices(Set<WebNode> storableNodes) {
    /*
     * PreparedStatement insertVerticesStatement = null, insertVertexAnnotationStatement = null; for (WebNode node :
     * storableNodes) { try { insertVerticesStatement = connection.prepareStatement(QUERY_INSERT_VERTEX);
     * insertVertexAnnotationStatement = connection.prepareStatement(QUERY_INSERT_VERTEXANNOTATION); if
     * (getWebConfig().isExpirationMode() || getWebConfig().enableAreaCalculation() || (node.isClosed() &&
     * node.containsRoutes())) { if (node.containsRoutes()) { for (Integer routeId : node.getRouteSchedules().keySet())
     * { Schedule schedule = node.getRouteSchedules().get(routeId); insertVertexAnnotationStatement.setInt(1,
     * node.getId()); // node id insertVertexAnnotationStatement.setInt(2, routeId);
     * insertVertexAnnotationStatement.setLong(3, schedule.getArrivalTime()); insertVertexAnnotationStatement.setLong(4,
     * schedule.getDepartureTime()); insertVertexAnnotationStatement.executeUpdate(); connection.commit(); } }
     * insertVerticesStatement.setInt(1, node.getId()); insertVerticesStatement.setDouble(2, node.getDistance());
     * insertVerticesStatement.setDouble(3, (node.getCheapestReachedRouteId() == WebNode.Value.NOT_SET) ? -1 :
     * node.getCheapestReachedRouteId()); insertVerticesStatement.setString(4, node.getState().toString());
     * insertVerticesStatement.setInt(5, node.getId()); insertVerticesStatement.executeUpdate(); connection.commit(); }
     * } catch (SQLException e) { LOGGER.severe(e.getMessage()); } finally { try { if (insertVerticesStatement != null)
     * insertVerticesStatement.close(); if (insertVertexAnnotationStatement != null)
     * insertVertexAnnotationStatement.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage());
     * e.printStackTrace(); } } }
     */
    PreparedStatement insertVerticesStatement = null, insertVertexAnnotationStatement = null;
    try {
      insertVerticesStatement = connection.prepareStatement(QUERY_INSERT_VERTEX);
      ((OraclePreparedStatement) insertVerticesStatement).setExecuteBatch(config.getBatchSize());
      insertVertexAnnotationStatement = connection.prepareStatement(QUERY_INSERT_VERTEXANNOTATION);
      ((OraclePreparedStatement) insertVertexAnnotationStatement).setExecuteBatch(config.getBatchSize());

      Set<Integer> storedNodes = new HashSet<Integer>();

      for (WebNode node : storableNodes) {
        if (storedNodes.contains(node.getId())) {
          System.err.println("Node" + node + " already added.");
        } else {
          storedNodes.add(node.getId());
        }
        if (getWebConfig().isExpirationMode() || getWebConfig().enableAreaCalculation()
            || (node.isClosed() && node.containsRoutes())) {
          if (node.containsRoutes()) {
            for (Integer routeId : node.getRouteSchedules().keySet()) {
              Schedule schedule = node.getRouteSchedules().get(routeId);
              insertVertexAnnotationStatement.setInt(1, node.getId()); // node id
              insertVertexAnnotationStatement.setInt(2, routeId);
              insertVertexAnnotationStatement.setLong(3, schedule.getArrivalTime());
              insertVertexAnnotationStatement.setLong(4, schedule.getDepartureTime());
              if (insertVertexAnnotationStatement.executeUpdate() == config.getBatchSize()) {
                connection.commit();
              }
            }
          }
          insertVerticesStatement.setInt(1, node.getId());
          insertVerticesStatement.setDouble(2, node.getDistance());
          insertVerticesStatement.setDouble(3,
              (node.getCheapestReachedRouteId() == WebNode.Value.NOT_SET) ? -1 : node.getCheapestReachedRouteId());
          insertVerticesStatement.setString(4, node.getState().toString());
          insertVerticesStatement.setInt(5, node.getId());
          if (insertVerticesStatement.executeUpdate() == config.getBatchSize()) {
            connection.commit();
          }
        }
      }
      connection.commit();
    } catch (SQLException e) {
      LOGGER.severe(e.getMessage());
    } finally {
      try {
        if (insertVerticesStatement != null)
          insertVerticesStatement.close();
        if (insertVertexAnnotationStatement != null)
          insertVertexAnnotationStatement.close();
      } catch (SQLException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Override
  public void storeVertices(Set<WebNode> storableNodes) {
    PreparedStatement insertVerticesStatement = null, insertVertexAnnotationStatement = null;
    try {
      insertVerticesStatement = connection.prepareStatement(QUERY_INSERT_VERTEX);
      ((OraclePreparedStatement) insertVerticesStatement).setExecuteBatch(config.getBatchSize());
      insertVertexAnnotationStatement = connection.prepareStatement(QUERY_INSERT_VERTEXANNOTATION);
      ((OraclePreparedStatement) insertVertexAnnotationStatement).setExecuteBatch(config.getBatchSize());

      Set<Integer> storedNodes = new HashSet<Integer>();

      for (WebNode node : storableNodes) {
        if (storedNodes.contains(node.getId())) {
          System.err.println("Node" + node + " already added.");
        } else {
          storedNodes.add(node.getId());
        }
        if (getWebConfig().isExpirationMode() || getWebConfig().enableAreaCalculation()
            || (node.isClosed() && node.containsRoutes())) {
          if (node.containsRoutes()) {
            for (Integer routeId : node.getRouteSchedules().keySet()) {
              Schedule schedule = node.getRouteSchedules().get(routeId);
              insertVertexAnnotationStatement.setInt(1, node.getId()); // node id
              insertVertexAnnotationStatement.setInt(2, routeId);
              insertVertexAnnotationStatement.setLong(3, schedule.getArrivalTime());
              insertVertexAnnotationStatement.setLong(4, schedule.getDepartureTime());
              insertVertexAnnotationStatement.addBatch();
            }
          }
          insertVerticesStatement.setInt(1, node.getId());
          insertVerticesStatement.setDouble(2, node.getDistance());
          insertVerticesStatement.setDouble(3,
              (node.getCheapestReachedRouteId() == WebNode.Value.NOT_SET) ? -1 : node.getCheapestReachedRouteId());
          insertVerticesStatement.setString(4, node.getState().toString());
          insertVerticesStatement.setInt(5, node.getId());
          insertVerticesStatement.addBatch();
        }
      }
      connection.commit();
    } catch (SQLException e) {
      LOGGER.severe(e.getMessage());
    } finally {
      try {
        if (insertVerticesStatement != null)
          insertVerticesStatement.close();
        if (insertVertexAnnotationStatement != null)
          insertVertexAnnotationStatement.close();
      } catch (SQLException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Override
  public DBResult getNodeAnnotation(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_NODEROUTES_ANNOTATION);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public BBox getIsochroneBoundingBox() {
    String q = " SELECT SDO_CS.TRANSFORM(SDO_AGGR_MBR(GEOMETRY)," + config.getClientSRID() + ") FROM "
        + config.getDestinationEdgeTableEntry().getTableName();
    // + " WHERE CLIENT_ID=:1 AND VISIBLE=1";
    PreparedStatement statement = null;
    long minX, minY, maxX, maxY;
    minX = Long.MAX_VALUE;
    minY = minX;
    maxX = Long.MIN_VALUE;
    maxY = maxX;

    try {
      statement = connection.prepareStatement(q);
      // statement.setInt(1, config.getClientSRID());
      ResultSet rSet = statement.executeQuery();
      if (rSet.next()) {
        STRUCT s = (STRUCT) rSet.getObject(1);
        oracle.sql.ARRAY arr = (ARRAY) s.getAttributes()[4];
        double[] doubleArray = arr.getDoubleArray();
        for (int i = 0; i < doubleArray.length; i++) {
          long d = Math.round(doubleArray[i]);
          if (i % 2 == 0) { // even
            minX = Math.min(minX, d);
            maxX = Math.max(maxX, d);
          } else { // odd
            minY = Math.min(minY, d);
            maxY = Math.max(maxY, d);
          }
        }
      }
      return new BBox(minX, minY, maxX, maxY);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
      }
    }
    return null;
  }

  @Override
  public Point transform(Point p) {
    if(config.getClientSRID()==config.getServerSRID()) return p;
    
    String point = "POINT(" + p.getX() + " " + p.getY() + ")";
    PreparedStatement statement = null;
    ResultSet rSet = null;

    try {

      statement = connection
          .prepareStatement("SELECT P.GEO.SDO_POINT.X X, P.GEO.SDO_POINT.Y Y FROM (SELECT SDO_CS.TRANSFORM(SDO_GEOMETRY(:1,:2),:3) GEO FROM DUAL) P");
      statement.setString(1, point);
      statement.setInt(2, config.getServerSRID());
      statement.setInt(3, config.getClientSRID());
      rSet = statement.executeQuery();
      if (rSet.next()) {
        return new Point(rSet.getDouble("X"), rSet.getDouble("Y"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (rSet != null)
          rSet.close();
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return null;
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
  public DBResult getAdjacentContinuousLinks(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_CONTINUOUS_ADJACENT_LINKS);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getAdjacentLinks(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_ALL_ADJACENT_LINKS);
    statement.setInt(1, nodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public int getTotalNumberOfInhabitants() {
    int totalInhabitants = 0;
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("SELECT SUM(INHABITANTS) FROM " + config.getProperty("tbl.building"));
      ResultSet rSet = statement.executeQuery();
      if (rSet.next()) {
        totalInhabitants = rSet.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
      }
    }
    return totalInhabitants;
  }

  @Override
  public Collection<IsoEdge> getIsochoneEdges(int dMax, double speed, boolean avoidNonPartialDuplicates) {
    Collection<IsoEdge> isoEdges = new ArrayList<IsoEdge>();
    Map<String, IsoEdge> addedLinks = new HashMap<String, IsoEdge>();
    PreparedStatement statement = null;
    DBResult dbResult = null;
    try {
      statement = connection.prepareStatement(QueryConstants.getIsoEdges(getConfig()));
      dbResult = new DBResult(statement, statement.executeQuery());
      ResultSet resultSet = dbResult.getResultSet();
      while (resultSet.next()) {
        int edgeId = resultSet.getInt("ID");
        int sourceId = resultSet.getInt("SOURCE");
        double adjNodeDistance = resultSet.getDouble("DISTANCE");
        double adjNodeOffset = resultSet.getDouble("OFFSET");
        double length = resultSet.getDouble("LENGTH");
        int targetId = resultSet.getInt("TARGET");
        JGeometry oraGeometry = JGeometry.load((STRUCT) resultSet.getObject("GEOMETRY"));
        AbstractLineString geometry = null;
        if (oraGeometry.getType() == JGeometry.GTYPE_CURVE) {
          geometry = new ORALineString(oraGeometry);
        } else {
          // points are not added
          continue;
        }
        IsoEdge edge;
        if (config.isIncoming()) {
          edge = new IsoEdge(edgeId, sourceId, targetId, length, adjNodeDistance, new Offset(adjNodeOffset, length),
                             geometry);
          edge.setRemainingDistance(adjNodeOffset > 0 ? 0 : dMax - edge.getAdjNodeDistance() * speed);
        } else {
          edge = new IsoEdge(edgeId, sourceId, targetId, length, adjNodeDistance, new Offset(0, adjNodeOffset),
                             geometry);
          edge.setRemainingDistance(adjNodeOffset < length ? 0 : dMax - edge.getAdjNodeDistance() * speed);
        }

        if (avoidNonPartialDuplicates) {
          if (addedLinks.containsKey(edge.getInvertedIdentifier())) {
            IsoEdge invertedEdge = addedLinks.get(edge.getInvertedIdentifier());
            if (invertedEdge.isPartial()) { // inverted is partial
              addedLinks.put(edge.getIdentifier(), edge);
              isoEdges.add(edge);
            }
          } else {
            addedLinks.put(edge.getIdentifier(), edge);
            isoEdges.add(edge);
          }
        } else {
          isoEdges.add(edge);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    return isoEdges;
  }

  @Override
  public void createBuffer(Collection<IsoEdge> edges, double bufferSize, boolean incoming) {
    String sqlNormalBuffer = "INSERT INTO " + getConfig().getDestinationAreaBufferTableEntry().getTableName()
        + " (ID, GEOMETRY) (SELECT ID, SDO_GEOM.SDO_ARC_DENSIFY(SDO_GEOM.SDO_BUFFER(GEOMETRY,:1, "
        + config.getProperty("spatial.buffer.tolerance") + "), 0.05, 'arc_tolerance=1 unit=m') FROM "
        + config.getEdgeTable() + " WHERE ID=:2 )";

    String sqlReducedBuffer = "INSERT INTO "
        + getConfig().getDestinationAreaBufferTableEntry().getTableName()
        + " (ID, GEOMETRY) (SELECT ID, SDO_GEOM.SDO_ARC_DENSIFY(SDO_GEOM.SDO_BUFFER(SDO_LRS.CLIP_GEOM_SEGMENT(SDO_LRS.CONVERT_TO_LRS_GEOM(GEOMETRY),:1,:2),:3,"
        + config.getProperty("spatial.buffer.tolerance") + "), 0.05, 'arc_tolerance=1 unit=m') FROM "
        + config.getEdgeTable() + " WHERE ID=:4)";

    PreparedStatement statement = null, stmtReduced = null;
    try {
      statement = connection.prepareStatement(sqlNormalBuffer);
      ((OraclePreparedStatement) statement).setExecuteBatch(config.getBatchSize());

      stmtReduced = connection.prepareStatement(sqlReducedBuffer);
      ((OraclePreparedStatement) stmtReduced).setExecuteBatch(config.getBatchSize());

      for (IsoEdge isoEdge : edges) {
        if (isoEdge.getRemainingDistance() >= bufferSize) {
          // create normal buffer
          statement.setDouble(1, bufferSize);
          statement.setInt(2, isoEdge.getId());
          if (statement.executeUpdate() == config.getBatchSize()) {
            connection.commit();
          }
        } else if (incoming) {
          if (bufferSize - isoEdge.getRemainingDistance() < isoEdge.getLength() - isoEdge.getOffset().getStartOffset()) {
            // only creating radius if the delta is greater than the length of the edge
            double startOffset = bufferSize - isoEdge.getRemainingDistance() + isoEdge.getOffset().getStartOffset();
            // stmtReduced.setDouble(1, isoEdge.getLength() - startOffset);
            stmtReduced.setDouble(1, startOffset);
            stmtReduced.setDouble(2, isoEdge.getOffset().getEndOffset());
            stmtReduced.setDouble(3, bufferSize);
            stmtReduced.setInt(4, isoEdge.getId());
            if (stmtReduced.executeUpdate() == config.getBatchSize()) {
              connection.commit();
            }
          }
        } else {
          if (bufferSize - isoEdge.getRemainingDistance() < isoEdge.getOffset().getEndOffset()) {
            double endOffset = isoEdge.getOffset().getEndOffset() - (bufferSize - isoEdge.getRemainingDistance());
            stmtReduced.setDouble(1, isoEdge.getOffset().getStartOffset());
            stmtReduced.setDouble(2, endOffset);
            stmtReduced.setDouble(3, bufferSize);
            stmtReduced.setInt(4, isoEdge.getId());
            if (stmtReduced.executeUpdate() == config.getBatchSize()) {
              connection.commit();
            }
          }
        }
      }
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (statement != null)
          statement.close();
        if (stmtReduced != null)
          stmtReduced.close();
      } catch (SQLException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Override
  public void storeArea(int areaId, List<Point> points, double bufferSize) {

    double[] ords = new double[points.size() * 2];
    int i = 0;
    // int cnt = 0;
    for (Point p : points) {
      // System.out.println(cnt++ + "\t" + p);
      ords[i++] = p.getX();
      ords[i++] = p.getY();
    }
    if (ords.length < 5 && ords.length % 2 > 0) {
      LOGGER.warning("Not enough values when creating polygon with id: " + areaId);
    } else {
      LOGGER.info("Insert polygon with # of ordinates: " + ords.length);
      JGeometry polygon = JGeometry.createLinearPolygon(ords, 2,
          Integer.valueOf(config.getProperty("sql.spatial.srid")));
      StringBuilder b1 = new StringBuilder();
      b1.append("INSERT INTO ").append(getConfig().getDestinationAreaBufferTableEntry().getTableName());
      b1.append("(\"ID\",\"GEOMETRY\") VALUES(:1,");
      b1.append("SDO_GEOM.SDO_ARC_DENSIFY(SDO_GEOM.SDO_BUFFER(:2,:3,");
      b1.append(config.getProperty("spatial.buffer.tolerance")).append(")");
      b1.append(", 0.000001, 'arc_tolerance=1')");
      b1.append(")");
      PreparedStatement statement = null;
      try {
        statement = connection.prepareStatement(b1.toString());
        // convert JGeometry instance to DB STRUCT
        STRUCT obj = JGeometry.store(polygon, connection);
        statement.setInt(1, areaId);
        statement.setObject(2, obj);
        statement.setObject(3, bufferSize);
        statement.executeUpdate();
        connection.commit();
      } catch (SQLException e) {
        e.printStackTrace();
        StringBuilder b = new StringBuilder();
        for (double ordinate : ords) {
          b.append(ordinate).append(",");
        }
        LOGGER.severe("Problems when inserting the points:" + b.toString() + " of polygon with id: " + areaId);
      } finally {
        try {
          if (statement != null)
            statement.close();
        } catch (SQLException e) {
          LOGGER.severe(e.getMessage());
          e.printStackTrace();
        }
      }
    }

  }

  @Override
  public void storeAreaFromEdges(int areaId, List<IsoEdge> borderEdges, double bufferSize) {
    // TODO Implement me!!
  }

  @Override
  public DBResult edgesInArea(int areaId) throws SQLException {
    StringBuilder b1 = new StringBuilder();
    b1.append("SELECT L.ID FROM ");
    b1.append(getConfig().getDestinationEdgeTableEntry().getTableName()).append(" L, ");
    b1.append(getConfig().getDestinationAreaBufferTableEntry().getTableName()).append(" A ");
    b1.append("WHERE A.ID=:1 AND SDO_INSIDE(L.GEOMETRY,A.GEOMETRY)='TRUE'");
    PreparedStatement statement = connection.prepareStatement(b1.toString());
    statement.setInt(1, areaId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public int reachedInhabitants() {
    int reachedInhabitants = 0;
    PreparedStatement statement = null;
    StringBuilder b = new StringBuilder();
    b.append("SELECT SUM(INHABITANTS) FROM (SELECT DISTINCT B.ID, B.INHABITANTS FROM ");
    b.append(config.getProperty("tbl.building")).append(" B, ");
    b.append(getConfig().getDestinationAreaBufferTableEntry().getTableName()).append(" A ");
    b.append("WHERE SDO_INSIDE(B.GEOMETRY, A.GEOMETRY)='TRUE')");
    try {
      statement = connection.prepareStatement(b.toString());
      ResultSet rSet = statement.executeQuery();
      if (rSet.next()) {
        reachedInhabitants = rSet.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
      }
    }
    return reachedInhabitants;
  }

  @Override
  public int totalInhabitants() {
    int totalInhabitants = 0;
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("SELECT SUM(INHABITANTS) FROM " + config.getProperty("tbl.building"));
      ResultSet rSet = statement.executeQuery();
      if (rSet.next()) {
        totalInhabitants = rSet.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
      }
    }
    return totalInhabitants;
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
  public void updateVertexTable() {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement(QUERY_UPDATE_VERTICES);
      statement.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (statement != null)
          statement.close();
      } catch (SQLException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Override
  public DBResult getAnnotation(int vertexId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_VERTEX_ANNOTATION);
    statement.setInt(1, vertexId);
    return new DBResult(statement, statement.executeQuery());
  }

}
