package isochrones.mrnex.network.link;

import isochrones.network.link.DiscreteLink;

public class ADiscreteLink { // extends DiscreteLink {
  
  double srcDistance = Double.POSITIVE_INFINITY;
  
  DiscreteLink link;
  short sourceInDegree, targetInDegree;

  /**
   * 
   * <p>Constructs a(n) <code>ADiscreteLink</code> object.</p> is a wrapper class additional including the in-degree of the edges
   * @param link
   * @param sourceInDegree
   * @param targetInDegree
   */
  public ADiscreteLink(DiscreteLink link, short targetInDegree) {
    this.link = link;
    this.targetInDegree = targetInDegree;
  }
  
  /*
  public ADiscreteLink(DiscreteLink link, short sourceInDegree, short targetInDegree) {
    this.link = link;
    this.sourceInDegree = sourceInDegree;
    this.targetInDegree = targetInDegree;
  }
  */
  
  /**
   * 
   * <p>Method getSrcDistance</p> 
   * @return the distance of the source node
   */
  public double getSrcDistance() {
    return srcDistance;
  }
  
  /**
   * 
   * <p>Method getSourceInDegree</p>
   * @return
   */
  public short getSourceInDegree() {
    return sourceInDegree;
  }
  
  /**
   * 
   * <p>Method getTargetInDegree</p>
   * @return
   */
  public short getTargetInDegree() {
    return targetInDegree;
  }
  
  public DiscreteLink getLink() {
    return link;
  }
  
  public int getStartNodeId() {
    return link.getStartNodeId();
  }
  
  public int getEndNodeId() {
    return link.getEndNodeId();
  }
  
}
