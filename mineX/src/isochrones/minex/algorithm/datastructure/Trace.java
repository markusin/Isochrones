package isochrones.minex.algorithm.datastructure;

import isochrones.algorithm.Mode;
import isochrones.algorithm.datastructure.AbstractTrace;
import isochrones.algorithm.statistics.DBType;
import isochrones.algorithm.statistics.Statistic;
import isochrones.algorithm.statistics.Type;
import isochrones.db.DBResult;
import isochrones.db.IsochroneQuery;
import isochrones.minex.network.node.NodeBINE;
import isochrones.network.NWMode;
import isochrones.network.NoSuchLinkException;
import isochrones.network.NoSuchNodeException;
import isochrones.network.link.ContinuousLink;
import isochrones.network.link.DiscreteLink;
import isochrones.network.link.ILink;
import isochrones.network.link.LinkCollection;
import isochrones.network.node.INode;
import isochrones.utils.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * The <code>Trace</code> class represents a bi-modal network trace used for the computation of isochrones. The trace
 * consists of merged pedestrian and bus networks. <code>Trace</code> allows to store nodes which can be connected to
 * pedestrian and bus links.
 * 
 * @author Markus Innerebner
 * @version 3.0
 */
public class Trace extends AbstractTrace {

  /* Data structures used by the trace ------------------------------------ */
  protected HashMap<Integer, NodeBINE> nodes;
  private Set<Integer> removedNodeIds = new HashSet<Integer>();
  
  private int maxNodeSize = 0;

  /**
   * 
   * <p>Constructs a(n) <code>Trace</code> object.</p>
   * @param mode
   * @param query
   * @param statistic
   */
  public Trace(Config config, IsochroneQuery query, Statistic statistic) {
    super(config,query,statistic);
    nodes = new HashMap<Integer, NodeBINE>();
  }

  @Override
  public ContinuousLink getInitialLink(int linkId) {
    ContinuousLink link = null;
    DBResult dbResult = null;
    try {
      long start = System.currentTimeMillis(); // Counter
      dbResult = (config.getMode().equals(Mode.UNIMODAL)) ? query.getContinuousLink(linkId) : query.getLink(linkId);
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
  
  /**
   * 
   */
  public NodeBINE getInitialNode(int nodeId) {
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
  private void addNode(int nodeId, short startNodeOutgoingNodes) {
    if (!nodes.containsKey(nodeId)) {
      statistic.log(Type.LOADED_NODES);
      nodes.put(nodeId, new NodeBINE(nodeId, startNodeOutgoingNodes));
      maxNodeSize = Math.max(maxNodeSize,nodes.size());
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
  public NodeBINE getNode(int nodeId) {
    return nodes.get(nodeId);
  }

  /**
   * Removes the passed <code>node</code> from the trace.
   * 
   * @param node the node that has to be removed
   */
  public void removeNode(NodeBINE node) {
    statistic.log(Type.REMOVED_NODES);
    if (removedNodeIds.contains(node.getId())) {
      System.err.println("Node: " + node.getId() + " already removed");
    } else {
      removedNodeIds.add(node.getId());
      //storeNode(node);
    }
    nodes.remove(node.getId());
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
  
  public int getMaxNodeSize() {
    return maxNodeSize;
  }
  
}
