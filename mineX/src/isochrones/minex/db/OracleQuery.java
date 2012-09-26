/**
 * 
 */
package isochrones.minex.db;

import isochrones.db.AbstractOracleQuery;
import isochrones.db.DBResult;
import isochrones.utils.Config;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * <p>The <code>OracleQuery</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class OracleQuery extends AbstractOracleQuery {
  
  private String QUERY_GET_CONTINUOUS_LINK,QUERY_GET_LINK,QUERY_GET_CONTINUOUS_INCIDENT_LINKS,QUERY_GET_ALL_INCIDENT_LINKS,QUERY_GET_CONTINUOUS_NODE,QUERY_GET_NODE;
  
  /**
   * 
   * <p>Constructs a(n) <code>OracleQuery</code> object.</p>
   * @param database
   */
  public OracleQuery(Config config) {
    super(config);
    initConstants();
  }
  
  @Override
  protected void initConstants() {
    super.initConstants();
    QUERY_GET_CONTINUOUS_LINK = QueryConstants.getContinuousLinkQueryString(config);
    QUERY_GET_LINK = QueryConstants.getLinkQueryString(config);
    QUERY_GET_CONTINUOUS_INCIDENT_LINKS = QueryConstants.getAdjacentContinuousLinksQueryString(config);
    QUERY_GET_ALL_INCIDENT_LINKS = QueryConstants.getAdjacentLinksQueryString(config);
    QUERY_GET_CONTINUOUS_NODE = QueryConstants.getContinuousNodeQueryString(config);
    QUERY_GET_NODE = QueryConstants.getNodeQueryString(config);
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
  public DBResult getAdjacentContinuousLinks(int endnodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_CONTINUOUS_INCIDENT_LINKS);
    statement.setInt(1, endnodeId);
    return new DBResult(statement, statement.executeQuery());
  }

  @Override
  public DBResult getAdjacentLinks(int nodeId) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(QUERY_GET_ALL_INCIDENT_LINKS);
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

}
