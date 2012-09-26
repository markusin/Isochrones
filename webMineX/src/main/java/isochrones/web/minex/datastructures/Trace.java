package isochrones.web.minex.datastructures;

import isochrones.algorithm.Mode;
import isochrones.algorithm.datastructure.AbstractTrace;
import isochrones.algorithm.statistics.DBType;
import isochrones.algorithm.statistics.Statistic;
import isochrones.algorithm.statistics.Type;
import isochrones.db.DBResult;
import isochrones.db.IsochroneQuery;
import isochrones.network.Location;
import isochrones.network.NWMode;
import isochrones.network.NoSuchLinkException;
import isochrones.network.NoSuchNodeException;
import isochrones.network.link.ContinuousLink;
import isochrones.network.link.DiscreteLink;
import isochrones.network.link.ILink;
import isochrones.network.node.INode;
import isochrones.utils.DBUtility;
import isochrones.web.cache.QueryPointCache;
import isochrones.web.config.Config;
import isochrones.web.db.IWebQuery;
import isochrones.web.geometry.BBox;
import isochrones.web.network.node.QueryPoint;
import isochrones.web.network.node.WebNode;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The <code>Trace</code> class is the data structure that contains all information about the network elements. It
 * allows to store nodes and links. The algorithm retrieves the network element via the trace. Whenever the trace does
 * not contain an element, it retrieves it from the DB. Whenever the trace removes elements, they are written into the
 * resultset object.
 * 
 * @author Markus Innerebner
 * @version 2.0
 */
public class Trace extends AbstractTrace {

  /* Data structures used by the trace ------------------------------------ */
  protected HashMap<Integer, WebNode> nodes;

  protected Set<WebNode> storableNodes = new HashSet<WebNode>();
  private Set<Integer> removedNodeIds = new HashSet<Integer>();
  private long timeTermination, timeStoreVertices, timeStoreEdges;

  /**
   * @param config
   * @param query
   * @param statistic
   * @param resultSet
   */
  public Trace(Config config, IsochroneQuery query, Statistic statistic) {
    super(config, query, statistic);
    init();
    nodes = new HashMap<Integer, WebNode>();
  }

  /**
   * <p>
   * Method getConfig
   * </p>
   * 
   * @return
   */
  private Config getConfig() {
    return (Config) config;
  }

  /**
   * <p>
   * Method getQuery
   * </p>
   * 
   * @return
   */
  public IWebQuery getQuery() {
    return (IWebQuery) query;
  }

  @Override
  public ContinuousLink getInitialLink(int linkId) {
    ContinuousLink link = null;
    DBResult dbResult = null;
    try {
      long start = System.currentTimeMillis(); // Counter
      if (getMode().equals(Mode.UNIMODAL)) {
        dbResult = query.getContinuousLink(linkId);
      } else {
        dbResult = query.getLink(linkId);
      }
      ResultSet resultSet = dbResult.getResultSet();
      if (resultSet.next()) {
        int sourceId = resultSet.getInt("SOURCE");
        int targetId = resultSet.getInt("TARGET");
        short degree = resultSet.getShort("NODE_DEGREE");
        double length = resultSet.getDouble("LENGTH");
        link = new ContinuousLink(linkId, sourceId, targetId, length);
        if(config.isIncoming()){
          addNode(sourceId, degree);
        } else {
          addNode(targetId, degree);
        }
        statistic.log(Type.LOADED_CONTINUOUS_LINKS);
      } else {
        throw new NoSuchLinkException(linkId);
      }
      statistic.logRuntime(DBType.GET_CONTINUOUS_LINK, System.currentTimeMillis() - start); 
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    return link;
  }

  @Override
  public WebNode getInitialNode(int nodeId) {
    DBResult dbResult = null;
    try {
      long start = System.currentTimeMillis(); // Counter
      dbResult = (config.getMode().equals(Mode.UNIMODAL)) ? query.getContinuousNode(nodeId) : query.getNode(nodeId);
      ResultSet resultSet = dbResult.getResultSet();
      if (resultSet.next()) {
        addNode(nodeId, resultSet.getShort("NODE_DEGREE"));
      } else {
        throw new NoSuchNodeException(nodeId);
      }
      statistic.logRuntime(DBType.GET_NODES, System.currentTimeMillis() - start); 
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    return getNode(nodeId);
  }

  /*
   * Adds the passed node to the trace if it does not exist in the trace
   */
  private void addNode(int nodeId, int startNodeOutgoingNodes) {
    if (!nodes.containsKey(nodeId)) {
      statistic.log(Type.LOADED_NODES);
      nodes.put(nodeId, new WebNode(nodeId, startNodeOutgoingNodes));
    }
  }

  @Override
  public Collection<ILink> getAdjacentContinuousLinks(INode node) {
    Collection<ILink> adjacentLinks = new ArrayList<ILink>();
    DBResult dbResult = null;
    try {
      int nodeId = node.getId();
      long start = System.currentTimeMillis(); // Counter
      dbResult = query.getAdjacentContinuousLinks(nodeId);
      ResultSet resultSet = dbResult.getResultSet();
      while (resultSet.next()) {
        ILink link;
        int linkId = resultSet.getInt("ID");
        int adjacentNodeId = resultSet.getInt("NODE_ID");
        short degree = resultSet.getShort("NODE_DEGREE");
        double length = resultSet.getDouble("LENGTH");
        if(config.isIncoming()){
          link = new ContinuousLink(linkId, adjacentNodeId, nodeId, length);
        } else {
          link = new ContinuousLink(linkId, nodeId, adjacentNodeId, length);
        }
        addNode(adjacentNodeId, degree);
        adjacentLinks.add(link);
        statistic.log(Type.LOADED_CONTINUOUS_LINKS);
      }
      statistic.logRuntime(DBType.GET_LINKS, System.currentTimeMillis() - start);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null) {
        dbResult.close();
      }
    }
    return adjacentLinks;
  }

  @Override
  protected Collection<ILink> getAllAdjacentLinks(INode node) {
    Collection<ILink> adjacentLinks = new ArrayList<ILink>();
    DBResult dbResult = null;
    try {
      int nodeId = node.getId();
      long start = System.currentTimeMillis(); // Counter
      dbResult = query.getAdjacentLinks(nodeId);
      ResultSet resultSet = dbResult.getResultSet();
      while (resultSet.next()) {
        ILink link;
        int linkId = resultSet.getInt("ID");
        byte linkType = resultSet.getByte("EDGE_MODE");
        int adjacentNodeId = resultSet.getInt("NODE_ID");
        short degree = resultSet.getShort("NODE_DEGREE");
        if (linkType==NWMode.CONTINUOUS) {
          double length = resultSet.getDouble("LENGTH");
          if(config.isIncoming()){
            link = new ContinuousLink(linkId, adjacentNodeId, nodeId, length);
          } else {
            link = new ContinuousLink(linkId, nodeId, adjacentNodeId, length);
          }
          statistic.log(Type.LOADED_CONTINUOUS_LINKS);
        } else {
          short routeId = resultSet.getShort("ROUTE_ID");
          if(config.isIncoming()){
            link = new DiscreteLink(linkId, adjacentNodeId, nodeId, routeId);
          } else {
            link = new DiscreteLink(linkId, nodeId, adjacentNodeId, routeId);
          }
          statistic.log(Type.LOADED_DISCRETE_LINKS);
        }
        addNode(adjacentNodeId, degree);
        adjacentLinks.add(link);
      }
      statistic.logRuntime(DBType.GET_LINKS, System.currentTimeMillis() - start);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    return adjacentLinks;
  }

  /**
   * Returns the node specified by the passed <code>nodeId</code>.
   * 
   * @param nodeId the ID of the requested node
   * @return the node specified by the passed node ID
   */
  public WebNode getNode(int nodeId) {
    return nodes.get(nodeId);
  }

  private void init() {
    DBUtility.truncateTable(config.getConnection(),  getConfig().getDestinationVertexTableEntry().getTableName());
    DBUtility.controlIndex(config.getConnection(), config.getDbVendor(), getConfig().getDestinationVertexTableEntry(), true);
    DBUtility.truncateTable(config.getConnection(), getConfig().getDestinationVertexAnnotatedTableEntry().getTableName());
    if(getConfig().enableAreaCalculation()){
      DBUtility.truncateTable(config.getConnection(), getConfig().getDestinationAreaBufferTableEntry().getTableName());
    }
  }

  /*
   * <p> Method terminate </p>
   * @param clientId
   * @return
   * @throws SQLException
   */
  public void terminate() {
    long start = System.currentTimeMillis();
    storableNodes.addAll(nodes.values());
    /*
    for (WebNode node : storableNodes) {
      removeNode(node);
    }
    */
    getQuery().storeVertices(storableNodes);
    getQuery().updateVertexTable();
    
    DBUtility
        .controlIndex(config.getConnection(), config.getDbVendor(),  getConfig().getDestinationVertexTableEntry(), false);
    timeTermination = System.currentTimeMillis() - start;
      query.storeLinks(storableLinks.values());
      DBUtility
          .controlIndex(config.getConnection(), config.getDbVendor(), config.getDestinationEdgeTableEntry(), false);
    /*
     * for (Node node : storableNodes) { getQuery().storeVertices(storableNodes); }
     */

    // LOGGER.info("Finished writing remaining nodes and links after "
    // + timeTermination + "ms.");
    // return getQuery().getIsochroneBoundingBox();
  }

  /**
   * <p>
   * Method getBoundingBox
   * </p>
   * 
   * @return
   */
  public BBox getBoundingBox() {
    return getQuery().getIsochroneBoundingBox();
  }

  /**
   * <p>
   * Method remove
   * </p>
   * 
   * @param node
   */
  public void removeNode(WebNode node) {
    storableNodes.add(node);
    if (storableNodes.size() >= config.getBatchSize()) {
      long start = System.currentTimeMillis(); // Counter
      getQuery().storeVertices(storableNodes);
      storableNodes.clear();
      timeStoreVertices += System.currentTimeMillis() - start; // Counter
    }
    if (removedNodeIds.contains(node.getId())) {
      LOGGER.warning("Node: " + node.getId() + " already removed");
    } else {
      nodes.remove(node.getId());
      removedNodeIds.add(node.getId());
    }
  }

  /**
   * <p>
   * Method projectOnLinks
   * </p>
   * projects the query point to the two closest continuous located links
   * 
   * @param qPoint the query point consisting of x and y coordinate
   * @return
   * @throws SQLException
   */
  public Set<isochrones.network.Location> projectOnLinks(QueryPoint qPoint) {
    Set<Location> locations = QueryPointCache.getCachedQueryPoint(qPoint);
    if (locations == null) {
      DBResult dbResult = null;
      try {
        dbResult = getQuery().projectOnLinks(qPoint);
        ResultSet rSet = dbResult.getResultSet();
        locations = new HashSet<Location>();
        while (rSet.next()) {
          int link = rSet.getInt("ID");
          double offset = rSet.getDouble("OFFSET");
          locations.add(new Location(link, offset));
        }
        QueryPointCache.addQueryPoint(qPoint, locations);
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        dbResult.close();
      }
    }
    return locations;
  }

  /**
   * <p>
   * Method appendLoggingData
   * </p>
   * 
   * @return
   */
  public JSONObject appendLoggingData(JSONObject jsonObject) {
    try {
      jsonObject.put("timeStoreEdges", timeStoreEdges);
      jsonObject.put("timeStoreVertices", timeStoreVertices);
      jsonObject.put("timeTermination", timeTermination);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  @Override
  public int getNodeSize() {
    return nodes.size();
  }

  @Override
  public int getLinkSize() {
    return 0;
  }

  @Override
  public int getContinuousLinkSize() {
    return 0;
  }

  @Override
  public int getDiscreteLinkSize() {
    return 0;
  }

  @Override
  public double getAdjacentNodeCost(INode node, INode adjacentNode,Set<Short> routeIds,Set<Integer> dateCodes, long fromTime, long toTime) {
    double minDistance = INode.Value.INFINITY;
    DBResult dbResult = null;
    try {
      long time = System.currentTimeMillis();
      if(config.isIncoming()){
        dbResult = query.getLatestDepartureTime(adjacentNode.getId(),node.getId(),routeIds,dateCodes,fromTime, Math.round(toTime - node.getDistance()));
        ResultSet resultSet = dbResult.getResultSet();
        while (resultSet.next()) {
          long departureTime = resultSet.getLong("TIME_D");
          long arrivalTime = resultSet.getLong("TIME_A");
          short routeId = resultSet.getShort("ROUTE_ID");
          WebNode adjacentWebNode = ((WebNode) adjacentNode);
          adjacentWebNode.setDepartureTime(routeId, departureTime);
          WebNode webNode = ((WebNode) node);
          webNode.setArrivalTime(routeId, arrivalTime);
          double distance = toTime - departureTime;
          if (distance < minDistance) {
            minDistance = distance;
            adjacentWebNode.setCheapestReachedRouteId(routeId);
          }
        }
      } else {
        dbResult = query.getEarliestArrivalTime(node.getId(), adjacentNode.getId(),routeIds,dateCodes,Math.round(fromTime + node.getDistance()), toTime);
        ResultSet resultSet = dbResult.getResultSet();
        while (resultSet.next()) {
          long departureTime = resultSet.getLong("TIME_D");
          long arrivalTime = resultSet.getLong("TIME_A");
          short routeId = resultSet.getShort("ROUTE_ID");
          WebNode adjacentWebNode = ((WebNode) adjacentNode);
          adjacentWebNode.setArrivalTime(routeId, arrivalTime);
          WebNode webNode = ((WebNode) node);
          webNode.setDepartureTime(routeId, departureTime);
          double distance = arrivalTime>0 ? arrivalTime - fromTime : INode.Value.INFINITY;
          if (distance < minDistance) {
            minDistance = distance;
            adjacentWebNode.setCheapestReachedRouteId(routeId);
          }
        }
      }
      statistic.logRuntime(DBType.GET_DISCRETE_HOMO_COST, System.currentTimeMillis() - time);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      dbResult.close();
    }
    return minDistance;
  }

  @Override
  public Map<Integer, Double> getAdjacentNodesCost(INode node,Set<Short> routeIds,Set<Integer> departureDateCodes, long fromTime,long toTime) {
    HashMap<Integer, Double> nodeDistances = new HashMap<Integer, Double>();
    DBResult dbResult = null;
    try {
      long time = System.currentTimeMillis(); // Counter
      if(config.isIncoming()){
        dbResult = query.getLatestDepartureTimes(node.getId(),routeIds,departureDateCodes,fromTime,Math.round(toTime - node.getDistance()));
        ResultSet resultSet = dbResult.getResultSet();
        while (resultSet.next()) {
          int adjacentNodeId = resultSet.getInt("SOURCE");
          long departureTime = resultSet.getLong("TIME_D");
          long arrivalTime = resultSet.getLong("TIME_A");
          short routeId = resultSet.getShort("ROUTE_ID");
          WebNode adjacentWebNode = nodes.get(adjacentNodeId);
          adjacentWebNode.setDepartureTime(routeId, departureTime);
          WebNode webNode = ((WebNode) node);
          webNode.setArrivalTime(routeId, arrivalTime);
          double distance = toTime - departureTime;
          if (nodeDistances.get(adjacentNodeId) == null || distance < nodeDistances.get(adjacentNodeId)) {
            nodeDistances.put(adjacentNodeId, distance);
            adjacentWebNode.setCheapestReachedRouteId(routeId);
          }
        }
      } else {
        dbResult = query.getEarliestArrivalTimes(node.getId(),routeIds,departureDateCodes,Math.round(fromTime + node.getDistance()),toTime);
        ResultSet resultSet = dbResult.getResultSet();
        while (resultSet.next()) {
          int adjacentNodeId = resultSet.getInt("TARGET");
          long departureTime = resultSet.getLong("TIME_D");
          long arrivalTime = resultSet.getLong("TIME_A");
          short routeId = resultSet.getShort("ROUTE_ID");
          WebNode adjacentWebNode = nodes.get(adjacentNodeId);
          adjacentWebNode.setArrivalTime(routeId, arrivalTime);
          WebNode webNode = ((WebNode) node);
          webNode.setDepartureTime(routeId, departureTime);
          double distance = arrivalTime>0 ? arrivalTime - fromTime : INode.Value.INFINITY;
          if (nodeDistances.get(adjacentNodeId) == null || distance < nodeDistances.get(adjacentNodeId)) {
            nodeDistances.put(adjacentNodeId, distance);
            adjacentWebNode.setCheapestReachedRouteId(routeId);
          }
        }
      }
      statistic.logRuntime(DBType.GET_DISCRETE_HETERO_COST, System.currentTimeMillis() - time);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      dbResult.close();
    }
    return nodeDistances;
  }
}
