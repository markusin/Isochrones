package isochrones.web.minex.algorithm;

import isochrones.algorithm.Isochrone;
import isochrones.algorithm.Mode;
import isochrones.algorithm.datastructure.AbstractTrace;
import isochrones.algorithm.statistics.Statistic;
import isochrones.algorithm.statistics.Type;
import isochrones.db.DBVendor;
import isochrones.network.Location;
import isochrones.network.Offset;
import isochrones.network.link.ContinuousLink;
import isochrones.network.link.DiscreteLink;
import isochrones.network.link.LinkCollection;
import isochrones.web.JSON;
import isochrones.web.config.Config;
import isochrones.web.db.OracleQuery;
import isochrones.web.db.PostgresQuery;
import isochrones.web.minex.datastructures.Trace;
import isochrones.web.network.node.QueryPoint;
import isochrones.web.network.node.WebNode;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.cometd.bayeux.server.ServerSession;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p>
 * The <code>MineX</code> class
 * </p>
 * <p>
 * Copyright: 2006 - 2010 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a>
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
public class MineX extends Isochrone {

  /* Trace ---------------------------------------------------------------- */
  protected PriorityQueue<WebNode> priorityQueue;
  Trace trace;

  double[] notificationCheckpoints;
  long totalComputationTime, timePopulatedClosestLinks;
  private ServerSession serverSession, remoteSession;
  private String channel;

  /**
   * <p>
   * Constructs a(n) <code>MineX</code> object.
   * </p>
   */
  public MineX(Config config, ServerSession serverSession, ServerSession remoteSession, String channel) {
    super(config, config.getDbVendor().equals(DBVendor.ORACLE) ? new OracleQuery(config, true)
        : new PostgresQuery(config, true), new Statistic(config.getAlgorithmName(), config.getMode()));

    priorityQueue = new PriorityQueue<WebNode>();
    this.serverSession = serverSession;
    this.remoteSession = remoteSession;
    this.channel = channel;
    trace = new Trace(config, query, getStatistic());
  }

  @Override
  protected void setParameters(int duration, double walkingSpeed, Calendar targetTime, AbstractTrace trace) {
    super.setParameters(duration, walkingSpeed, targetTime, trace);
    int progressBarSize = Integer.parseInt(config.getProperty("client.progressbar.size"));
    notificationCheckpoints = new double[progressBarSize / 5];
    for (int i = 0; i < notificationCheckpoints.length; i++) {
      notificationCheckpoints[i] = ((double) (i + 1)) / 20 * duration;
    }
  }

  /**
   * <p>
   * Method computeIsochrone
   * </p>
   * 
   * @param qPoints
   * @param duration
   * @param walkingSpeed
   * @param targetTime
   * @param mode
   * @param clientSRID
   * @param resultSet
   * @return
   */
  public void computeIsochrone(Set<QueryPoint> qPoints, int duration, double walkingSpeed, Calendar targetTime,
                               Mode mode) {

    long start = System.currentTimeMillis();
    // project query points on edges
    Set<Location> locations = new HashSet<Location>();
    for (QueryPoint qPoint : qPoints) {
      locations.addAll(trace.projectOnLinks(qPoint));
    }
    timePopulatedClosestLinks = System.currentTimeMillis() - start;
    computeIsochrone(locations.toArray(new Location[locations.size()]), duration, walkingSpeed, targetTime);

    JSONObject root = new JSONObject();
    try {
      totalComputationTime = System.currentTimeMillis() - start;
      JSONObject jsonLoggingObj = new JSONObject();
      jsonLoggingObj.put("timeQuerypointProjection", timePopulatedClosestLinks);
      jsonLoggingObj.put("totalComputationTimeServer", totalComputationTime);
      root.put(JSON.BBOX, trace.getBoundingBox().toJSON());
      root.put(JSON.LOGGING, jsonLoggingObj);
      deliver(root);
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime) {
    setParameters(locations, duration, walkingSpeed, targetTime, trace);
    exploreInitialLocations();
    compute();
    terminate();
  }

  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime) {
    setParameters(duration, walkingSpeed, targetTime, trace);
    exploreInitialNodes(nodeIds);
    compute();
    terminate();
  }

  @Override
  protected void exploreInitialNodes(int[] nodeIds) {
    for (int nodeId : nodeIds) {
      WebNode node = trace.getInitialNode(nodeId);
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
      WebNode node;
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

  @Override
  public void compute() {
    WebNode node = priorityQueue.poll(); // Dequeues the first node from priority queue
    int i = 1;
    while (node != null) {
      expandNode(node);
      if (i < notificationCheckpoints.length && node.getDistance() > notificationCheckpoints[i]) {
        JSONObject jsonObject = new JSONObject();
        try {
          jsonObject.put("currentDistance", notificationCheckpoints[i++]);
        } catch (JSONException e1) {
          e1.printStackTrace();
        }
        deliver(jsonObject);
      }
      node = priorityQueue.poll(); // Dequeues the next node
    }
  }

  /*
   * Expands the passed node by exploring the links connected to it (except the outgoing bus links). In addition, links
   * and nodes that are no longer needed are removed from the trace and the link geometries written to the result table.
   */
  protected void expandNode(WebNode node) {
    statistic.log(Type.EXPANDED_NODES);
    node.setClosed();
    LinkCollection adjacentLinks = trace.getAdjacentLinkCollection(node);
    for (ContinuousLink link : adjacentLinks.getContinuousLinks()) {
      exploreContinuousLink(link, node);
    }
    if (adjacentLinks.sizeDiscreteLinks() > 0) {
      exploreDiscreteLinks(adjacentLinks, node);
    }

    /*
     * Removes the passed node if expanded and reached through all outgoing links.
     */
    if (node.isExpired()) {
      trace.removeNode(node);
    }
  }

  protected void exploreContinuousLink(ContinuousLink link, WebNode node) {
    statistic.log(link, Type.EXPLORED_CONTINUOUS_LINKS);
    WebNode adjacentNode = trace.getNode(config.isIncoming() ? link.getStartNodeId() : link.getEndNodeId());
    adjacentNode.registerVisitedAdjacentLinks((short)1);

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

  protected void exploreDiscreteLinks(LinkCollection adjacentLinks, WebNode node) {
    if (adjacentLinks.sizeDiscreteLinks() > 0) {
      Collection<DiscreteLink> discreteLinks = adjacentLinks.getDiscreteLinks();
      // case 1: all links have the same adjacent node
      if (adjacentLinks.getAdjacentDiscreteNodeSize() == 1) {
        DiscreteLink link = discreteLinks.iterator().next();
        WebNode adjacentNode = trace.getNode(config.isIncoming() ? link.getStartNodeId() : link.getEndNodeId());
        adjacentNode.registerVisitedAdjacentLinks((short) discreteLinks.size());
        if (!adjacentNode.isClosed()) {
          double newDistance = trace.getAdjacentNodeCost(node, adjacentNode, adjacentLinks.getRouteIds(),
              departureDateCodes, fromTimeInSecondsAfterMidnight, toTimeInSecondsAfterMidnight);
          statistic.log(link, Type.EXPLORED_HOMO_DISCRETE_LINKS);
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
        HashMap<Integer, Short> nodesWithLinkCounter = new HashMap<Integer, Short>();
        for (DiscreteLink discreteLink : discreteLinks) {
          int sourceNodeId = config.isIncoming() ? discreteLink.getStartNodeId() : discreteLink.getEndNodeId();
          if (!nodesWithLinkCounter.containsKey(sourceNodeId)) {
            nodesWithLinkCounter.put(sourceNodeId, (short) 1);
          } else {
            nodesWithLinkCounter.put(sourceNodeId, (short) (nodesWithLinkCounter.get(sourceNodeId) + 1));
          }
        }
        statistic.log(Type.EXPLORED_HETERO_DISCRETE_LINKS);
        Map<Integer, Double> ajacentNodesCost = trace.getAdjacentNodesCost(node, adjacentLinks.getRouteIds(),
            departureDateCodes, fromTimeInSecondsAfterMidnight, toTimeInSecondsAfterMidnight);
        for (Integer adjacentNodeId : ajacentNodesCost.keySet()) {
          WebNode adjacentNode = trace.getNode(adjacentNodeId);
          adjacentNode.registerVisitedAdjacentLinks(nodesWithLinkCounter.get(adjacentNodeId));
          if (!adjacentNode.isClosed()) {
            double newDistance = ajacentNodesCost.get(adjacentNodeId);
            if (newDistance <= maxDuration && newDistance < adjacentNode.getDistance()) {
              adjacentNode.setDistance(newDistance);
              updateQueue(adjacentNode);
            }
          } else {
            if (adjacentNode.isExpired()) {
              trace.removeNode(adjacentNode);
            }
          }
        }
      }
    }
  }

  /*
   * Updates the priority queue.
   */
  private void updateQueue(WebNode node) {
    priorityQueue.remove(node);
    priorityQueue.offer(node);
  }

  public int[] getStatistics() {
    int[] statistics = new int[2];

    statistics[0] = maxTraceSize;
    statistics[1] = maxPrioQueueSize;

    return statistics;
  }

  /**
   * <p>
   * Method getRuntimeDataJSON
   * </p>
   * 
   * @return
   */
  public JSONObject appendLoggingData(JSONObject jsonObject) {
    trace.appendLoggingData(jsonObject);
    try {
      jsonObject.put("timeQuerypointProjection", timePopulatedClosestLinks);
      // jsonObject.put("timeScheduleLookup", timeComputeCosts);
      jsonObject.put("totalComputationTimeServer", totalComputationTime);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  @Override
  public void terminate() {
    trace.terminate();
  }

  public void deliver(JSONObject jsonObj) {
    remoteSession.deliver(serverSession, channel, jsonObj.toString(), null);
  }

}
