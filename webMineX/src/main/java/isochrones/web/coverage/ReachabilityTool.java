package isochrones.web.coverage;

import isochrones.algorithm.Mode;
import isochrones.algorithm.TableEntry;
import isochrones.db.DBResult;
import isochrones.db.DBVendor;
import isochrones.network.Offset;
import isochrones.web.config.Config;
import isochrones.web.db.IWebQuery;
import isochrones.web.db.OracleQuery;
import isochrones.web.db.PostgresQuery;
import isochrones.web.services.WebService;
import isochrones.web.utils.SBAUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;

public class ReachabilityTool {

  private Config config;
  private IWebQuery query;
  private Set<Integer> exploredEdges = new HashSet<Integer>();
  private long timeInit, timeBufferQuery;
  protected static final Logger LOGGER = Logger.getLogger(WebService.class.getPackage().getName());

  /**
   * <p>
   * Constructs a(n) <code>ReachabilityTool</code> object.
   * </p>
   * 
   * @param config
   */
  public ReachabilityTool(Config config) {
    this.config = config;
    this.query = config.getDbVendor().equals(DBVendor.ORACLE) ? new OracleQuery(config, false)
        : new PostgresQuery(config, false);
  }

  /**
   * <p>
   * Method init
   * </p>
   * 
   * @param tableEntry
   * @param disableIndex
   */
  private void init(TableEntry tableEntry, boolean disableIndex) {
    long start = System.currentTimeMillis();
    // if(disableIndex)
    // query.disableIndex(config.getProperty("idx.linkBuffer"));
    // query.deleteEntries(tableEntry, clientId);
    timeInit = System.currentTimeMillis() - start;
  }

  /**
   * <p>
   * Method createIsoAreaLBA
   * </p>
   * creates the area of isochrones using the link based approach. It creates around each link a buffer and stored them
   * in the DB.
   * 
   * @param tableEntry
   * @param dMax
   * @param bufferDistance
   * @param speed
   */
  public void createIsoAreaLBA(TableEntry tableEntry, int dMax, double bufferDistance, double speed, boolean incoming) {
    query.createBuffer(query.getIsochoneEdges(dMax, speed, true), bufferDistance,incoming);
  }

  /**
   * <p>
   * Method createIsoAreaSBA
   * </p>
   * creates the area of isochrones using the surface based approach. It creates for each isochrone island a the
   * isochrone area as a polygon. The algorithm starts from the most-left located link and collects the outmost located
   * link until it reaches the starting link. The function is implemented in a depth-first-search manner. After
   * retrieving the source link, the polygon is created and with a surrounded buffer it is stored in the DB. Then the
   * links laying within that buffer are not considered anymore for the next calculation. The procedure terminates when
   * all links are processed.
   * 
   * @param isochrone
   * @param dMax
   * @param bufferDistance
   * @param speed
   * @param sessionId
   * @param mode
   */
  public void createIsoAreaSBA(TableEntry tableEntry, int dMax, double bufferDistance, double speed) {
    init(tableEntry, false);
    Collection<IsoEdge> isoEdges = query.getIsochoneEdges(dMax, speed, false);
    int areaId = 0;

    GeoIsochrone isochrone = new GeoIsochrone();
    isochrone.addAll(isoEdges,config.isIncoming());
    // TODO check if this is required
    isochrone.populateInvertedLinks(config.isIncoming());

    // while (!isoEdges.isEmpty()) {
    while (!isochrone.isEmpty()) {
      createMargins(isochrone);
      LOGGER.fine("Insert isochrone area with id: " + areaId);
      /*
       * System.out.println("Border Edges:"); for (IsoEdge isoEdge : isochrone.getBorderEdges()) {
       * System.out.println(isoEdge); }
       */
      // query.storeAreaFromEdges(areaId, isochrone.getBorderEdges(), bufferDistance);
      query.storeArea(areaId, isochrone.getOrdinates(), bufferDistance);
      isochrone.deleteOrdinates();
      if (config.getMode().equals(Mode.MULTIMODAL)) {
        LOGGER.fine("Querying links in polygon with id: " + areaId);
        long start = System.currentTimeMillis();
        DBResult dbResult = null;
        ResultSet rSet = null;

        try {
          dbResult = query.edgesInArea(areaId++);
          rSet = dbResult.getResultSet();
          while (rSet.next()) {
            isochrone.disableEdge(rSet.getInt(1));
          }
          timeBufferQuery += System.currentTimeMillis() - start;
        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          dbResult.close();
        }
      } else {
        isochrone.disableAllEdges();
      }
    }
  }

  /**
   * <p>
   * Method retrieveIslands
   * </p>
   * 
   * @param links
   * @return
   */
  public void createMargins(GeoIsochrone isochrone) {
    IsoEdge sourceEdge = isochrone.getSourceEdge(config.isIncoming());
    LOGGER.info("Create Area of island with source edge:" + sourceEdge);
    boolean found = DFS(null, sourceEdge, isochrone);
    if (found) {
      LOGGER.info("Isochrone Area succesful computed");
    } else {
      isochrone.addEdgeOrdinates(sourceEdge, config.isIncoming());
      LOGGER.info("Isochrone Area NOT succesful computed");
    }
    /*
     * if (found) { System.out.println("Involved edges:"); for (PLink link : island.getBorderLinks()) {
     * System.out.println(link); } }
     */
    // return islands.values();
  }

  public boolean DFS(IsoEdge referenceEdge, IsoEdge rootEdge, GeoIsochrone island) {
    if (referenceEdge != null && referenceEdge.getId() == rootEdge.getId()) {
      island.addEdgeOrdinates(rootEdge, config.isIncoming());
      return true;
    } else {
      if (referenceEdge == null) {
        referenceEdge = rootEdge;
      }
      if (exploredEdges.contains(referenceEdge.getId())) {
        LOGGER.warning("Edge " + referenceEdge + " has already explored!!");
      } else {
        exploredEdges.add(referenceEdge.getId());
      }
      if (!referenceEdge.equals(rootEdge) && referenceEdge.isPartial()) { 
        // case 1: edge is partial
        IsoEdge invertedEdge = island.getInvertedLink(referenceEdge);
        if (SBAUtil.reachedFromBothSides(referenceEdge, invertedEdge,config.isIncoming())) {
          // case 1.1: the adjacent vertex is reachable via the inverted edge, so entering in another recursion level
          int referenceNodeId = config.isIncoming() ? referenceEdge.getStartNodeId() : referenceEdge.getEndNodeId();
          boolean found = false;
          referenceEdge.mergeGeometry(invertedEdge);
          referenceEdge.setOffset(new Offset(0, referenceEdge.getLength()));
          island.addEdgeOrdinates(referenceEdge, config.isIncoming());
          Set<IsoEdge> adjEdges = island.getAdjacentEdges(referenceNodeId);
          if (adjEdges != null) {
            boolean useLastPointRef = config.isIncoming() ? false : true;
            TreeMap<Double, IsoEdge> adjs = calcultateAngles(referenceEdge,useLastPointRef,adjEdges,config.isIncoming());
            for (Iterator<IsoEdge> iterator = adjs.values().iterator(); iterator.hasNext() && !found;) {
              IsoEdge currentEdge = iterator.next();
              found = DFS(currentEdge, rootEdge, island);
              if (!found) {
                // System.out.println("Backward from edge:" + myLink);
                if (!currentEdge.isPartial()) {
                  island.addEdgeOrdinates(currentEdge, !config.isIncoming());
                  // island.addOrdinate(myLink, true);
                } else {
                  IsoEdge myInverted = island.getInvertedLink(currentEdge);
                  if (myInverted != null) {
                    if (!myInverted.isPartial()) { // then we merge the geometries
                      island.addEdgeOrdinates(myInverted, !config.isIncoming());
                    } else {
                      island.addEdgeOrdinates(currentEdge, !config.isIncoming());
                    }
                  } else {
                    island.addEdgeOrdinates(currentEdge, !config.isIncoming());
                  }
                }
              } else {
                return true;
              }
            }
          }
          return found;
        } else {
          // case 1.2: the adjacent vertex is NOT reachable, so exit from current recursion level and return false
          island.addEdgeOrdinates(referenceEdge, config.isIncoming());
          return false;
        }
      } else {
     // case 2: visited edge either the root node or it is NOT partial, so entering in another recursion level
        int referenceNodeId;
        if (referenceEdge.getId() == rootEdge.getId() ) { // handling for the first explored edge
          referenceNodeId = config.isIncoming() ? referenceEdge.getEndNodeId() : referenceEdge.getStartNodeId();
        } else {
          referenceNodeId = config.isIncoming() ? referenceEdge.getStartNodeId() : referenceEdge.getEndNodeId();
        }
        boolean found = false;
        island.addEdgeOrdinates(referenceEdge, referenceEdge.getId() == rootEdge.getId() ? !config.isIncoming() : config.isIncoming());
        Set<IsoEdge> adjacentEdges = island.getAdjacentEdges(referenceNodeId);
        if (adjacentEdges != null) {
          boolean useLastPointRef;
          if(referenceEdge.getId() == rootEdge.getId()){
            useLastPointRef = config.isIncoming() ? true : false;
          } else {
            useLastPointRef = config.isIncoming() ? false : true;
          }
          TreeMap<Double, IsoEdge> adjs = calcultateAngles(referenceEdge, useLastPointRef, adjacentEdges, config.isIncoming());
          for (Iterator<IsoEdge> iterator = adjs.values().iterator(); iterator.hasNext() && !found;) {
            IsoEdge currentEdge = iterator.next();
            found = DFS(currentEdge, rootEdge, island);
            if (!found) {
              island.addEdgeOrdinates(currentEdge, !config.isIncoming());
            } else {
              return true;
            }
          }
        }
        return found;
      }
    }
  }

  /**
   * 
   * <p>Method calcultateAngles</p> calculates the relative angles between the reference edge and all adjacent edges
   * @param referenceEdge the reference edge from which to compute the angle
   * @param useLastPointRef true, if the angle of the reference edge is computed with the last two ordinates of the edge, otherwise it is computed with the first two ordinates
   * @param adjEdges the adjacent edges to which compute the angles
   * @param useLastPoint true, if the angle of the adjacent edges is computed with the last two ordinates of the edge, otherwise it is computed with the first two ordinates
   * @return an ordered map (with key=angle and value=edge) sorted with the relative angle to the reference edge in ascending order 
   */
  public TreeMap<Double, IsoEdge> calcultateAngles(IsoEdge referenceEdge,boolean useLastPointRef,Set<IsoEdge> adjEdges, boolean useLastPoint) {
    TreeMap<Double, IsoEdge> angleLinks = new TreeMap<Double, IsoEdge>();
    TreeMap<Double, List<IsoEdge>> duplicates = new TreeMap<Double, List<IsoEdge>>();
    double referenceAngle = SBAUtil.calculateAngle(useLastPointRef, referenceEdge);
    for (IsoEdge currentLink : adjEdges) {
      if (!currentLink.equals(referenceEdge)) {
        if (currentLink.inverted(referenceEdge))
          angleLinks.put(360.0, currentLink);
        else {
          double angle = SBAUtil.calcultateRelativeAngle(SBAUtil.calculateAngle(useLastPoint, currentLink), referenceAngle);
          IsoEdge previous = angleLinks.put(angle, currentLink);
          if (previous != null) {
            if (!duplicates.containsKey(angle)) {
              duplicates.put(angle, new ArrayList<IsoEdge>());
              duplicates.get(angle).add(previous);
            }
            duplicates.get(angle).add(currentLink);
            angleLinks.remove(angle);
          }
        }
      }
    }
    for (List<IsoEdge> links : duplicates.values()) {
      for (IsoEdge pLink : links) {
        double currentAngle = SBAUtil.calculateAngleFiner(referenceEdge, pLink);
        angleLinks.put(currentAngle, pLink);
      }
    }
    return angleLinks;
  }

  /**
   * <p>
   * Method retrieveReachedInhabitants
   * </p>
   * 
   * @return
   */
  public int retrieveReachedInhabitants(TableEntry entry) {
    return query.reachedInhabitants();
  }

  public int retrieveTotalInhabitants() {
    return query.totalInhabitants();
  }

  /**
   * <p>
   * Method getTotalTimeBufferQuery
   * </p>
   * 
   * @return
   */
  public long getTotalTimeBufferQuery() {
    return timeBufferQuery;
  }

  /**
   * <p>
   * Method getTimeInit
   * </p>
   * 
   * @return
   */
  public long getTimeInit() {
    return timeInit;
  }

}
