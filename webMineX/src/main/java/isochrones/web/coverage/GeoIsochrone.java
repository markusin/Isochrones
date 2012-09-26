package isochrones.web.coverage;

import isochrones.web.constants.Comparable;
import isochrones.web.geometry.AbstractLineString;
import isochrones.web.geometry.Point;
import isochrones.web.utils.SBAUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
*
* <p>The <code>GeoIsochrone</code> class</p>
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p> </p>
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
 */
public class GeoIsochrone {

  Map<Integer, IsoEdge> members = new HashMap<Integer, IsoEdge>();
  Map<Integer, Set<IsoEdge>> adjList = new HashMap<Integer, Set<IsoEdge>>();
  // Map<Integer,List<PLink>> outgoingAdjList = new HashMap<Integer,List <PLink>>();
  Map<Integer, IsoEdge> invertedEdges = new HashMap<Integer, IsoEdge>();
  IsoEdge sourceEdge = null;
  List<IsoEdge> borderLinks = new ArrayList<IsoEdge>();
  List<Point> points = new ArrayList<Point>();
  int rootNodeId;
  Point lastInsertedPoint;

  /**
   * <p>
   * Constructs a(n) <code>IsochroneIsland</code> object.
   * </p>
   * 
   * @param rootNodeId
   */
  public GeoIsochrone(int rootNodeId) {
    this.rootNodeId = rootNodeId;
  }

  /**
   * 
   * <p>Constructs a(n) <code>GeoIsochrone</code> object.</p>
   */
  public GeoIsochrone() {
  }

  /**
   * <p>
   * Method addLink adds the link to the collection and and checks if it is the most left top located
   * </p>
   * 
   * @param link
   */
  public void addEdge(IsoEdge edge, boolean incoming) {
    if (sourceEdge == null || 
		 (SBAUtil.reachedFromBothSides(edge, invertedEdges.get(edge.getId()),incoming) && sourceEdge.getGeometry().compareTo(edge.getGeometry()) > isochrones.web.constants.Comparable.BEFORE)) {
      sourceEdge = edge;
    }
    if(incoming){
      if (!adjList.containsKey(edge.getEndNodeId())) {
        adjList.put(edge.getEndNodeId(), new HashSet<IsoEdge>());
      }
      adjList.get(edge.getEndNodeId()).add(edge);
    } else {
      if (!adjList.containsKey(edge.getStartNodeId())) {
        adjList.put(edge.getStartNodeId(), new HashSet<IsoEdge>());
      }
      adjList.get(edge.getStartNodeId()).add(edge);
    }
    members.put(edge.getId(), edge);
  }

  /**
   * <p>
   * Method addAll
   * </p>
   * 
   * @param links
   */
  public void addAll(Collection<IsoEdge> links, boolean incoming) {
    for (IsoEdge link : links) {
      addEdge(link,incoming);
    }
  }

  public Set<IsoEdge> getAdjacentEdges(int nodeId) {
    return adjList.get(nodeId);
  }

  // public List<PLink> getOutgoingLinks(int nodeId) {
  // return outgoingAdjList.get(nodeId);
  // }

  public IsoEdge getInvertedLink(IsoEdge link) {
    return invertedEdges.get(link.getId());
  }

  public void populateInvertedLinks(boolean incoming) {
    for (IsoEdge edge : members.values()) {
      Set<IsoEdge> adjacentEdges = getAdjacentEdges(incoming ? edge.getStartNodeId() : edge.getEndNodeId());
      if (adjacentEdges != null) {
        for (IsoEdge invertedEdge : adjacentEdges) {
          if (((incoming && invertedEdge.getStartNodeId() == edge.getEndNodeId())||(!incoming && invertedEdge.getEndNodeId() == edge.getStartNodeId())) && Math.round(invertedEdge.getLength())==Math.round(edge.getLength())) {
            invertedEdges.put(edge.getId(), invertedEdge);
            invertedEdge.setInvertedEdge(edge);
            break;
          }
        }
      }
    }
  }


  /**
   * 
   * <p>Method getSourceEdge</p>
   * @return the source edge of the island
   */
  public IsoEdge getSourceEdge(boolean incoming) {
    IsoEdge srcEdge = null;
    for (IsoEdge edge : members.values()) {
      if (!edge.isProcessed()) {
        if (srcEdge == null) {
          srcEdge = edge;
        } else {
          if(!SBAUtil.reachedFromBothSides(edge, edge.getInvertedEdge(),incoming)) {
            if (srcEdge.getGeometry().compareTo(edge.getGeometry()) > Comparable.BEFORE) {
              srcEdge = edge;
            }
          }
        }
      }
    }
    return srcEdge;
  }

  public int getRootNodeId() {
    return rootNodeId;
  }

  private void addBorderEdge(IsoEdge link) {
    borderLinks.add(link);
  }

  /**
   * 
   * <p>Method addEdgeOrdinates</p> add the ordinates of the passed link
   * to the ordinatecollection. Skips to add a point, if the last inserted 
   * ordinate is equal to the current one
   * @param edge the link object from which to add the ordinates
   * @param reversed true, if the ordinates must be added from the last to the first
   */
  public void addEdgeOrdinates(IsoEdge edge, boolean reversed) {
    if (reversed) {
    	// System.out.println("Adding REVERSED edge: " + edge);
      AbstractLineString reverseLine = edge.getGeometry().reverse();
      Iterator<Point> iter = reverseLine.iterator();
      while (iter.hasNext()) {
        addPoint(iter.next());
      }
    } else {
    	// System.out.println("Adding edge: " + edge);
      Iterator<Point> iter = edge.getGeometry().iterator();
      while (iter.hasNext()) {
        addPoint(iter.next());
      }
    }
    addBorderEdge(edge);
  }

  /**
   * 
   * <p>Method addPoint</p>
   * @param p
   */
  public void addPoint(Point p) {
    if(lastInsertedPoint==null || !lastInsertedPoint.equals(p)){
      points.add(p);
      lastInsertedPoint = p;
    }
  }

  /**
   * 
   * <p>Method getBorderEdges</p>
   * @return
   */
  public List<IsoEdge> getBorderEdges() {
    return borderLinks;
  }

  /**
   * <p>
   * Method getOrdinates
   * </p>
   * returns the ordinate that should represent a polygon of the border edges
   * 
   * @return
   */
  public List<Point> getOrdinates() {
    return points;
  }

  /**
   * <p>
   * Method disableLinks
   * </p>
   * 
   * @param id
   */
  public void disableEdge(int id) {
    IsoEdge edge = members.get(id);
    if (edge != null) {
      members.get(id).setProcessed(true);
    }
  }

  public void disableAllEdges() {
    for (IsoEdge edge : members.values()) {
      edge.setProcessed(true);
    }
  }

  /**
   * <p>
   * Method isEmpty
   * </p>
   * 
   * @return
   */
  public boolean isEmpty() {
    for (IsoEdge edge : members.values()) {
      if (!edge.isProcessed()) {
        //empty &= false;
        return false;
      }
    }
    return true;
  }

  /**
   * <p>
   * Method deleteOrdinates
   * </p>
   */
  public void deleteOrdinates() {
    points.clear();
  }

}
