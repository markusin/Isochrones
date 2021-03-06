package isochrones.minex.algorithm;

import isochrones.algorithm.Isochrone;
import isochrones.algorithm.statistics.Statistic;
import isochrones.algorithm.statistics.Type;
import isochrones.db.DBVendor;
import isochrones.minex.algorithm.datastructure.Trace;
import isochrones.minex.db.OracleQuery;
import isochrones.minex.db.PostgresQuery;
import isochrones.minex.network.node.NodeBINE;
import isochrones.network.Location;
import isochrones.network.Offset;
import isochrones.network.link.ContinuousLink;
import isochrones.network.link.DiscreteLink;
import isochrones.network.link.LinkCollection;
import isochrones.network.node.INode;
import isochrones.utils.Config;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * The <code>MineX</code> class allows to compute isochrones in a bi-modal, schedule-based transport network, where
 * walking in combination with the public transport is considered. This version of the isochrone algorithm uses one
 * pedestrian and several bus networks which are merged into a single network.
 * <p>
 * Isochrones are defined as the set of all points from which a specific query point is reachable within a given time
 * span. The used graph exploration algorithm is based on Dijkstra's Shortest Path. This particular version of the
 * isochrone algorithm tries to optimize memory utilization. It starts with an empty graph, adding nodes and links as
 * the network exploration proceeds. Moreover, network segments, which are no longer used, are removed from the graph.
 * 
 * @author Markus Innerebner
 * @version 3.0
 */
public class MineX extends Isochrone {

  /* Trace ---------------------------------------------------------------- */
  protected PriorityQueue<NodeBINE> priorityQueue;
  protected Trace trace;

  /**
   * <p>
   * Constructs a(n) <code>BINE</code> object.
   * </p>
   * 
   * @param config
   */
  public MineX(Config config) {
    super(config, config.getDbVendor().equals(DBVendor.ORACLE) ? new OracleQuery(config) : new PostgresQuery(config),
          new Statistic(config.getAlgorithmName(), config.getMode()));
    priorityQueue = new PriorityQueue<NodeBINE>();
    trace = new Trace(config, query, statistic);
  }

  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime) {
    setParameters(locations, duration, walkingSpeed, targetTime, trace);
    exploreInitialLocations();
    compute();
  }

  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime) {
    setParameters(duration, walkingSpeed, targetTime, trace);
    exploreInitialNodes(nodeIds);
    compute();
  }

  @Override
  public void compute() {
    NodeBINE node = priorityQueue.poll(); // Dequeues the first node from priority queue
    while (node != null) {
      expandNode(node);
      maxPrioQueueSize = Math.max(maxPrioQueueSize, priorityQueue.size());// Counter
      maxTraceSize = Math.max(maxTraceSize, trace.size()); // Counter
      node = priorityQueue.poll(); // Dequeues the next node
    }
    terminate();
  }

  @Override
  protected void exploreInitialNodes(int[] nodeIds) {
    for (int nodeId : nodeIds) {
      NodeBINE node = trace.getInitialNode(nodeId);
      node.setDistance(0);
      updateQueue(node);
    }
  }

  @Override
  protected void exploreInitialLocations() {
    for (int i = 0; i < locations.length; i++) {
      /* Retrieves the pedestrian link the query point is situated on */
      ContinuousLink continuousLink = trace.getInitialLink(locations[i].getLinkId());
      statistic.log(continuousLink, Type.EXPLORED_CONTINUOUS_LINKS);
      double locationOffset = locations[i].getOffset();
      double distance;
      NodeBINE node;
      Offset offset;

      if (config.isIncoming()) {
        node = trace.getNode(continuousLink.getStartNodeId());
        distance = locationOffset / walkingSpeed;
        offset = new Offset(Math.max(0, locationOffset - maxDuration * walkingSpeed), locationOffset);
      } else {
        node = trace.getNode(continuousLink.getEndNodeId());
        distance = (continuousLink.getLength() - locationOffset) / walkingSpeed;
        offset = new Offset(locationOffset, Math.min(continuousLink.getLength(),
            Math.abs(continuousLink.getLength() - locationOffset - maxDuration * walkingSpeed)));
      }
      continuousLink.setOffset(offset);
      trace.output(continuousLink);

      if (distance <= maxDuration && distance < node.getDistance()) {
        node.setDistance(distance);
        updateQueue(node);
      }
    }
  }

  /*
   * Expands the passed node by exploring the links connected to it (except the outgoing bus links). In addition, links
   * and nodes that are no longer needed are removed from the trace and the link geometries written to the result table.
   */
  protected void expandNode(NodeBINE node) {
    statistic.log(Type.EXPANDED_NODES);
    node.setClosed();
    LinkCollection adjacentLinks = trace.getAdjacentLinkCollection(node);
    for (ContinuousLink link : adjacentLinks.getContinuousLinks()) {
      exploreContinuousLink(link, node);
    }
    if (adjacentLinks.sizeDiscreteLinks() > 0) {
      exploreDiscreteLinks(adjacentLinks, node);
    }

    if (node.isExpired()) {
      trace.removeNode(node);
    }
  }

  /**
   * <p>
   * Method exploreContinuousLink
   * </p>
   * 
   * @param link
   * @param node
   */
  protected void exploreContinuousLink(ContinuousLink link, INode node) {
    statistic.log(link, Type.EXPLORED_CONTINUOUS_LINKS);
    NodeBINE adjacentNode = trace.getNode(config.isIncoming() ? link.getStartNodeId() : link.getEndNodeId());
    adjacentNode.registerVisitedAdjacentLinks((short) 1);
    if (!adjacentNode.isClosed()) {
      double newDistance = node.getDistance() + link.getLength() / walkingSpeed;
      if (newDistance <= maxDuration && newDistance < adjacentNode.getDistance()) {
        adjacentNode.setDistance(newDistance);
        updateQueue(adjacentNode);
      }
    } else {
      if (adjacentNode.getId() != node.getId() && adjacentNode.isExpired()) {
        trace.removeNode(adjacentNode);
      }
    }
    double destinationOffset;
    if (config.isIncoming()) {
      destinationOffset = Math.max(0, link.getLength() - (maxDuration - node.getDistance()) * walkingSpeed);
      link.setOffset(new Offset(destinationOffset, link.getLength()));
    } else {
      double remainingDistance = maxDuration - node.getDistance() < 0 ? 0 : maxDuration - node.getDistance();
      destinationOffset = Math.min(link.getLength(), remainingDistance * walkingSpeed);
      link.setOffset(new Offset(0, destinationOffset));
    }
    trace.output(link);
  }

  /**
   * <p>
   * Method exploreDiscreteLinks
   * </p>
   * 
   * @param adjacentLinks
   * @param node
   */
  protected void exploreDiscreteLinks(LinkCollection adjacentLinks, NodeBINE node) {
    if (adjacentLinks.sizeDiscreteLinks() > 0) {
      Collection<DiscreteLink> discreteLinks = adjacentLinks.getDiscreteLinks();
      // case 1
      if (adjacentLinks.getAdjacentDiscreteNodeSize() == 1) {
        DiscreteLink link = discreteLinks.iterator().next();
        statistic.log(link, Type.EXPLORED_HOMO_DISCRETE_LINKS);
        NodeBINE adjacentNode = trace.getNode(config.isIncoming() ? link.getStartNodeId() : link.getEndNodeId());
        adjacentNode.registerVisitedAdjacentLinks((short) discreteLinks.size());
        if (!adjacentNode.isClosed()) {
          double newDistance = trace.getAdjacentNodeCost(node, adjacentNode, adjacentLinks.getRouteIds(),
              departureDateCodes, fromTimeInSecondsAfterMidnight, toTimeInSecondsAfterMidnight);
          if (newDistance <= maxDuration && newDistance < adjacentNode.getDistance()) {
            adjacentNode.setDistance(newDistance);
            updateQueue(adjacentNode);
          }
        } else {
          if (adjacentNode.isExpired()) {
            trace.removeNode(adjacentNode);
          }
        }
      } else { // case 2: links have at least 2 different adjacent nodes
        statistic.log(Type.EXPLORED_HETERO_DISCRETE_LINKS);
        HashMap<Integer, Set<Short>> routesPerNode = new HashMap<Integer, Set<Short>>();
        for (DiscreteLink discreteLink : discreteLinks) {
          int adjNodeId = config.isIncoming() ? discreteLink.getStartNodeId() : discreteLink.getEndNodeId();
          if (!routesPerNode.containsKey(adjNodeId)) {
            routesPerNode.put(adjNodeId, new HashSet<Short>());
          }
          routesPerNode.get(adjNodeId).add(discreteLink.getRouteId());
        }
        Set<Short> queriedRouteIds =  new HashSet<Short>();
        Set<NodeBINE> queriedNodes = new HashSet<NodeBINE>();
        // registering and remove routes that should not be queried
        for (Integer adjacentNodeId : routesPerNode.keySet()) {
          NodeBINE adjacentNode = trace.getNode(adjacentNodeId);
          adjacentNode.registerVisitedAdjacentLinks((short) routesPerNode.get(adjacentNodeId).size());
          statistic.logExploredNode(adjacentNode.getId());
          if (!adjacentNode.isClosed()) {
            queriedNodes.add(adjacentNode);
            queriedRouteIds.addAll(routesPerNode.get(adjacentNodeId));
          }
        }
        if (!queriedRouteIds.isEmpty()) {
          Map<Integer, Double> ajacentNodesCost = trace.getAdjacentNodesCost(node, queriedRouteIds, departureDateCodes,
              fromTimeInSecondsAfterMidnight, toTimeInSecondsAfterMidnight);
          for (Integer adjacentNodeId : ajacentNodesCost.keySet()) {
            NodeBINE adjacentNode = trace.getNode(adjacentNodeId);
            if (!adjacentNode.isClosed()) {
              double newDistance = ajacentNodesCost.get(adjacentNodeId);
              if (newDistance <= maxDuration && newDistance < adjacentNode.getDistance()) {
                adjacentNode.setDistance(newDistance);
                updateQueue(adjacentNode);
              }
            } else {
              //System.out.println("Node " + adjacentNode.getId() + " was closed.");
            }
          }
        }
        for (Integer adjacentNodeId : routesPerNode.keySet()) {
          NodeBINE adjacentNode = trace.getNode(adjacentNodeId);
          if (adjacentNode.isClosed() && adjacentNode.isExpired()) {
            trace.removeNode(adjacentNode);
          }
        }
      }
    }
  }

  /*
   * Updates the priority queue.
   */
  protected void updateQueue(NodeBINE node) {
    priorityQueue.remove(node);
    priorityQueue.offer(node);
  }

  /*
   * (non-Javadoc)
   * @see isochrones.algorithm.AbstractIsochrone#terminate()
   */
  @Override
  public void terminate() {
    statistic.setSizeValues(maxTraceSize, maxPrioQueueSize);
    if (outputWriting) {
      trace.terminate();
    }
  }
}
