package isochrones.mrnex.algorithm.datastructure;

import isochrones.algorithm.Mode;
import isochrones.algorithm.datastructure.AbstractTrace;
import isochrones.algorithm.statistics.DBType;
import isochrones.algorithm.statistics.Statistic;
import isochrones.algorithm.statistics.Type;
import isochrones.db.DBResult;
import isochrones.db.IsochroneQuery;
import isochrones.mrnex.algorithm.statistics.MIERWINEStatistic;
import isochrones.mrnex.db.IQuery;
import isochrones.mrnex.network.node.ANode;
import isochrones.network.GeoPoint;
import isochrones.network.GeoQueryPoint;
import isochrones.network.NWMode;
import isochrones.network.NoSuchLinkException;
import isochrones.network.link.ContinuousLink;
import isochrones.network.link.DiscreteLink;
import isochrones.network.link.ILink;
import isochrones.network.link.LinkCollection;
import isochrones.network.node.INode;
import isochrones.utils.Config;
import isochrones.utils.DBUtility;
import isochrones.utils.MathUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The <code>Trace</code> class represents a bi-modal network trace used for the computation of isochrones. The trace
 * consists of merged pedestrian and bus networks. <code>Trace</code> allows to store nodes which can be connected to
 * pedestrian and bus links.
 * 
 * @author Markus Innerebner
 * @version 1.0
 */
public class Trace extends AbstractTrace {
  
  public static final int EMPTY_MEMORY = 0;

  /* Data structures used by the trace ------------------------------------ */
  private Map<Integer, ANode> nodes;
  private Map<Integer, ILink> links;
  private Map<Integer, Set<ILink>> adjList;
  // private int totalFetched = 0;
  private double walkingSpeed;
  private int maxDuration;
  private int maxMemorySize = Integer.MIN_VALUE;
  private int minFreeMemorySize;
  private int maxNodeSize = Integer.MIN_VALUE;
  private boolean unlimitedMemory;

  private Map<Integer, Integer> nodeGeoLookup = new HashMap<Integer, Integer>();

  // logging
  private Set<Integer> removedNodeIds = new HashSet<Integer>();

  /**
   * Class Constructor that creates a trace of the specified <code>type
   * </code>. Should be invoked, when result is not written into DB
   * 
   * @param type the type of trace that will be created
   */
  public Trace(Config config, IsochroneQuery query, Statistic statistic) {
    super(config, query, statistic);
    maxMemorySize = config.getMaxMemorySize();
    minFreeMemorySize = (int) (maxMemorySize *0.1);
    if (maxMemorySize == Integer.MAX_VALUE) {
      unlimitedMemory = true;
    }
    nodes = new HashMap<Integer, ANode>();
    links = new HashMap<Integer, ILink>();
    adjList = new HashMap<Integer, Set<ILink>>();
    if (config.isDebug()) {
      DBUtility.truncateTable(config.getConnection(), config.getProperty("tbl.log_circles"));
      DBUtility.truncateTable(config.getConnection(), config.getProperty("tbl.log_nodes"));
      // DBUtility.truncateTable(config.getConnection(), config.getProperty("tbl.log_vertex"));
    }
  }

  /**
   * <p>
   * Method setParameters
   * </p>
   * 
   * @param walkingSpeed
   * @param maxDuration
   */
  public void setParameters(double walkingSpeed, int maxDuration) {
    this.walkingSpeed = walkingSpeed;
    this.maxDuration = maxDuration;
  }

  @Override
  public ANode getInitialNode(int nodeId) {
    if (getMode().equals(Mode.MULTIMODAL)) {
      DBResult dbResult = null;
      try {
        dbResult = query.getNode(nodeId);
        ResultSet rSet = dbResult.getResultSet();
        if (rSet.next()) {
          ANode node = new ANode(nodeId, rSet.getShort("DEGREE"));
          node.setCoordinate(new GeoPoint(rSet.getDouble("X"), rSet.getDouble("Y")));
          addNode(node);
        }
      } catch (SQLException e) {
        e.printStackTrace();
      } finally {
        if (dbResult != null)
          dbResult.close();
      }
    } else {
      addNode(nodeId, NWMode.CONTINUOUS);
    }
    return nodes.get(nodeId);

  }

  @Override
  public ContinuousLink getInitialLink(int linkId) {
    ContinuousLink link = (ContinuousLink) links.get(linkId);
    if (link == null) {
      DBResult dbResult = null;
      try {
        long start = System.currentTimeMillis(); // Counter
        dbResult = (config.getMode().equals(Mode.UNIMODAL)) ? query.getContinuousLink(linkId) : query.getLink(linkId);
        ResultSet rSet = dbResult.getResultSet();
        if (rSet.next()) {
          int sourceId = rSet.getInt("SOURCE");
          int targetId = rSet.getInt("TARGET");
          double length = rSet.getDouble("LENGTH");
          short degree = rSet.getShort("DEGREE");
          link = new ContinuousLink(linkId, sourceId, targetId, length);
          addLink(link, degree);
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
    }
    return link;
  }

  public ContinuousLink getInitialLink(int linkId, ANode referenceNode) {
    ContinuousLink link = (ContinuousLink) links.get(linkId);
    if (link == null) {
      DBResult dbResult = null;
      try {
        long start = System.currentTimeMillis(); // Counter
        dbResult = (config.getMode().equals(Mode.UNIMODAL)) ? query.getContinuousLink(linkId) : query.getLink(linkId);
        ResultSet rSet = dbResult.getResultSet();
        if (rSet.next()) {
          int sourceId = rSet.getInt("SOURCE");
          byte sourceMode = rSet.getByte("SOURCE_MODE");
          short degree = rSet.getShort("DEGREE");
          int targetId = rSet.getInt("TARGET");
          byte targetMode = rSet.getByte("TARGET_MODE");
          double length = rSet.getDouble("LENGTH");
          link = new ContinuousLink(linkId, sourceId, targetId, length);
          addLink(link, sourceMode, targetMode, degree, referenceNode);
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
    }
    return link;
  }

  @Deprecated
  public double initialExplore(GeoQueryPoint q, double dMax, double speed, Map<Integer, ANode> loadedIERNodes) {
    return -1;
  }

  /**
   * <p>
   * Method addLink
   * </p>
   * 
   * @param link
   * @param sourceMode
   * @param targetMode
   * @param degree
   * @param referenceNode
   */
  public void addLink(ILink link, byte sourceMode, byte targetMode, short degree, INode referenceNode) {
    if (config.isIncoming()) {
      addNode(link.getStartNodeId(), sourceMode, degree, referenceNode);
      addNode(link.getEndNodeId(), targetMode, referenceNode);
      if (!links.containsKey(link.getId())) {
        links.put(link.getId(), link);
        adjList.get(link.getEndNodeId()).add(link);
      }
    } else {
      addNode(link.getStartNodeId(), sourceMode, referenceNode);
      addNode(link.getEndNodeId(), targetMode, degree, referenceNode);
      if (!links.containsKey(link.getId())) {
        links.put(link.getId(), link);
        adjList.get(link.getStartNodeId()).add(link);
      }
    }
  }

  public void addLink(ILink link, byte sourceMode, byte targetMode, short degree) {
    if (config.isIncoming()) {
      addNode(link.getStartNodeId(), sourceMode, degree);
      if (!links.containsKey(link.getId())) {
        links.put(link.getId(), link);
        adjList.get(link.getEndNodeId()).add(link);
      }
    } else {
      addNode(link.getEndNodeId(), targetMode, degree);
      if (!links.containsKey(link.getId())) {
        links.put(link.getId(), link);
        adjList.get(link.getStartNodeId()).add(link);
      }
    }
  }

  public void addLink(ILink link, short degree) {
    if (config.isIncoming()) {
      addNode(link.getStartNodeId(), degree);
      addNode(link.getEndNodeId());
      if (!links.containsKey(link.getId())) {
        links.put(link.getId(), link);
        adjList.get(link.getEndNodeId()).add(link);
      }
    } else {
      addNode(link.getStartNodeId());
      addNode(link.getEndNodeId(), degree);
      if (!links.containsKey(link.getId())) {
        links.put(link.getId(), link);
        adjList.get(link.getStartNodeId()).add(link);
      }
    }
  }

  /**
   * <p>
   * Method addLink
   * </p>
   * 
   * @param link
   * @param degree
   * @param referenceNode
   */
  public void addLink(DiscreteLink link, short degree, INode referenceNode) {
    if (config.isIncoming()) {
      addNode(link.getStartNodeId(), NWMode.DISCRETE, degree, referenceNode);
      addNode(link.getEndNodeId(), NWMode.DISCRETE, referenceNode);
      if (!links.containsKey(link.getId())) {
        links.put(link.getId(), link);
        adjList.get(link.getEndNodeId()).add(link);
      }
    } else {
      addNode(link.getStartNodeId(), NWMode.DISCRETE, referenceNode);
      addNode(link.getEndNodeId(), NWMode.DISCRETE, degree, referenceNode);
      if (!links.containsKey(link.getId())) {
        links.put(link.getId(), link);
        adjList.get(link.getStartNodeId()).add(link);
      }
    }
  }

  @Deprecated
  private void addNode(int nodeId, byte mode, short sourceOutDegree, INode refNode) {
    ANode node = nodes.get(nodeId);
    if (node == null) {
      statistic.log(Type.LOADED_NODES);
      node = new ANode(nodeId, mode, sourceOutDegree);
      addNode(nodeId, node);
    } else if (!node.isSetOutDegreeCounter()) {
      node.setDegreeCounter(sourceOutDegree);
    }
    node.setReferenceNode(refNode);
    if (!adjList.containsKey(nodeId)) {
      adjList.put(nodeId, new HashSet<ILink>());
    }
  }

  /**
   * <p>
   * Method addNode
   * </p>
   * 
   * @param nodeId
   * @param mode
   * @param degree
   */
  private void addNode(int nodeId, byte mode, short degree) {
    ANode node = nodes.get(nodeId);
    if (node == null) {
      node = new ANode(nodeId, mode, degree);
      addNode(nodeId, node);
    } else if (!node.isSetOutDegreeCounter()) {
      node.setDegreeCounter(degree);
    }
    if (!adjList.containsKey(nodeId)) {
      adjList.put(nodeId, new HashSet<ILink>());
    }
  }

  /**
   * <p>
   * Method addNode
   * </p>
   * 
   * @param nodeId
   */
  private void addNode(int nodeId) {
    ANode node = nodes.get(nodeId);
    if (node == null) {
      node = new ANode(nodeId);
      adjList.put(nodeId, new HashSet<ILink>());
      addNode(nodeId, node);
    }
  }

  /**
   * <p>
   * Method addNode
   * </p>
   * 
   * @param nodeId
   * @param degree
   */
  private void addNode(int nodeId, short degree) {
    ANode node = nodes.get(nodeId);
    if (node == null) {
      node = new ANode(nodeId, degree);
      adjList.put(nodeId, new HashSet<ILink>());
      addNode(nodeId, node);
    } else if (!node.isSetOutDegreeCounter()) {
      node.setDegreeCounter(degree);
    }
  }

  /**
   * <p>
   * Method addNode
   * </p>
   * 
   * @param nodeId
   * @param node
   */
  private void addNode(int nodeId, ANode node) {
    nodes.put(node.getId(), node);
    maxNodeSize = Math.max(maxNodeSize, nodes.size());
    statistic.log(Type.LOADED_NODES);
  }

  @Deprecated
  private void addNode(int nodeId, byte mode, INode refNode) {
    ANode node = nodes.get(nodeId);
    if (node == null) {
      node = new ANode(nodeId, mode, Short.MIN_VALUE);
      statistic.log(Type.LOADED_NODES);
      addNode(nodeId, node);
    }
    node.setReferenceNode(refNode);
    if (!adjList.containsKey(nodeId)) {
      adjList.put(nodeId, new HashSet<ILink>());
    }
  }

  @Deprecated
  public void addNode(ANode node) {
    if (!nodes.containsKey(node.getId())) {
      statistic.log(Type.LOADED_NODES);
      addNode(node.getId(), node);
    }
    if (!adjList.containsKey(node.getId())) {
      adjList.put(node.getId(), new HashSet<ILink>());
    }
  }

  @Override
  protected Collection<ILink> getAdjacentContinuousLinks(INode iNode) {
    return adjList.get(iNode.getId());
  }

  @Override
  protected Collection<ILink> getAllAdjacentLinks(INode iNode) {
    return adjList.get(iNode.getId());
  }

  @Override
  public LinkCollection getAdjacentLinkCollection(INode node) {
    LinkCollection linkCollection = new LinkCollection();
    linkCollection.addAll(getAdjacentLinks(node), config.isIncoming());
    // we have to check this, because a preexplored node might only be have discrete links, when density is too small
    if (linkCollection.getContinuousLinks().isEmpty() && ((ANode) node).isPreExplored()) {
      Collection<ILink> incidentContinuousLinks = loadContinuousLinksFromINE((ANode) node);
      linkCollection.addAll(incidentContinuousLinks, config.isIncoming());
    }
    return linkCollection;
  }

  /**
   * <p>
   * Method loadLinksFromIER
   * </p>
   * retrieves the links via euclidean network restriction. This method <b>always</b> performs a DB lookup
   * 
   * @param node
   * @param range
   */
  public void loadLinksFromIER(ANode node, double range) {

    DBResult dbResult = null;
    try {
      long start = System.currentTimeMillis(); // Counter
      dbResult = getQuery().getLinksInRange(node.getCoordinate(), range);
      ResultSet rSet = dbResult.getResultSet();
      int fetched = 0;
      while (rSet.next()) {
        int linkId = rSet.getInt("ID");
        int sourceId = rSet.getInt("SOURCE");
        short degree = rSet.getShort("DEGREE");
        int targetId = rSet.getInt("TARGET");
        byte mode = rSet.getByte("EDGE_MODE");
        if (mode == NWMode.CONTINUOUS) {
          double length = rSet.getDouble("LENGTH");
          addLink(new ContinuousLink(linkId, sourceId, targetId, length), degree);
          statistic.log(Type.LOADED_CONTINUOUS_LINKS);
        } else {
          short routeId = rSet.getShort("ROUTE_ID");
          addLink(new DiscreteLink(linkId, sourceId, targetId, routeId), degree);
          statistic.log(Type.LOADED_DISCRETE_LINKS);
        }
        fetched++;
      }
      ((MIERWINEStatistic) statistic).logDBIER(DBType.GET_IER_LINKS, System.currentTimeMillis() - start);
      // statistic.logRuntime(DBType.GET_IER_LINKS, System.currentTimeMillis() - start);
      statistic.log(Type.LOADED_LINKS_WITH_IER, fetched);

      if (config.isDebug()) {
        getQuery().logLoadedIERNode(node.getId(), node.getDistance(), maxDuration - node.getDistance());
        getQuery().logLoadedIERCircle(node.getId(), (maxDuration - node.getDistance()) * walkingSpeed, range);
      }
    } catch (SQLException e) {
      System.err.println("Problems with node:" + node);
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
  }

  /**
   * <p>
   * Method loadLinksFromIER
   * </p>
   * 
   * @param node
   * @param intersections
   */
  public void loadLinksFromIER(ANode node, Collection<ANode> intersections) {
    String[] aios = new String[1];
    DBResult dbResult = null;
    try {
      long start = System.currentTimeMillis(); // Counter
      dbResult = getQuery().getLinksInRange(node.getCoordinate(), node.getRadius(), intersections, aios);
      ResultSet rSet = dbResult.getResultSet();
      int fetched = 0;
      while (rSet.next()) {
        int linkId = rSet.getInt("ID");
        int sourceId = rSet.getInt("SOURCE");
        short degree = rSet.getShort("DEGREE");
        int targetId = rSet.getInt("TARGET");
        byte mode = rSet.getByte("EDGE_MODE");
        if (mode == NWMode.CONTINUOUS) {
          double length = rSet.getDouble("LENGTH");
          addLink(new ContinuousLink(linkId, sourceId, targetId, length), degree);
          statistic.log(Type.LOADED_CONTINUOUS_LINKS);
        } else {
          short routeId = rSet.getShort("ROUTE_ID");
          addLink(new DiscreteLink(linkId, sourceId, targetId, routeId), degree);
          statistic.log(Type.LOADED_DISCRETE_LINKS);
        }
        fetched++;
      }
      ((MIERWINEStatistic) statistic).logDBIER(DBType.GET_IER_LINKS, System.currentTimeMillis() - start);
      // / getStatistic().logRuntime(DBType.GET_IER_LINKS, System.currentTimeMillis() - start);
      statistic.log(Type.LOADED_LINKS_WITH_IER, fetched);

      if (config.isDebug()) {
        getQuery().logLoadedIERNode(node.getId(), node.getDistance(), maxDuration - node.getDistance());
        if (aios[0] == null) {
          getQuery().logLoadedIERCircle(node.getId(), (maxDuration - node.getDistance()) * walkingSpeed,
              node.getRadius());
        } else {
          getQuery().logLoadedIERArea(node.getId(), aios, node.getRadius());
        }
      }
    } catch (SQLException e) {
      System.err.println("Problems with node:" + node);
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    // System.out.println("Memory size: " + nodes.size() + "/" + maxMemorySize);
  }

  /**
   * <p>
   * Method loadLinksFromINE
   * </p>
   * 
   * @param node
   * @param queryType
   * @return
   */
  public Set<ILink> loadLinksFromINE(ANode node, byte queryType) {
    if (queryType == NWMode.CONTINUOUS) {
      return loadContinuousLinksFromINE(node);
    } else if (queryType == NWMode.DISCRETE) {
      return loadDiscreteLinksFromINE(node);
    } else { // loads edges from all networks
      return loadLinksFromINE(node);
    }
  }

  /**
   * <p>
   * Method loadLinksFromINE
   * </p>
   * retrieves the links via incremental network expansion
   * 
   * @param node
   * @return
   */
  public Set<ILink> loadLinksFromINE(ANode node) {
    if (config.isDebug()) {
      getQuery().logLoadedIERNode(node.getId(), node.getDistance(), maxDuration - node.getDistance());
    }

    Set<ILink> adjLinks = adjList.get(node.getId());

    DBResult dbResult = null;
    try {
      int nodeId = node.getId();
      long start = System.currentTimeMillis(); // Counter
      dbResult = query.getAdjacentLinks(nodeId);
      ResultSet rSet = dbResult.getResultSet();
      int fetched = 0;
      while (rSet.next()) {
        ILink link;
        int linkId = rSet.getInt("ID");
        int adjNodeId = rSet.getInt("NODE_ID");
        byte adjNodeMode = rSet.getByte("NODE_MODE");
        short adjNodeDegree = rSet.getShort("DEGREE");
        byte mode = rSet.getByte("EDGE_MODE");
        if (mode == NWMode.CONTINUOUS) {
          double length = rSet.getDouble("LENGTH");
          if (config.isIncoming()) {
            link = new ContinuousLink(linkId, adjNodeId, nodeId, length);
          } else {
            link = new ContinuousLink(linkId, nodeId, adjNodeId, length);
          }
          statistic.log(Type.LOADED_CONTINUOUS_LINKS);
        } else {
          short routeId = rSet.getShort("ROUTE_ID");
          if (config.isIncoming()) {
            link = new DiscreteLink(linkId, adjNodeId, nodeId, routeId);
          } else {
            link = new DiscreteLink(linkId, nodeId, adjNodeId, routeId);
          }
          statistic.log(Type.LOADED_DISCRETE_LINKS);
        }
        addLink(link, adjNodeMode, node.getMode(), adjNodeDegree, node.getReferenceNode());
        nodes.get(adjNodeId).setReferenceNode(node.getReferenceNode());
        adjLinks.add(link);
        fetched++;
      }
      statistic.logRuntime(DBType.GET_INE_LINKS, System.currentTimeMillis() - start);
      statistic.log(Type.LOADED_LINKS_WITH_INE, fetched);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    return adjLinks;
  }

  public Set<ILink> loadDiscreteLinksFromINE(ANode node) {
    Set<ILink> adjLinks = new HashSet<ILink>();
    DBResult dbResult = null;
    try {
      int nodeId = node.getId();
      long start = System.currentTimeMillis(); // Counter
      dbResult = getQuery().getAdjacentDiscreteLinks(nodeId);
      ResultSet rSet = dbResult.getResultSet();
      int fetched = 0;
      while (rSet.next()) {
        int linkId = rSet.getInt("ID");
        int adjNodeId = rSet.getInt("NODE_ID");
        short adjNodeDegree = rSet.getShort("DEGREE");
        short routeId = rSet.getShort("ROUTE_ID");
        DiscreteLink link;
        if (config.isIncoming()) {
          link = new DiscreteLink(linkId, adjNodeId, nodeId, routeId);
        } else {
          link = new DiscreteLink(linkId, nodeId, adjNodeId, routeId);
        }
        addLink(link, NWMode.DISCRETE, node.getMode(), adjNodeDegree, node.getReferenceNode());
        adjLinks.add(link);
        fetched++;
      }
      // System.out.println("INE_DISC" + ++INE_LOAD_DISC);
      statistic.logRuntime(DBType.GET_INE_LINKS, System.currentTimeMillis() - start);
      statistic.log(Type.LOADED_DISCRETE_LINKS, fetched);
      statistic.log(Type.LOADED_LINKS_WITH_INE, fetched);

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    return adjLinks;
  }

  public Set<ILink> loadContinuousLinksFromINE(ANode node) {
    Set<ILink> inLinks = new HashSet<ILink>();
    DBResult dbResult = null;
    try {
      int nodeId = node.getId();
      long start = System.currentTimeMillis(); // Counter
      dbResult = getQuery().getAdjacentContinuousLinks(nodeId);
      ResultSet rSet = dbResult.getResultSet();
      int fetched = 0;
      while (rSet.next()) {
        int linkId = rSet.getInt("ID");
        int adjNodeId = rSet.getInt("NODE_ID");
        byte adjNodeMode = config.getMode().equals(Mode.UNIMODAL) ? NWMode.CONTINUOUS : rSet.getByte("NODE_MODE");
        short adjNodeDegree = rSet.getShort("DEGREE");
        double length = rSet.getDouble("LENGTH");
        ContinuousLink link = config.isIncoming() ? new ContinuousLink(linkId, adjNodeId, nodeId, length)
            : new ContinuousLink(linkId, nodeId, adjNodeId, length);
        addLink(link, adjNodeMode, node.getMode(), adjNodeDegree, node.getReferenceNode());
        inLinks.add(link);
        fetched++;
      }
      // System.out.println("INE_CNT" + ++INE_LOAD_CNT);
      statistic.logRuntime(DBType.GET_INE_LINKS, System.currentTimeMillis() - start);
      statistic.log(Type.LOADED_CONTINUOUS_LINKS, fetched);
      statistic.log(Type.LOADED_LINKS_WITH_INE, fetched);
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    return inLinks;
  }

  @Override
  public ANode getNode(int nodeId) {
    return nodes.get(nodeId);
  }

  /**
   * Removes the passed <code>node</code> from the trace.
   * 
   * @param node the node that has to be removed
   */
  public void removeNode(ANode node) {
    statistic.log(Type.REMOVED_NODES);
    if (removedNodeIds.contains(node.getId())) {
      System.err.println("Node: " + node.getId() + " already removed");
    } else {
      // System.out.println("Removing node " + node.getId());
      removedNodeIds.add(node.getId());
      // storeNode(node);
    }
    adjList.remove(node.getId());
    nodes.remove(node.getId());
  }

  @Override
  public int getNodeSize() {
    return nodes.size();
  }

  @Override
  public int getLinkSize() {
    return links.size();
  }

  @Override
  public int getContinuousLinkSize() {
    if (getMode().equals(Mode.UNIMODAL))
      return links.size();
    else {
      int size = 0;
      for (ILink link : links.values()) {
        if (link instanceof ContinuousLink) {
          size++;
        }
      }
      return size;
    }
  }

  @Override
  public int getDiscreteLinkSize() {
    if (getMode().equals(Mode.UNIMODAL))
      return 0;
    else {
      int size = 0;
      for (ILink link : links.values()) {
        if (link instanceof DiscreteLink) {
          size++;
        }
      }
      return size;
    }
  }

  @Override
  public void terminate() {
    int unexploredLinks = 0;
    for (ILink link : links.values()) {
      if (link.getOffset() != null) {
        // storableLinks.add(link);
      } else {
        unexploredLinks++;
      }
    }
    statistic.log(Type.UNEXPLORED_LINKS, unexploredLinks);
    super.terminate();

    /*
     * for (Integer linkId : loadedFromDB.keySet()) { int loaded = loadedFromDB.get(linkId); if(loaded>1){
     * System.out.println("Link with id " + linkId + " was loaded " + loaded + " times."); } }
     */

  }

  public IQuery getQuery() {
    return (IQuery) query;
  }

  public GeoQueryPoint getCoordinates(int nodeId) {
    long start = System.currentTimeMillis(); // Counter
    GeoQueryPoint geoPoint = new GeoQueryPoint(nodeId);
    DBResult dbResult = null;
    try {
      dbResult = getQuery().getCoordinates(geoPoint);
      ResultSet rSet = dbResult.getResultSet();
      if (rSet.next()) {
        geoPoint.setCoordinates(rSet.getDouble("X"), rSet.getDouble("Y"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    statistic.logRuntime(DBType.GET_COORDINATE, System.currentTimeMillis() - start);
    return geoPoint;
  }

  /**
   * <p>
   * Method geoReference
   * </p>
   * 
   * @param geoPoint
   */
  public void geoReference(GeoQueryPoint geoPoint) {
    long start = System.currentTimeMillis(); // Counter
    DBResult dbResult = null;
    try {
      dbResult = getQuery().getCoordinates(geoPoint);
      ResultSet rSet = dbResult.getResultSet();
      if (rSet.next()) {
        geoPoint.setCoordinates(rSet.getDouble("X"), rSet.getDouble("Y"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    statistic.logRuntime(DBType.GET_COORDINATE, System.currentTimeMillis() - start);
  }
  

  public int getMaxNodeSize() {
    return maxNodeSize;
  }

  /**
   * <p>
   * Method getMaxMemorySize returns the maximal available memory size in terms of nodes
   * </p>
   * 
   * @return
   */
  public int getMaxMemorySize() {
    return maxMemorySize;
  }

  /**
   * <p>
   * Method getAvailableMemorySize returns the available memory size in terms of nodes
   * </p>
   * 
   * @return
   */
  public int getAvailableMemorySize(float factor) {
    int available = maxMemorySize - (getNodeSize() + minFreeMemorySize) ;
    if(maxMemorySize*0.95<getNodeSize()) {
      double perc = 100.0 - ((double)getNodeSize()/maxMemorySize) * 100;
      System.err.println("Housten, we have a problem. Free memory size is on " + NumberFormat.getInstance().format(perc) + "%");
      // return EMPTY_MEMORY;
    }
    return (int) Math.max(30,available * factor);
    //return (int) (available * factor);
  }

  /**
   * <p>
   * Method getCoordinate
   * </p>
   * 
   * @param nodeId
   * @return
   */
  public GeoPoint getCoordinate(int nodeId) {
    DBResult dbResult = null;
    try {
      // /*
      if (!nodeGeoLookup.containsKey(nodeId)) {
        nodeGeoLookup.put(nodeId, 1);
      } else {
        nodeGeoLookup.put(nodeId, nodeGeoLookup.get(nodeId) + 1);
      }
      // */
      long start = System.currentTimeMillis();
      dbResult = getQuery().getCoordinate(nodeId);
      ResultSet rSet = dbResult.getResultSet();
      if (rSet.next()) {
        double x = rSet.getDouble("X");
        double y = rSet.getDouble("Y");
        statistic.logRuntime(DBType.GET_COORDINATE, System.currentTimeMillis() - start);
        return new GeoPoint(x, y);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    System.err.println("Node with id: " + nodeId + " has no stored geometry");
    return null;
  }

  /**
   * <p>
   * Method getRange returns the maximal range for the given vertex with a maximal number of specified vertices. Query
   * find the last two tuples having a memory size smaller than memorySize and then interpolates the range.
   * </p>
   * 
   * @param nodeId the vertex identifier
   * @param memorySize the maximal number of vertices
   * @return the available range as the euclidean distance
   */
  public double getRange(int nodeId, int memorySize) {
    DBResult dbResult = null;
    try {
      long start = System.currentTimeMillis();
      dbResult = getQuery().getRange(nodeId, memorySize);
      ResultSet rSet = dbResult.getResultSet();
      int s0 = 0, s1 = 0;
      double ed0 = 0, ed1 = 0;

      if (rSet.next()) {
        s0 = rSet.getInt("s0");
        ed0 = rSet.getDouble("ed0");
        s1 = rSet.getInt("s1");
        ed1 = rSet.getDouble("ed1");
      }
      double ed = MathUtil.interpolate(ed0, ed1, s0, s1, memorySize);
      statistic.logRuntime(DBType.GET_RANGE_FROM_SIZE, System.currentTimeMillis() - start);
      return ed;
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (dbResult != null)
        dbResult.close();
    }
    return 0;
  }

  /**
   * <p>
   * Method hasUnlimitedMemory
   * </p>
   * 
   * @return
   */
  public boolean unlimitedMemory() {
    return unlimitedMemory;
  }

}
