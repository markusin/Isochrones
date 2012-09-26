package isochrones.mrnex.algorithm;

import isochrones.algorithm.Isochrone;
import isochrones.algorithm.statistics.Type;
import isochrones.db.DBVendor;
import isochrones.mrnex.algorithm.datastructure.Trace;
import isochrones.mrnex.algorithm.statistics.MIERWINEStatistic;
import isochrones.mrnex.db.OracleQuery;
import isochrones.mrnex.db.PostgresQuery;
import isochrones.mrnex.network.node.ANode;
import isochrones.mrnex.utils.RangeUtil;
import isochrones.network.Location;
import isochrones.network.Offset;
import isochrones.network.link.ContinuousLink;
import isochrones.network.link.DiscreteLink;
import isochrones.network.link.LinkCollection;
import isochrones.utils.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class MRNEX extends Isochrone {

  /* Trace ---------------------------------------------------------------- */
  protected PriorityQueue<ANode> priorityQueue;
  protected Trace trace;
  /* The map containing all nodes whose data are loaded in a range search */
  protected Map<Integer, ANode> loadedIERNodes = new HashMap<Integer, ANode>();

  /**
   * <p>
   * Constructs a(n) <code>MRNEX</code> object.
   * </p>
   * 
   * @param config
   */
  public MRNEX(Config config) {
    super(config, config.getDbVendor().equals(DBVendor.ORACLE) ? new OracleQuery(config) : new PostgresQuery(config),
          new MIERWINEStatistic(config.getAlgorithmName(), config.getMode()));
    priorityQueue = new PriorityQueue<ANode>();
    trace = new Trace(config, query, statistic);
  }

  @Override
  public MIERWINEStatistic getStatistic() {
    return (MIERWINEStatistic) statistic;
  }

  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime) {
    setParameters(locations, duration, walkingSpeed, targetTime, trace);
    trace.setParameters(walkingSpeed, maxDuration);
    exploreInitialLocations();
    compute();
  }

  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime) {
    setParameters(duration, walkingSpeed, targetTime, trace);
    trace.setParameters(walkingSpeed, maxDuration);
    exploreInitialNodes(nodeIds);
    compute();
  }

  @Override
  public void compute() {
    ANode node = priorityQueue.poll(); // Dequeues the first node from priority queue
    while (node != null) {
      expandNode(node);
      node = priorityQueue.poll(); // Dequeues the next node
    }
    terminate();
  }

  @Override
  protected void exploreInitialLocations() {
    for (int i = 0; i < locations.length; i++) {
      ContinuousLink continuousLink = trace.getInitialLink(locations[i].getLinkId());
      statistic.log(continuousLink, Type.EXPLORED_CONTINUOUS_LINKS);
      double locationOffset = locations[i].getOffset();
      double distance;
      ANode node;
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

  protected void exploreInitialNodes(int[] nodeIds) {
    for (int nodeId : nodeIds) {
      ANode node = trace.getInitialNode(nodeId);
      node.setDistance(0);
      node.setRadius(node.getRemainingDistance(maxDuration, walkingSpeed));
      updateQueue(node);
    }
  }

  protected void expandNode(ANode node) {
    if (config.isDebug()) {
      // ((IsochroneQuery) trace.getQuery()).logVertices(node.getId(), node.getDistance());
    }
    statistic.log(Type.EXPANDED_NODES);
    LinkCollection incomingLinks = trace.getAdjacentLinkCollection(node);
    if (incomingLinks.isEmpty()) { // node becomes stalled
      
      if (node.getCoordinate() == null) {
        node.setCoordinate(trace.getCoordinate(node.getId())); // db lookup
      }
      if(trace.unlimitedMemory()){
        node.setRadius(node.getRemainingDistance(maxDuration, walkingSpeed));
      } else {
          int reservedMemorySize = trace.getAvailableMemorySize(loadedIERNodes.isEmpty() ? 0.5f : 1f);
          double eDist = trace.getRange(node.getId(),reservedMemorySize);  // db lookup
          node.setRadius(Math.min(node.getRemainingDistance(maxDuration, walkingSpeed),eDist));
      }
      List<ANode> intersections = new ArrayList<ANode>();
      // check if there is an intersection between stalled vertex and each vertex loaded with a range query.
      // if this is the case, than add the loaded vertex to the set of intersections
      for (ANode loadedNode : loadedIERNodes.values()) {
        if (RangeUtil.euclideanDistance(node.getCoordinate(), loadedNode.getCoordinate()) < node.getRadius()
            + loadedNode.getRadius()) {
          intersections.add(loadedNode);
        }
      }
      trace.loadLinksFromIER(node, intersections);  // db lookup
      loadedIERNodes.put(node.getId(), node);
      incomingLinks = trace.getAdjacentLinkCollection(node);
    }
    node.setClosed();
    for (ContinuousLink link : incomingLinks.getContinuousLinks()) {
      exploreContinuousLink(link, node);
    }
    if (incomingLinks.sizeDiscreteLinks() > 0) {
      exploreDiscreteLinks(incomingLinks, node);
    }

    if (node.isExpired()) {
      trace.removeNode(node);
    }
  }

  protected void exploreContinuousLink(ContinuousLink link, ANode node) {
    statistic.log(link, Type.EXPLORED_CONTINUOUS_LINKS);
    ANode adjacentNode = trace.getNode(config.isIncoming() ? link.getStartNodeId() : link.getEndNodeId());
    adjacentNode.registerVisitedAdjacentLinks((short) 1);

    if (!adjacentNode.isClosed()) {
      double newDistance = node.getDistance() + link.getLength() / walkingSpeed;
      if (newDistance <= maxDuration && newDistance < adjacentNode.getDistance()) {
        adjacentNode.setDistance(newDistance);
        updateQueue(adjacentNode);
      }
    } else {
      if (adjacentNode.getId() != node.getId() && adjacentNode.isExpired()) {
        // Checks for loops (same node on both ends)
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

  protected void exploreDiscreteLinks(LinkCollection adjacentLinks, ANode node) {
    if (adjacentLinks.sizeDiscreteLinks() > 0) {
      Collection<DiscreteLink> discreteLinks = adjacentLinks.getDiscreteLinks();
      // case 1: all links have the same adjacent node
      if (adjacentLinks.getAdjacentDiscreteNodeSize() == 1) {
        DiscreteLink link = discreteLinks.iterator().next();
        ANode adjacentNode = trace.getNode(config.isIncoming() ? link.getStartNodeId() : link.getEndNodeId());
        adjacentNode.registerVisitedAdjacentLinks((short) discreteLinks.size());
        statistic.log(link, Type.EXPLORED_HOMO_DISCRETE_LINKS);
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
        Set<Short> queriedRouteIds = new HashSet<Short>();
        Set<ANode> queriedNodes = new HashSet<ANode>();
        // registering and remove routes that should not be queried
        for (Integer adjacentNodeId : routesPerNode.keySet()) {
          ANode adjacentNode = trace.getNode(adjacentNodeId);
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
            ANode adjacentNode = trace.getNode(adjacentNodeId);
            if (!adjacentNode.isClosed()) {
              double newDistance = ajacentNodesCost.get(adjacentNodeId);
              if (newDistance <= maxDuration && newDistance < adjacentNode.getDistance()) {
                adjacentNode.setDistance(newDistance);
                updateQueue(adjacentNode);
              }
            } else {
              // System.out.println("Node " + adjacentNode.getId() + " was closed.");
            }
          }
        }
        for (Integer adjacentNodeId : routesPerNode.keySet()) {
          ANode adjacentNode = trace.getNode(adjacentNodeId);
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
  protected void updateQueue(ANode node) {
    priorityQueue.remove(node);
    priorityQueue.offer(node);
  }

  @Override
  public void terminate() {
    // System.err.println("Loaded continuous nodes from point query: " + loadedContinuousFromCNodeWithPointQuery);
    // System.out.println("Loaded discrete nodes from point query: " + loadedContinuousFromDNodeWithPointQuery);
    // System.out.println("Final node size :" + trace.getNodeSize());
    statistic.setSizeValues(maxTraceSize, maxPrioQueueSize);
    if (outputWriting) {
      trace.terminate();
    }
  }

}