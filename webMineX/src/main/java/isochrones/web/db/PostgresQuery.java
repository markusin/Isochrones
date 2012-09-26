/**
 * 
 */
package isochrones.web.db;

import isochrones.db.AbstractPostgresQuery;
import isochrones.db.DBResult;
import isochrones.network.NWMode;
import isochrones.network.Offset;
import isochrones.web.config.Config;
import isochrones.web.coverage.IsoEdge;
import isochrones.web.geometry.AbstractLineString;
import isochrones.web.geometry.BBox;
import isochrones.web.geometry.PGLineString;
import isochrones.web.geometry.Point;
import isochrones.web.network.Schedule;
import isochrones.web.network.node.QueryPoint;
import isochrones.web.network.node.WebNode;
import isochrones.web.utils.PGUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.postgis.Geometry;
import org.postgis.LineString;
import org.postgis.MultiLineString;
import org.postgis.MultiPoint;
import org.postgis.PGgeometry;
import org.postgresql.PGStatement;

/**
 * <p>
 * The <code>AbstractPostgresQuery</code> class
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
public class PostgresQuery extends AbstractPostgresQuery implements IWebQuery {

  private String QUERY_CREATE_NORMAL_BUFFER, QUERY_CREATE_REDUCED_BUFFER;
  private String QUERY_GET_NODEROUTES_ANNOTATION;
  private String QUERY_INSERT_VERTEX, QUERY_INSERT_VERTEXANNOTATION, QUERY_UPDATE_VERTICES;

  private String QUERY_GET_CONTINUOUS_LINK, QUERY_GET_LINK, QUERY_GET_CONTINUOUS_NODE, QUERY_GET_NODE,
      QUERY_GET_CONTINUOUS_ADJACENT_LINKS, QUERY_GET_ALL_ADJACENT_LINKS, QUERY_GET_VERTEX_ANNOTATION;

  /**
   * <p>
   * Constructs a(n) <code>PostgresQuery</code> object.
   * </p>
   * 
   * @param config
   * @param initConstants
   */
  public PostgresQuery(Config config, boolean initConstants) {
    super(config);
    try {
      ((org.postgresql.PGConnection) config.getConnection()).addDataType("geometry",
          Class.forName("org.postgis.PGgeometry"));
    } catch (SQLException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
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

    QUERY_CREATE_NORMAL_BUFFER = "INSERT INTO " + getConfig().getDestinationAreaBufferTableEntry().getTableName()
        + " (ID, GEOMETRY) (SELECT ID, ST_Buffer(GEOMETRY,?,'quad_segs=8')  FROM " + config.getEdgeTable()
        + " WHERE ID=?)";

    QUERY_CREATE_REDUCED_BUFFER = "INSERT INTO " + getConfig().getDestinationAreaBufferTableEntry().getTableName()
        + " (ID, GEOMETRY) (SELECT ID, ST_Buffer(ST_LINE_SUBSTRING(GEOMETRY,?,?),?,'quad_segs=8') FROM "
        + config.getEdgeTable() + " WHERE ID=?)";

    QUERY_GET_VERTEX_ANNOTATION = "SELECT TO_TIMESTAMP(V.TIME_A,'SSSSS') ARRIVAL_TIME, TO_TIMESTAMP(V.TIME_D,'SSSSS') DEPARTURE_TIME, R.ROUTE_SHORT_NAME, R.ROUTE_TYPE FROM "
        + getConfig().getDestinationVertexAnnotatedTableEntry().getTableName()
        + " V, "
        + getConfig().getRouteTable()
        + " R WHERE V.ROUTE_ID=R.ROUTE_ID";

    QUERY_UPDATE_VERTICES = "UPDATE " + getConfig().getDestinationVertexTableEntry().getTableName()
        + " SET \"T_TYPE\"=R.ROUTE_TYPE FROM " + getConfig().getRouteTable() + " R WHERE R.ROUTE_ID = "
        + getConfig().getDestinationVertexTableEntry().getTableName() + ".\"ROUTE_ID\" " + "AND "
        + getConfig().getDestinationVertexTableEntry().getTableName() + ".\"ROUTE_ID\">=0";
  }

  @Override
  public isochrones.web.config.Config getWebConfig() {
    return (isochrones.web.config.Config) config;
  }

  @Override
  public DBResult projectOnLinks(QueryPoint qPoint) throws SQLException {
    String point = "POINT(" + qPoint.getX() + " " + qPoint.getY() + ")";
    StringBuilder b1 = new StringBuilder();
    b1.append("SELECT L.ID, ST_Line_Locate_Point(L.EDGE_GEO,L.POINT_GEO)*L.LENGTH AS OFFSET ");
    b1.append("FROM (SELECT E.ID, E.GEOMETRY EDGE_GEO, E.LENGTH, P.GEOMETRY POINT_GEO FROM ");
    b1.append(config.getEdgeTable()).append(" E, ");
    b1.append(" (SELECT ");
    if (config.getClientSRID() == config.getServerSRID()) {
      b1.append("ST_PointFromText(?,?) ");
    } else {
      b1.append("ST_Transform(ST_PointFromText(?,?),?)");
    }
    b1.append(" GEOMETRY) P");
    b1.append(" WHERE E.EDGE_MODE=").append(NWMode.CONTINUOUS).append(" AND st_dwithin(E.GEOMETRY,P.GEOMETRY, ?)"); // tolerance
    b1.append(" ORDER BY st_distance(E.GEOMETRY,P.GEOMETRY) LIMIT 2) L");

    PreparedStatement statement = connection.prepareStatement(b1.toString());
    statement.setString(1, point);
    statement.setInt(2, config.getClientSRID());
    if (config.getClientSRID() != config.getServerSRID()) {
      statement.setInt(3, config.getServerSRID());
      statement.setInt(4, Integer.parseInt(config.getProperty("par.distance_tolerance")));
    } else {
      statement.setInt(3, Integer.parseInt(config.getProperty("par.distance_tolerance")));
    }
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public void storeVertices(Set<WebNode> storableNodes) {
    PreparedStatement insertVerticesStatement = null, insertVertexAnnotationStatement = null;
    try {
      insertVerticesStatement = connection.prepareStatement(QUERY_INSERT_VERTEX);
      ((PGStatement) insertVerticesStatement).setPrepareThreshold(config.getBatchSize());
      insertVertexAnnotationStatement = connection.prepareStatement(QUERY_INSERT_VERTEXANNOTATION);
      ((PGStatement) insertVertexAnnotationStatement).setPrepareThreshold(config.getBatchSize());
      for (WebNode node : storableNodes) {
        if (getWebConfig().isExpirationMode() || getWebConfig().enableAreaCalculation()
            || (node.isClosed() && node.containsRoutes())) {
          if (node.containsRoutes()) {
            for (Integer routeId : node.getRouteSchedules().keySet()) {
              Schedule schedule = node.getRouteSchedules().get(routeId);
              insertVertexAnnotationStatement.setInt(1, node.getId()); // node id
              insertVertexAnnotationStatement.setInt(2, routeId);
              insertVertexAnnotationStatement.setLong(3, schedule.getArrivalTime());
              insertVertexAnnotationStatement.setLong(4, schedule.getDepartureTime());
              insertVertexAnnotationStatement.executeUpdate();
            }
          }
          insertVerticesStatement.setInt(1, node.getId());
          insertVerticesStatement.setDouble(2, node.getDistance());
          insertVerticesStatement.setDouble(3,
              (node.getCheapestReachedRouteId() == WebNode.Value.NOT_SET) ? -1 : node.getCheapestReachedRouteId());
          insertVerticesStatement.setString(4, node.getState().toString());
          insertVerticesStatement.setInt(5, node.getId());
          insertVerticesStatement.executeUpdate();
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
    String q = "select ST_XMin(mbr.geo) min_x, ST_yMin(mbr.geo) min_y, ST_XMax(mbr.geo) max_x, ST_yMax(mbr.geo) max_y from "
        + "(SELECT st_transform(ST_SetSRID(st_extent(\"GEOMETRY\"),?),?) GEO FROM "
        + config.getDestinationEdgeTableEntry().getTableName() + ") mbr";

    PreparedStatement statement = null;
    long minX, minY, maxX, maxY;
    minX = Long.MAX_VALUE;
    minY = minX;
    maxX = Long.MIN_VALUE;
    maxY = maxX;

    try {
      statement = connection.prepareStatement(q);
      statement.setInt(1, config.getServerSRID());
      statement.setInt(2, config.getClientSRID());
      ResultSet rSet = statement.executeQuery();
      if (rSet.next()) {
        minX = Math.round(rSet.getDouble("min_x"));
        minY = Math.round(rSet.getDouble("min_y"));
        maxX = Math.round(rSet.getDouble("max_x"));
        maxY = Math.round(rSet.getDouble("max_y"));
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
          .prepareStatement("SELECT ST_X(P.GEO) X, ST_Y(P.GEO) Y FROM (SELECT ST_Transform(ST_PointFromText(?,?),?) GEO ) P");
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
        PGgeometry pgGeometry = (PGgeometry) resultSet.getObject("GEOMETRY");
        AbstractLineString geometry = null;
        if (pgGeometry.getGeoType() == Geometry.LINESTRING) {
          geometry = new PGLineString((LineString) pgGeometry.getGeometry());
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

    PreparedStatement statement = null, stmtReduced = null;
    try {
      statement = connection.prepareStatement(QUERY_CREATE_NORMAL_BUFFER);
      // ((PGStatement) statement).setPrepareThreshold(config.getBatchSize());

      stmtReduced = connection.prepareStatement(QUERY_CREATE_REDUCED_BUFFER);
      // ((PGStatement) stmtReduced).setPrepareThreshold(config.getBatchSize());

      for (IsoEdge isoEdge : edges) {
        if (isoEdge.getRemainingDistance() >= bufferSize) {
          // create normal buffer
          statement.setDouble(1, bufferSize);
          statement.setInt(2, isoEdge.getId());
          statement.executeUpdate();
        } else {
          if (incoming) {
            if (bufferSize - isoEdge.getRemainingDistance() < isoEdge.getLength()
                - isoEdge.getOffset().getStartOffset()) {
              // only creating radius if the delta is greater than the length of the edge
              double startOffset = bufferSize - isoEdge.getRemainingDistance() + isoEdge.getOffset().getStartOffset();
              // stmtReduced.setDouble(1, isoEdge.getLength() - startOffset);
              stmtReduced.setDouble(1, startOffset / isoEdge.getLength());
              stmtReduced.setDouble(2, isoEdge.getOffset().getEndOffset() / isoEdge.getLength());
              stmtReduced.setDouble(3, bufferSize);
              stmtReduced.setInt(4, isoEdge.getId());
              stmtReduced.executeUpdate();
            } else {
              if (bufferSize - isoEdge.getRemainingDistance() < isoEdge.getOffset().getEndOffset()) {
                double endOffset = isoEdge.getOffset().getEndOffset() - (bufferSize - isoEdge.getRemainingDistance());
                stmtReduced.setDouble(1, isoEdge.getOffset().getStartOffset());
                stmtReduced.setDouble(2, endOffset / isoEdge.getLength());
                stmtReduced.setDouble(2, bufferSize);
                stmtReduced.setInt(3, isoEdge.getId());
                stmtReduced.executeUpdate();
              }
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
    LOGGER.info("Insert polygon with # of ordinates: " + points.size());

    String geometryString;
    /*
     * if(points.size()<4) { LineString lineString = new LineString(PGUtil.asPGPoints(points)); geometryString =
     * lineString.getTypeString()+ lineString.getValue(); } else { LinearRing linearRing = new
     * LinearRing(PGUtil.asPGPoints(points)); Polygon polygon = new Polygon(new LinearRing[] {linearRing});
     * //MultiPolygon mpoly = new MultiPolygon(new Polygon[] {polygon}); //geometryString = mpoly.getTypeString()+
     * mpoly.getValue(); geometryString = polygon.getTypeString()+ polygon.getValue(); }
     */

    // org.postgis.Point[] pgPoints = PGUtil.asPGPoints(points);

    MultiPoint mpoints = new MultiPoint(PGUtil.asPGPoints(points));
    geometryString = mpoints.getTypeString() + mpoints.getValue();

    StringBuilder b = new StringBuilder();
    b.append("INSERT INTO ").append(getConfig().getDestinationAreaBufferTableEntry().getTableName());
    // b.append("(ID,GEOMETRY) VALUES(?,ST_Buffer(ST_GeomFromText(?," + config.getServerSRID() +"),?))");
    // b.append("(ID,GEOMETRY) VALUES(?,ST_Buffer(ST_ConvexHull(ST_GeomFromText(?," + config.getServerSRID()
    // +")),?,'endcap=round join=round'))");
    // b.append("(ID,GEOMETRY) VALUES(?,ST_GeomFromText(?," + config.getServerSRID() +"))");
    b.append("(ID,GEOMETRY) VALUES(?,ST_Buffer(ST_ConcaveHull(ST_GeomFromText(?," + config.getServerSRID()
        + "),0.40,false),?,'endcap=round join=round'))");
    // b.append("(ID,GEOMETRY) VALUES(?,ST_Buffer(ST_ConcaveHull(ST_GeomFromText(?," + config.getServerSRID()
    // +"),0.90),?,'endcap=round join=round'))");

    // StringBuilder b2 = new StringBuilder();
    // b2.append("INSERT INTO LOG_POINTS (ID,GEOMETRY) VALUES(?,ST_GeomFromText(?," + config.getServerSRID() + "))");

    PreparedStatement statement = null;
    // PreparedStatement pstmt2 = null;
    try {

      // pstmt2 = connection.prepareStatement(b2.toString());
      // pstmt2.setInt(1, areaId);
      // pstmt2.setString(2, geometryString);

      statement = connection.prepareStatement(b.toString());
      statement.setInt(1, areaId);
      // statement.setObject(2,new PGgeometry(polygon),Types.OTHER);
      statement.setString(2, geometryString);
      statement.setDouble(3, bufferSize);
      // statement.setDouble(3, 0.0);
      statement.executeUpdate();
      // pstmt2.executeUpdate();
      connection.commit();
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
  public void storeAreaFromEdges(int areaId, List<IsoEdge> borderEdges, double bufferSize) {
    LOGGER.info("Insert polygon with # of ordinates: " + borderEdges.size());

    String geometryString;
    /*
     * if(points.size()<4) { LineString lineString = new LineString(PGUtil.asPGPoints(points)); geometryString =
     * lineString.getTypeString()+ lineString.getValue(); } else { LinearRing linearRing = new
     * LinearRing(PGUtil.asPGPoints(points)); Polygon polygon = new Polygon(new LinearRing[] {linearRing});
     * //MultiPolygon mpoly = new MultiPolygon(new Polygon[] {polygon}); //geometryString = mpoly.getTypeString()+
     * mpoly.getValue(); geometryString = polygon.getTypeString()+ polygon.getValue(); }
     */

    // org.postgis.Point[] pgPoints = PGUtil.asPGPoints(points);

    MultiLineString multiLineString = new MultiLineString(PGUtil.asPGLineString(borderEdges));
    geometryString = multiLineString.getTypeString() + multiLineString.getValue();

    StringBuilder b = new StringBuilder();
    b.append("INSERT INTO ").append(getConfig().getDestinationAreaBufferTableEntry().getTableName());
    // b.append("(ID,GEOMETRY) VALUES(?,ST_Buffer(ST_GeomFromText(?," + config.getServerSRID() +"),?))");
    // b.append("(ID,GEOMETRY) VALUES(?,ST_Buffer(ST_ConvexHull(ST_GeomFromText(?," + config.getServerSRID()
    // +")),?,'endcap=round join=round'))");
    // b.append("(ID,GEOMETRY) VALUES(?,ST_GeomFromText(?," + config.getServerSRID() +"))");
    b.append("(ID,GEOMETRY) VALUES(?,ST_Buffer(ST_ConcaveHull(ST_GeomFromText(?," + config.getServerSRID()
        + "),0.80),?,'endcap=round join=round'))");
    // b.append("(ID,GEOMETRY) VALUES(?,ST_Buffer(ST_ConcaveHull(ST_GeomFromText(?," + config.getServerSRID()
    // +"),0.90),?,'endcap=round join=round'))");

    // StringBuilder b2 = new StringBuilder();
    // b2.append("INSERT INTO LOG_POINTS (ID,GEOMETRY) VALUES(?,ST_GeomFromText(?," + config.getServerSRID() +"))");

    PreparedStatement statement = null;
    // PreparedStatement pstmt2 = null;
    try {

      // pstmt2 = connection.prepareStatement(b2.toString());
      // pstmt2.setInt(1, areaId);
      // pstmt2.setString(2,geometryString);

      statement = connection.prepareStatement(b.toString());
      statement.setInt(1, areaId);
      // statement.setObject(2,new PGgeometry(polygon),Types.OTHER);
      statement.setString(2, geometryString);
      statement.setDouble(3, bufferSize);
      // statement.setDouble(3, 0.0);
      statement.executeUpdate();
      // pstmt2.executeUpdate();
      connection.commit();
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
  public DBResult edgesInArea(int areaId) throws SQLException {
    StringBuilder b1 = new StringBuilder();
    b1.append("SELECT L.ID FROM ");
    b1.append(getConfig().getDestinationEdgeTableEntry().getTableName()).append(" L, ");
    b1.append(getConfig().getDestinationAreaBufferTableEntry().getTableName()).append(" A ");
    b1.append("WHERE A.ID=? AND ST_Within(L.GEOMETRY,A.GEOMETRY)=TRUE");
    PreparedStatement statement = connection.prepareStatement(b1.toString());
    statement.setInt(1, areaId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public int reachedInhabitants() {
    int reachedInhabitants = 0;
    PreparedStatement statement = null;
    StringBuilder b = new StringBuilder();
    b.append("SELECT SUM(I.INHABITANTS) FROM (SELECT DISTINCT B.ID, B.INHABITANTS FROM ");
    b.append(config.getProperty("tbl.building")).append(" B, ");
    b.append(getConfig().getDestinationAreaBufferTableEntry().getTableName()).append(" A ");
    b.append("WHERE ST_Within(B.GEOMETRY, A.GEOMETRY)='TRUE') I");
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
      connection.commit();
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
