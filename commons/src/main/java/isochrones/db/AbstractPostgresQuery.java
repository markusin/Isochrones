/**
 * 
 */
package isochrones.db;

import isochrones.network.GeoPoint;
import isochrones.network.Offset;
import isochrones.network.link.ILink;
import isochrones.network.node.Entity;
import isochrones.utils.Config;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

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
public abstract class AbstractPostgresQuery extends IsochroneQuery {

  private String QUERY_E_NN, QUERY_STORE_LINKS, QUERY_STORE_PARTIALLINKS;
  
  public AbstractPostgresQuery(Config config) {
    super(config);
    if(config!=null) init();
  }
  
  private void init() {
    QUERY_STORE_LINKS = "INSERT INTO " + config.getDestinationEdgeTableEntry().getTableName()
        + " (\"ID\",\"SOURCE\",\"TARGET\",\"OFFSET\",\"LENGTH\",\"GEOMETRY\") (SELECT ID,SOURCE,TARGET,?,LENGTH," + " GEOMETRY FROM " + config.getEdgeTable() + " WHERE ID =?)";

    QUERY_STORE_PARTIALLINKS = "INSERT INTO " + config.getDestinationEdgeTableEntry().getTableName()
        + " (\"ID\",\"SOURCE\",\"TARGET\",\"OFFSET\",\"LENGTH\",\"GEOMETRY\") (SELECT ID,SOURCE,TARGET,?,LENGTH," + 
        " ST_LINE_SUBSTRING(GEOMETRY,?,?) FROM " + config.getEdgeTable() + " WHERE ID = ?)";
    
    QUERY_E_NN = "SELECT E.ENTITY_ID,E.EDGE_ID,E.OFFSET,E.GEOMETRY,ST_Distance(E.GEOMETRY,Q.GEOMETRY) FROM " 
        + "(SELECT M.ENTITY_ID,M.EDGE_ID,M.OFFSET,P.GEOMETRY FROM"
        + config.getProperty("tbl.entities.mapping") + " M," + config.getProperty("tbl.entities")
        + " P WHERE M.ENTITY_ID=P.ENTITY_ID) E,"
        + " ( SELECT ST_PointFromText(?,?) AS GEOMETRY) Q"
        + " ORDER BY ST_Distance(E.GEOMETRY,Q.GEOMETRY) LIMIT ?";
  }

  @Override
  public final void storeLinks(Collection<ILink> links) {
    PreparedStatement statementPartial = null, statementFull = null;
    try {
      statementPartial = connection.prepareStatement(QUERY_STORE_PARTIALLINKS);
      statementFull = connection.prepareStatement(QUERY_STORE_LINKS);
      ((PGStatement) statementPartial).setPrepareThreshold(config.getBatchSize());
      ((PGStatement) statementFull).setPrepareThreshold(config.getBatchSize());
      for (ILink link : links) {
        Offset offset = link.getOffset();
        if (offset != null) {
          int linkID = link.getId();
          if (offset.getStartOffset() == 0 && offset.getEndOffset() == link.getLength()) {
            statementFull.setDouble(1, config.isIncoming() ? offset.getStartOffset() : offset.getEndOffset());
            statementFull.setInt(2, linkID);
            statementFull.executeUpdate();
          } else {
            if(config.isIncoming()){
              if (offset.getStartOffset() < link.getLength()) {
                statementPartial.setDouble(1, config.isIncoming() ? offset.getStartOffset() : offset.getEndOffset());
                statementPartial.setDouble(2, offset.getStartOffset() / link.getLength());
                statementPartial.setDouble(3, offset.getEndOffset() / link.getLength());
                statementPartial.setInt(4, linkID);
                statementPartial.executeUpdate();
              }
            } else {
              if (offset.getStartOffset() > 0) {
                statementPartial.setDouble(1, offset.getStartOffset());
                statementPartial.setDouble(2, offset.getStartOffset() / link.getLength());
                statementPartial.setDouble(3, offset.getEndOffset() / link.getLength());
                statementPartial.setInt(4, linkID);
                statementPartial.executeUpdate();
              }
            }
          }
        }
      }
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      try {
        statementFull.close();
        statementPartial.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
  @Override
  public void storeEntities(Collection<Entity> entities) {
    PreparedStatement insertVerticesStatement = null;
    try {
      insertVerticesStatement = connection.prepareStatement(QUERY_INSERT_ENTITIES);
      ((PGStatement) insertVerticesStatement).setPrepareThreshold(config.getBatchSize());
      int k =1;
      for (Entity entity : entities) {
        insertVerticesStatement.setDouble(1, entity.getDistance());
        insertVerticesStatement.setInt(2, k++);
        insertVerticesStatement.setInt(3, entity.getId());
        insertVerticesStatement.executeUpdate();
      }
      connection.commit();
    } catch (SQLException e) {
      LOGGER.severe(e.getMessage());
    } finally {
      try {
        if (insertVerticesStatement != null)
          insertVerticesStatement.close();
      } catch (SQLException e) {
        LOGGER.severe(e.getMessage());
        e.printStackTrace();
      }
    }
  }

  @Override
  public void logVertices(int vertexId, double distance) {
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(QUERY_LOG_VERTICES);
      ((PGStatement) pstmt).setPrepareThreshold(config.getBatchSize());
      pstmt.setInt(1, vertexId);
      pstmt.setDouble(2, distance);
      pstmt.executeUpdate();
    } catch (SQLException e) {
      e.printStackTrace();
      try {
        connection.rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
    } finally {
      try {
        pstmt.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
  /**
   * 
   * <p>Method euclideanNN</p>
   * @param q
   * @param k
   * @return a dbresult projecting the entity, the mapped edge id, the offset, the geometry and the euclidean distance
   * @throws SQLException
   */
  public DBResult euclideanNN(GeoPoint q, int k) throws SQLException {
    PreparedStatement pstmt = connection.prepareStatement(QUERY_E_NN);
    pstmt.setString(1, "POINT(" + q.getX() + " " + q.getY() + ")");
    pstmt.setInt(2, config.getServerSRID());
    pstmt.setInt(3, k*2); // since each entity lays on 2 edges, we have to double the k
    return new DBResult(pstmt, pstmt.executeQuery());
  }

}
