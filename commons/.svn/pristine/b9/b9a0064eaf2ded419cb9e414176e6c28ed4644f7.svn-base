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

import oracle.jdbc.OraclePreparedStatement;

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
public abstract class AbstractOracleQuery extends IsochroneQuery {

  protected static final int ORA_ERROR_TABLE_NOT_EXIST = 942;
  protected static final int ORA_ERROR_INDEX_NOT_EXIST = 1418;
  private String QUERY_E_NN, QUERY_STORE_LINKS, QUERY_STORE_PARTIALLINKS;

  public AbstractOracleQuery(Config config) {
    super(config);
    init();
  }

  private void init() {
    QUERY_E_NN = "SELECT M.ENTITY_ID,M.EDGE_ID,M.OFFSET,P.GEOMETRY,SDO_NN_DISTANCE(1) FROM " + config.getProperty("tbl.entities.mapping")
        + " M," + config.getProperty("tbl.entities")
        + " P WHERE M.ENTITY_ID=P.ENTITY_ID AND SDO_NN(P.GEOMETRY,SDO_GEOMETRY(:1,:2),:3,1)='TRUE'";
    
    QUERY_STORE_LINKS = "INSERT INTO " + config.getDestinationEdgeTableEntry().getTableName()
        + " (\"ID\",\"SOURCE\",\"TARGET\",\"OFFSET\",\"LENGTH\",\"GEOMETRY\") (SELECT ID,SOURCE,TARGET,:1,LENGTH," + " GEOMETRY FROM " + config.getEdgeTable() + " WHERE ID = :2)";

    QUERY_STORE_PARTIALLINKS = "INSERT INTO " + config.getDestinationEdgeTableEntry().getTableName()
        + " (\"ID\",\"SOURCE\",\"TARGET\",\"OFFSET\",\"LENGTH\",\"GEOMETRY\") (SELECT ID,SOURCE,TARGET,:1,LENGTH," + " SDO_LRS.CONVERT_TO_STD_GEOM"
        + "(SDO_LRS.CLIP_GEOM_SEGMENT(SDO_LRS.CONVERT_TO_LRS_GEOM(GEOMETRY), :2, :3)) " + "FROM " + config.getEdgeTable()
        + " WHERE ID = :4)";
  
  }

  @Override
  public final void storeLinks(Collection<ILink> links) {

    PreparedStatement statementPartial = null, statementFull = null;
    try {
      statementFull = connection.prepareStatement(QUERY_STORE_LINKS);
      ((OraclePreparedStatement) statementFull).setExecuteBatch(config.getBatchSize());
      statementPartial = connection.prepareStatement(QUERY_STORE_PARTIALLINKS);
      ((OraclePreparedStatement) statementPartial).setExecuteBatch(config.getBatchSize());
      for (ILink link : links) {
        Offset offset = link.getOffset();
        if (offset != null) {
          int linkID = link.getId();
          // if (offset.getStartOffset() == 0 && offset.getEndOffset() == 0) {
          if (offset.getStartOffset() == 0 && offset.getEndOffset() == link.getLength()) {
            statementFull.setDouble(1, config.isIncoming() ? offset.getStartOffset() : offset.getEndOffset());
            statementFull.setInt(2, linkID);
            if (statementFull.executeUpdate() == config.getBatchSize()) {
              connection.commit();
            }
          } else {
            if (offset.getStartOffset() < link.getLength()) {
              statementPartial.setDouble(1, config.isIncoming() ? offset.getStartOffset() : offset.getEndOffset());
              statementPartial.setDouble(2, offset.getStartOffset());
              statementPartial.setDouble(3, offset.getEndOffset());
              statementPartial.setInt(4, linkID);
              if (statementPartial.executeUpdate() == config.getBatchSize()) {
                connection.commit();
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
      ((OraclePreparedStatement) insertVerticesStatement).setExecuteBatch(config.getBatchSize());
      int k = 1;
      for (Entity entity : entities) {
        insertVerticesStatement.setDouble(1, entity.getDistance());
        insertVerticesStatement.setInt(2, k++);
        insertVerticesStatement.setInt(3, entity.getId());
        if (insertVerticesStatement.executeUpdate() == config.getBatchSize()) {
          connection.commit();
        }
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
  public final void logVertices(int vertexId, double distance) {
    PreparedStatement pstmt = null;
    try {
      pstmt = connection.prepareStatement(QUERY_LOG_VERTICES);
      pstmt.setInt(1,vertexId);
      pstmt.setDouble(2,distance);
      pstmt.executeUpdate();
      connection.commit();
    } catch (SQLException e) {
      e.printStackTrace();
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
    pstmt.setString(3, "SDO_NUM_RES=" + k*2); // since each entity lays on 2 edges, we have to double the k
    return new DBResult(pstmt, pstmt.executeQuery());
  }

}
