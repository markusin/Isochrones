package isochrones.mrnex.network.node;

import isochrones.network.GeoPoint;
import isochrones.network.GeoQueryPoint;
import isochrones.network.node.INode;
import isochrones.network.node.Node;

/**
 * 
*
* <p>The <code>ANode</code> class</p> represents an Annotated Node
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p> </p>
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
 */
public class ANode extends Node {
  
  @Deprecated
  private byte mode;
  @Deprecated
  private boolean reachedByBus = false;
  @Deprecated
  private INode referenceNode;
  @Deprecated
  private boolean preExplored;
  @Deprecated
  private GeoQueryPoint queryPoint;
  
  public static final short NOT_SET = Short.MIN_VALUE;
  private short degreeCounter = NOT_SET;
  private GeoPoint coordinate;
  private double radius = Double.NEGATIVE_INFINITY;
  
  /**
   * 
   * <p>Constructs a(n) <code>ANode</code> object used if the degree is not available.</p>
   * @param nodeId
   */
  public ANode(int nodeId) {
    super(nodeId);
  }
  
  /**
   * 
   * <p>Constructs a(n) <code>ANode</code> object.</p>
   * @param nodeId
   * @param degree
   */
  public ANode(int nodeId, short degree) {
    super(nodeId);
    this.degreeCounter = degree;
  }

  @Deprecated 
  public ANode(int nodeId, byte mode, short degree) {
    super(nodeId);
    this.mode = mode;
    this.degreeCounter = degree;
  }
  
  /**
   * 
   * <p>Constructs a(n) <code>ANode</code> object.</p>
   * @param nodeId the id of the node
   * @param mode the mode of the node {CONTINUOUS OR DISCRETE}
   
  public ANode(int nodeId, byte mode) {
    super(nodeId);
    this.mode = mode;
  }
  */
  
  public void registerVisitedAdjacentLinks(short visitedLinks) {
    this.degreeCounter -= visitedLinks;
  }
  
  public boolean isSetOutDegreeCounter(){
    return degreeCounter!=Short.MIN_VALUE;
  }
  
  /**
   * 
   * <p>Method setOutDegreeCounter</p>
   * @param outDegreeCounter
   */
  public void setDegreeCounter(short degree) {
    this.degreeCounter = degree;
  }
  
  public short getDegree() {
    return degreeCounter;
  }

  /**
   * 
   * <p>Method isExpired</p> means he can be removed from the trace
   * @return
   */
  public boolean isExpired() {
    return degreeCounter==0;
  }
  
  
  
  /**
   * 
   * <p>Method getRadius</p>
   * @return
   */
  public double getRadius() {
    return radius;
  }
  
  /**
   * 
   * <p>Method setRadius</p>
   * @param radius
   */
  public void setRadius(double radius) {
    this.radius = radius;
  }
  
  /**
   * 
   * <p>Method getCoordinate</p>
   * @return
   */
  public GeoPoint getCoordinate() {
    return coordinate;
  }
  
  /**
   * 
   * <p>Method setCoordinate</p>
   * @param coordinate
   */
  public void setCoordinate(GeoPoint coordinate) {
    this.coordinate = coordinate;
  }
  
  /**
   * 
   * <p>Method getRemainingDistance</p>
   * @param walkingSpeed
   * @return
   */
  public double getRemainingDistance(double dMax, double walkingSpeed) {
    return (dMax-getDistance()) *walkingSpeed;
  }
  
  @Deprecated
  public byte getMode() {
    return mode;
  }
 
  @Deprecated
  public void setMode(byte mode) {
    this.mode = mode;
  }
  
  @Deprecated
  public boolean isReachedByBus() {
    return reachedByBus;
  }
  
  @Deprecated
  public void setReachedByBus(boolean reachedByBus) {
    this.reachedByBus = reachedByBus;
  }
  
  @Deprecated
  public void setReferenceNode(INode node) {
    this.referenceNode = node;
  }
  
  @Deprecated
  public void setQueryPoint(GeoQueryPoint queryPoint) {
    this.queryPoint = queryPoint;
  }
  
  @Deprecated
  public GeoQueryPoint getQueryPoint() {
    return queryPoint;
  }
  
  @Deprecated
  public INode getReferenceNode() {
    return referenceNode;
  }
  
  @Deprecated
  public boolean isQueryPoint() {
    return getId()==queryPoint.getId();
  }
  
  @Deprecated
  public boolean isPreExplored() {
    return preExplored;
  }
  
  @Deprecated
  public void setPreExplored(boolean preExplored) {
    this.preExplored = preExplored;
  }
    
}
