package isochrones.mrnex.db;

import isochrones.db.DBResult;
import isochrones.mrnex.network.node.ANode;
import isochrones.network.GeoPoint;
import isochrones.network.GeoQueryPoint;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

/**
 * The <code>IQuery</code> interface provides only constant query strings that can be used in both db vendors.
 * 
 * @author Markus Innerebner
 * @version 3.0
 */
public interface IQuery {
  
  /**
   * 
   * <p>Method getCoordinates</p>
   * @param q
   * @return
   * @throws SQLException
   */
  public DBResult getCoordinates(GeoQueryPoint q) throws SQLException;
  
  /**
   * 
   * <p>Method getLinksInRange</p>
   * @param q
   * @param range
   * @return
   * @throws SQLException
   */
  public DBResult getLinksInRange(GeoQueryPoint q, double range) throws SQLException;
  
  /**
   * 
   * <p>Method getLinksInRange</p>
   * @param location
   * @param range
   * @return
   * @throws SQLException
   */
  public DBResult getLinksInRange(GeoPoint q, double range) throws SQLException;
  
  /**
   * 
   * <p>Method getLinksInRange</p> returns the links, whose end (start) vertex resides in the given range
   * @param q
   * @param range
   * @param loadedNodes
   * @return
   * @throws SQLException
   */
  public DBResult getLinksInRange(GeoPoint q, double range, Collection<ANode> loadedNodes,String[] aois) throws SQLException ;
  
  /**
   * 
   * <p>Method getContinuousLinksInRange</p>
   * @param q
   * @param range
   * @return
   * @throws SQLException
   */
  public DBResult getContinuousLinksInRange(GeoQueryPoint q, double range) throws SQLException ;
  
  /**
   * 
   * <p>Method getContinuousLinksInRange</p>
   * @param nodeId
   * @param range
   * @return
   * @throws SQLException
   */
  public DBResult getContinuousLinksInRange(int nodeId, double range) throws SQLException;

  

  /**
   * 
   * <p>Method logLoadedIERCircle</p>
   * @param nodeId
   * @param maxRadius
   * @param loadingRange
   */
  public void logLoadedIERCircle(int nodeId, double maxRadius, double loadingRange);
  
  /**
   * 
   * <p>Method logLoadedIERCircle</p>
   * @param q
   * @param maxRadius
   * @param loadingRange
   * @throws SQLException
   */
  public void logLoadedIERCircle(GeoQueryPoint q, double maxRadius, double loadingRange);
  
  /**
   * 
   * <p>Method logLoadedIERNode</p>
   * @param nodeId
   * @param distance
   * @param remainingDistance
   */
  public void logLoadedIERNode(int nodeId, double distance, double remainingDistance);
  
  /**
   * 
   * <p>Method getCoordinate</p>
   * @param nodeId
   * @return
   * @throws SQLException
   */
  public DBResult getCoordinate(int nodeId) throws SQLException ;
  
  /**
   * 
   * <p>Method getCoordinate</p>
   * @param nodeId
   * @param density
   * @return
   * @throws SQLException
   */
  public DBResult getCoordinate(int nodeId, int density) throws SQLException;
  
  /**
   * 
   * <p>Method getCoordinates</p>
   * @param nodeIds
   * @return
   * @throws SQLException
   */
  public DBResult getCoordinates(Integer[] nodeIds) throws SQLException ;

  /**
   * 
   * <p>Method getAdjacentDiscreteLinks</p> returns as result set the incident links
   * @param nodeId
   * @return
   * @throws SQLException
   */
  public DBResult getAdjacentDiscreteLinks(int nodeId) throws SQLException ;
  
  /**
   * 
   * <p>Method getAdjacentDiscreteLinksGeo</p> returns as result set the incident links and also the 
   * coordinates of the source vertex
   * @param nodeId
   * @return
   * @throws SQLException
   */
  public DBResult getAdjacentDiscreteLinksGeo(int nodeId) throws SQLException ;

  /**
   * 
   * <p>Method getAdjacentContinuousLinks</p>
   * @param nodeId
   * @return
   * @throws SQLException
   */
  public DBResult getAdjacentContinuousLinks(int nodeId) throws SQLException ;

  /**
   * 
   * <p>Method getDensity</p> computes the density for a given vertex in a given range
   * @param nodeId 
   * @param radius
   * @return the number of reachable edges in the given range
   * @throws SQLException
   */
  public DBResult getDensity(int nodeId, double radius) throws SQLException;

  /**
   * 
   * <p>Method getRange</p>
   * @param nodeId
   * @param density
   * @return
   * @throws SQLException
   */
  public DBResult getRange(int nodeId, int density) throws SQLException;
  
  
  /**
   * 
   * <p>Method getLatestDepartureTimeWithSourceLocation</p>
   * @param source
   * @param target
   * @param routeId
   * @param businessDays
   * @param earliestTime
   * @param latestTime
   * @return
   * @throws SQLException
   */
  public DBResult getLatestDepartureTimeWithSourceLocation(int source, int target, short routeId, Set<Integer> businessDays, long earliestTime, long latestTime) throws SQLException;
  
  /**
   * 
   * <p>Method getLatestDepartureTimeWithSourceLocation</p>
   * @param source
   * @param target
   * @param businessDays
   * @param earliestTime
   * @param latestTime
   * @return
   * @throws SQLException
   */
  public DBResult getLatestDepartureTimeWithSourceLocation(int source, int target,
                                                           Set<Integer> businessDays, long earliestTime, long latestTime) throws SQLException;
  
  /**
   * 
   * <p>Method getLatestDepartureTimeWithSourceLocation</p>
   * @param target
   * @param businessDays
   * @param earliestTime
   * @param latestTime
   * @return
   * @throws SQLException
   */
  public DBResult getLatestDepartureTimeWithSourceLocation(int target, Set<Integer> businessDays, long earliestTime, long latestTime) throws SQLException;

  
  /**
   * 
   * <p>Method getLinksInLimitedRange</p>
   * @param id the id of the vertex
   * @param cordinate
   * @param nodeSize
   * @return
   * @throws SQLException
   */
  public DBResult getLinksInLimitedRange(int id, GeoPoint cordinate, int nodeSize) throws SQLException;

  /**
   * 
   * <p>Method logLoadedIERArea</p>
   * @param id
   * @param aios
   * @param range
   * @throws SQLException
   */
  public void logLoadedIERArea(int id, String[] aios, double range) throws SQLException; 
}
