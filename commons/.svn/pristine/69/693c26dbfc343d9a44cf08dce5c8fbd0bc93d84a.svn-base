/**
 * 
 */
package isochrones.algorithm.datastructure;

import isochrones.algorithm.Mode;
import isochrones.algorithm.statistics.DBType;
import isochrones.algorithm.statistics.DBWType;
import isochrones.algorithm.statistics.Statistic;
import isochrones.algorithm.statistics.Type;
import isochrones.db.DBResult;
import isochrones.db.IsochroneQuery;
import isochrones.network.Offset;
import isochrones.network.link.ContinuousLink;
import isochrones.network.link.DiscreteLink;
import isochrones.network.link.ILink;
import isochrones.network.link.LinkCollection;
import isochrones.network.node.Entity;
import isochrones.network.node.INode;
import isochrones.utils.Config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * <p>
 * The <code>ITrace</code> class
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
public abstract class AbstractTrace {

  protected IsochroneQuery query;
  
  // Map<Integer, Integer> scheduleCounter = new HashMap<Integer, Integer>();

  // protected int batchSize = Integer.parseInt(Config.getProperty("sql.numberOfBatchInserts"));
  protected Statistic statistic;
  // private String linkTable;
  // protected TableEntry isoLinkEntry;
  // protected boolean outputWriting = false;
  // protected Set<ILink> storableLinks = new HashSet<ILink>();
  protected Map<Integer, ILink> storableLinks = new HashMap<Integer, ILink>();
  protected Config config;
  protected static final Logger LOGGER = Logger.getLogger(AbstractTrace.class.getPackage().getName());
  protected DateFormat dateFormat = new SimpleDateFormat("yyyy'-'MM'-'dd'-'HH':'mm':'ss");

  protected int[] queryPointIds;

  // protected double distance;

  protected Map<Integer, TreeMap<Double, Entity>> entitiesOnEdge;

  protected AbstractTrace(Config config, IsochroneQuery query, Statistic statistic) {
    this.config = config;
    this.query = query;
    this.statistic = statistic;
    if (config.isOutputWriting()) {
      query.clearResultTable();
      query.controlDestinationIndex(true);
    }
    if (config.isKNN()) {
      loadEntities();
    }
  }

  private void loadEntities() {
    entitiesOnEdge = new HashMap<Integer, TreeMap<Double, Entity>>();
    DBResult dbResult = null;
    try {
      // long time = System.currentTimeMillis(); // Counter
      dbResult = query.loadEntities();
      ResultSet resultSet = dbResult.getResultSet();
      while (resultSet.next()) {
        int edgeId = resultSet.getInt("EDGE_ID");
        int entityId = resultSet.getInt("ENTITY_ID");
        double offset = resultSet.getInt("START_OFFSET");
        if (!entitiesOnEdge.containsKey(edgeId)) {
          entitiesOnEdge.put(edgeId, new TreeMap<Double, Entity>());
        }
        entitiesOnEdge.get(edgeId).put(offset, new Entity(entityId));
      }
      // statistic.logRuntime(DBType.GET_DISCRETE_HOMO_COST, System.currentTimeMillis() - time);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      dbResult.close();
    }
  }

  protected Mode getMode() {
    return config.getMode();
  }
  
  /**
   * 
   * <p>Method findEntities</p>
   * @param edgeId the id of the edge on which to find the intersecting entities
   * @return a sorted map having as key the start offset and a value the entity
   */
  public TreeMap<Double,Entity> findEntities(int edgeId) {
    return entitiesOnEdge.get(edgeId);
  }

  /**
   * <p>
   * Method setQueryPoint
   * </p>
   * 
   * @param nodeId
   */
  public void setQueryPoint(int[] nodeId) {
    this.queryPointIds = nodeId;
  }

  /**
   * <p>
   * Method getStatistic
   * </p>
   * 
   * @return
   */
  public Statistic getStatistic() {
    return statistic;
  }

  /**
   * Returns the node specified by the passed <code>nodeId</code>.
   * 
   * @param nodeId the ID of the requested node
   * @return the node specified by the passed node ID
   */
  public abstract INode getNode(int nodeId);
  
  /**
   * Returns the continuous link specified by the passed link ID. It takes the link from the trace if it is already
   * there. Otherwise, the new pedestrian link is obtained from the database.
   * 
   * @param linkId the ID of the requested pedestrian link
   * @return the continuous link specified by the passed link ID
   */
  public abstract ILink getInitialLink(int linkId);

  /**
   * <p>
   * Method getInitialNode
   * </p>
   * returns the link instance with the corresponding id
   * 
   * @param linkId
   * @return
   */
  public abstract INode getInitialNode(int nodeId);

  /**
   * <p>
   * Method getAdjacentContinuousLinks
   * </p>
   * 
   * @param nodeId
   * @return
   */
  protected abstract Collection<ILink> getAdjacentContinuousLinks(INode node);

  /**
   * <p>
   * Method getIncidentLinks
   * </p>
   * 
   * @param nodeId
   * @return
   */
  protected abstract Collection<ILink> getAllAdjacentLinks(INode node);

  /**
   * <p>
   * Method getAdjacentLinks
   * </p>
   * returns all adjacent links of the current node
   * 
   * @param nodeId the node id from which to find the ajacent links
   * @return a set of links
   */
  public Collection<ILink> getAdjacentLinks(INode node) {
    Collection<ILink> adjLinks;
    if (getMode() == Mode.UNIMODAL) {
      adjLinks = getAdjacentContinuousLinks(node);
    } else {
      adjLinks = getAllAdjacentLinks(node);
    }

    return adjLinks;
  }

  /**
   * <p>
   * Method getAdjacentLinkCollection
   * </p>
   * returns the incoming link. Be aware if a node has incoming links, but their source vertex of a discrete edge is
   * closed, it will not be part of the result
   * 
   * @param node
   * @return
   */
  public LinkCollection getAdjacentLinkCollection(INode node) {
    LinkCollection linkCollection = new LinkCollection();
    for (ILink link : getAdjacentLinks(node)) {
      if (link instanceof ContinuousLink)
        linkCollection.add((ContinuousLink) link);
      else {
        linkCollection.add((DiscreteLink) link,config.isIncoming());
      }
    }
    return linkCollection;
  }

  /**
   * <p>
   * Method getDateCodes
   * </p>
   * returns the date code of the given day. In case of empty result it changes to the pedestrian mode
   * 
   * @param targetTime
   * @return
   */
  public Set<Integer> getDateCodes(Calendar targetTime) {
    HashSet<Integer> dateCodes = new HashSet<Integer>();
    DBResult dbResult = null;
    try {
      long start = System.currentTimeMillis();
      dbResult = query.getDateCodes(targetTime);
      ResultSet resultSet = dbResult.getResultSet();
      while (resultSet.next()) {
        dateCodes.add(resultSet.getInt("service_id"));
      }
      if (dateCodes.isEmpty()) {
        config.setMode(Mode.UNIMODAL);
        statistic.setMode(getMode());
        LOGGER.warning("No schedules found for the " + "selected day: " + dateFormat.format(targetTime.getTime())
            + "\nSwitching to WALKING mode!");
      }
      try {
        assert config.isDebug() : statistic.logRuntime(DBType.GET_CALENDAR_DATE, System.currentTimeMillis() - start);
      } catch (AssertionError e) {
        System.out.println("Statistic disabled!!");
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      dbResult.close();
    }
    return dateCodes;
  }

  /**
   * Stores the passed <code>link</code> in the database. If the passed link is a continuous link and the mode
   * outputWriting is enabled, the link geometry is written to the result table, and the link is removed from the trace
   * afterwards. Otherwise, the link is removed from the trace only.
   * 
   * @param link the link that has to be removed
   */
  public void output(ILink link) {
    if (config.isOutputWriting()) {
      Offset offset = link.getOffset();
      if (offset != null) {
        if (storableLinks.containsKey(link.getId())) {

          ILink linkOld = storableLinks.get(link.getId());
          Offset oldOffset = linkOld.getOffset();
          if (oldOffset.getStartOffset() < offset.getStartOffset()) {
            if (oldOffset.getEndOffset() >= offset.getStartOffset()) {
              // merge
              linkOld.setOffset(new Offset(oldOffset.getStartOffset(), offset.getEndOffset()));
            }
          } else {
            // override
            storableLinks.put(link.getId(), link);
          }
        } else {
          storableLinks.put(link.getId(), link); //
        }
        if (storableLinks.size() >= config.getFlushLimit()) {
          long time1 = System.currentTimeMillis(); // Counter
          query.storeLinks(storableLinks.values());
          statistic.logRuntime(DBWType.STORE_CONTINUOUS_LINKS, System.currentTimeMillis() - time1);
          storableLinks.clear();
        }
      }
    }
  }
  
  public void storeEntities(Collection<Entity> entities) {
    query.storeEntities(entities);
  }


  /**
   * <p>
   * Method terminate
   * </p>
   * writes the remaining storable links into the db and enables the index
   * 
   * @throws SQLException
   */
  public void terminate() {
    if (storableLinks.size() > 0) {
      long time1 = System.currentTimeMillis(); // Counter
      query.storeLinks(storableLinks.values());
      statistic.logRuntime(DBWType.STORE_CONTINUOUS_LINKS, System.currentTimeMillis() - time1);
      storableLinks.clear();
    }
    if (config.isOutputWriting()) {
      query.controlDestinationIndex(false);
    }
    statistic.log(Type.REMAINING_NODES, getNodeSize());
  }

  /**
   * <p>
   * Method getIncomingNodeCost
   * </p>
   * computes the cost of the incoming node over a schedule-based network by visiting exact one edge
   * 
   * @param node
   * @param adjacentNode
   * @param routeIds
   * @param dateCodes
   * @param fromTime
   * @param toTime
   * @return
   */
  public double getAdjacentNodeCost(INode node, INode adjacentNode, Set<Short> routeIds, Set<Integer> dateCodes,
                                    long fromTime, long toTime) {
    /*
    if(!scheduleCounter.containsKey(node.getId())) {
      scheduleCounter.put(node.getId(),0);
    }
    scheduleCounter.put(node.getId(),  scheduleCounter.get(node.getId())+1);
    */
    double distance = INode.Value.INFINITY;
    DBResult dbResult = null;
    try {
      long time = System.currentTimeMillis(); // Counter
      if (config.isIncoming()) {
        dbResult = query.getLatestDepartureTime(adjacentNode.getId(),node.getId(),routeIds,dateCodes,fromTime, Math.round(toTime - node.getDistance()));
        ResultSet resultSet = dbResult.getResultSet();
        if (resultSet.next()) {
          distance = toTime - resultSet.getLong("CLOSTEST_TIME");
        }
      } else {
        dbResult = query.getEarliestArrivalTime(node.getId(), adjacentNode.getId(),routeIds,dateCodes,Math.round(fromTime + node.getDistance()), toTime);
        ResultSet resultSet = dbResult.getResultSet();
        if (resultSet.next()) {
          distance = resultSet.getLong("CLOSTEST_TIME") - fromTime;
        }
      }

      statistic.logRuntime(DBType.GET_DISCRETE_HOMO_COST, System.currentTimeMillis() - time);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      dbResult.close();
    }
    return distance;
  }

  /**
   * <p>
   * Method getIncomingNodesCost
   * </p>
   * computes the costs of every source vertex having in common the target vertex
   * 
   * @param node
   * @param sourceNodeIds
   * @param departureDateCodes
   * @param fromTime
   * @param toTime
   * @return
   */
  public Map<Integer, Double> getAdjacentNodesCost(INode node, Set<Short> routeIds, Set<Integer> departureDateCodes,
                                                   long fromTime, long toTime) {
    /*
    if(!scheduleCounter.containsKey(node.getId())) {
      scheduleCounter.put(node.getId(),0);
    }
    scheduleCounter.put(node.getId(),  scheduleCounter.get(node.getId())+1);
    */

    HashMap<Integer, Double> nodeDistances = new HashMap<Integer, Double>();

    DBResult dbResult = null;
    try {
      long time = System.currentTimeMillis(); // Counter
      if (config.isIncoming()) {
        dbResult = query.getLatestDepartureTimes(node.getId(), routeIds, departureDateCodes, fromTime,
            Math.round(toTime - node.getDistance()));
        ResultSet resultSet = dbResult.getResultSet();
        while (resultSet.next()) {
          nodeDistances.put(resultSet.getInt("SOURCE"), (double) (toTime - resultSet.getLong("CLOSTEST_TIME")));
        }
      } else {
        dbResult = query.getEarliestArrivalTimes(node.getId(), routeIds, departureDateCodes,
            fromTime + Math.round(node.getDistance()), toTime);
        ResultSet resultSet = dbResult.getResultSet();
        while (resultSet.next()) {
          nodeDistances.put(resultSet.getInt("TARGET"), (double) (resultSet.getLong("CLOSTEST_TIME") - fromTime));
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

  /**
   * <p>
   * Method size
   * </p>
   * returns the number of elements in the trace
   * 
   * @return
   */
  public final int size() {
    return getNodeSize() + getLinkSize();
  }

  /**
   * <p>
   * Method getNodeSize
   * </p>
   * 
   * @return
   */
  public abstract int getNodeSize();

  /**
   * <p>
   * Method getLinkSize
   * </p>
   * returns the number of links
   * 
   * @return
   */
  public abstract int getLinkSize();

  /**
   * <p>
   * Method getContinuousLinkSize
   * </p>
   * 
   * @return
   */
  public abstract int getContinuousLinkSize();

  /**
   * <p>
   * Method getDiscreteLinkSize
   * </p>
   * 
   * @return
   */
  public abstract int getDiscreteLinkSize();
  
  /*
  public Map<Integer, Integer> getScheduleCounter() {
    return scheduleCounter;
  }
  */

}
