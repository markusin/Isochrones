package isochrones.web.network.node;

import isochrones.network.node.Node;
import isochrones.web.network.Schedule;

import java.util.Hashtable;

/**
 * The <code>Node</code> class represents a node of the single bimodal network. A node is uniquely identified by a node
 * ID. In addition, it has fields which allow to store its distance from the POI, the number of outgoing links and the
 * number of links through which it has been reached.
 * 
 * @author Markus Innerebner
 * @author Willi Cometti
 * @version 3.0
 */
public class WebNode extends Node {

  private Hashtable<Integer, Schedule> routes;
  private int cheapestReachedRouteId = Value.NOT_SET;
  private Status state = Status.OPEN;
  
  /**
   * counts the number of outgoing link and decrements it, if
   * an outgoing vertex is set to the closed state.
   */
  protected int outDegreeCounter;
  
  /**
   * 
   * <p>Constructs a(n) <code>Node</code> object.</p> used for memory measurement
   */

  /**
   * <p>
   * Constructs a(n) <code>Node</code> object.
   * </p>
   * used for memory measurement
   */
  public WebNode(){}
    

  // state = node.isExpired() ? Status.EXPIRED : (node.isClosed() ? Status.CLOSED : Status.OPEN);
  
  /**
   * Class Constructor that creates a node.
   * 
   * @param nodeId the node ID
   */
  public WebNode(int nodeId) {
    super(nodeId);
  }

  public WebNode(int nodeId, int outgoingNodes) {
    super(nodeId);
    this.outDegreeCounter = outgoingNodes;
  }
  
  @Override
  public void setClosed() {
    super.setClosed();
    this.state = Status.CLOSED;
  }
  
  /**
   * 
   * <p>Method registerVisitedOutLinks</p> decrements the counter of visited adjacent edges.
   * @param visitedLinks the number of visited adjacent links
   */
  public void registerVisitedAdjacentLinks(short visitedLinks) {
    this.outDegreeCounter -= visitedLinks;
  }
   

  /**
   * 
   * <p>Method isExpired</p> means he can be removed from the trace
   * @return
   */
  public boolean isExpired() {
    if(outDegreeCounter==0) {
      setState(Status.EXPIRED);
      return true;
    }
    return false;
  }
  
  /**
   * 
   * <p>Method sizeOf</p>
   * @return the size of each node object in bytes
   */
  public static int sizeOf() {
    return Node.sizeOf() + 32/8; // counter
  }

  public Hashtable<Integer, Schedule> getRouteSchedules() {
    return routes;
  }
  
  public void setState(Status state) {
    this.state = state;
  }
  
  public Status getState() {
    return state;
  }

  /**
   * <p>
   * Method addRoute
   * </p>
   * 
   * @param routeId
   * @param routeEntity
   */
  public void addRoute(int routeId) {
    if (routes == null) {
      routes = new Hashtable<Integer, Schedule>();
    }
    routes.put(routeId, new Schedule(routeId));
  }
  
  public void setArrivalTime(int routeId, long arrivalTime) {
    if (routes == null) {
      routes = new Hashtable<Integer, Schedule>();
    }
    if(!routes.containsKey(routeId)) {
      routes.put(routeId, new Schedule(routeId));
    }
    routes.get(routeId).setArrivalTime(arrivalTime);
  }
  
  public void setDepartureTime(int routeId, long departureTime) {
    if (routes == null) {
      routes = new Hashtable<Integer, Schedule>();
    }
    if(!routes.containsKey(routeId)) {
      routes.put(routeId, new Schedule(routeId));
    }
    routes.get(routeId).setDepartureTime(departureTime);
  }

  public void setCheapestReachedRouteId(int cheapestReachedRouteId) {
    this.cheapestReachedRouteId = cheapestReachedRouteId;
  }

  /**
   * 
   * <p>Method getCheapestReachedRouteId</p>
   * @return
   */
  public int getCheapestReachedRouteId() {
    return cheapestReachedRouteId;
  }

  /**
   * <p>
   * Method isRouteEmpty
   * </p>
   * 
   * @return
   */
  public boolean containsRoutes() {
    return routes == null ? false : !routes.isEmpty();
  }

  public void setEmptyRoute() {
    routes.clear();
  }

  public enum Status {
    OPEN, CLOSED, EXPIRED
  }

}