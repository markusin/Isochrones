package isochrones.web.coverage;

import isochrones.network.Offset;
import isochrones.network.link.ContinuousLink;
import isochrones.web.geometry.AbstractLineString;

public class IsoEdge extends ContinuousLink {

  double adjNodeDistance, remainingDistance;
  boolean processed;
  
  IsoEdge invertedEdge;
  
  AbstractLineString geometry;

  /**
   * @param id
   * @param sourceId
   * @param targetId
   * @param length
   * @param adjNodeDistance
   * @param offset
   */
  public IsoEdge(int id, int sourceId, int targetId, double length, double adjNodeDistance, Offset offset, AbstractLineString geometry) {
    super(id, sourceId, targetId, length);
    this.adjNodeDistance = adjNodeDistance;
    setOffset(offset);
    this.geometry = geometry;
  }

  /**
   * @return
   */
  public double getAdjNodeDistance() {
    return adjNodeDistance;
  }

  /**
	 * 
	 */
  public String getIdentifier() {
    return getStartNodeId() + "," + getEndNodeId();
  }

  /**
	   * 
	   */
  public String getInvertedIdentifier() {
    return getEndNodeId() + "," + getStartNodeId();
  }

  public double getRemainingDistance() {
    return remainingDistance;
  }

  public void setRemainingDistance(double remainingDistance) {
    this.remainingDistance = remainingDistance;
  }
  
  /**
   * 
   * <p>Method getGeometry</p>
   * @return
   */
  public AbstractLineString getGeometry() {
    return geometry;
  }
  
  /**
   * 
   * <p>Method setProcessed</p>
   * @param processed
   */
  public void setProcessed(boolean processed) {
    this.processed = processed;
  }
  
  /**
   * 
   * <p>Method isProcessed</p>
   * @return
   */
  public boolean isProcessed() {
    return processed;
  }
  
  public boolean isPartial() {
    return getOffset().getStartOffset()>0 || getOffset().getEndOffset()<getLength();
  }
  
  /**
   * 
   * @param mergingEdge
   */
  public void mergeGeometry(IsoEdge mergingEdge){
    if (mergingEdge.isPartial()) {
    	geometry = geometry.merge(mergingEdge.getGeometry().reverse());
    } else {
    	geometry = mergingEdge.getGeometry().reverse();
    }
  }
  
  public boolean inverted(IsoEdge other) {
    return other.getStartNodeId() == this.getEndNodeId() && other.getEndNodeId() == this.getStartNodeId();
  }
  
  /**
   * 
   * <p>Method setInvertedEdge</p>
   * @param invertedEdge
   */
  public void setInvertedEdge(IsoEdge invertedEdge) {
    this.invertedEdge = invertedEdge;
  }
  
  /**
   * 
   * <p>Method getInvertedEdge</p>
   * @return
   */
  public IsoEdge getInvertedEdge() {
    return invertedEdge;
  }

  /*
    public void _mergeGeometry(IsoEdge mergingEdge){
        if (mergingEdge.isPartial()) {
        
          //double[] ordinatesArrayMy = this.getGeometry().getOrdinatesArray();
          //this.getGeometry().getOrdinatesArray();
          //double[] ordinatesArrayInverted = mergingEdge.getGeometry().getOrdinatesArray();

          double minDist = SBAUtil.eucideanDist(mergingLine.getFirstPoint(), geometry.getFirstPoint());
          int i = 1;
          for (i = 1; i < mergingLine.numPoints(); i ++) {
            if (minDist < SBAUtil.eucideanDist(mergingLine.getPoint(i), geometry.getPoint(i))) {
              break;
            }
          }
          // position i is stored
          Point[] ordsNew = new Point[geometry.numPoints() + mergingLine.numPoints()-i];
          int k = 0;
          for (int j = mergingLine.numPoints()-1; j >= i; j -- ,k ++) {
            ordsNew[k] = mergingLine.getPoint(j);
          }
          
          System.arraycopy(geometry.getPoints(), 0, ordsNew, k, ordsNew.length);
          /*
          for (int j = ordinatesArrayMy.length ; j < ordsNew.length; j += 2,i +=2) {
            ordsNew[j] = ordinatesArrayInverted[i];
            ordsNew[j+1] = ordinatesArrayInverted[i+1];
          }
          
          //geometry = JGeometry.createLinearLineString(ordsNew, 2,geometry.getSRID());

        } else {
          double[] ords = mergingEdge.getGeometry().getOrdinatesArray();
          double[] ordsNew = new double[ords.length];
          int i=0;
          for (int j = ords.length - 2; j >=0; j -= 2,i+=2) {
            ordsNew[i] = ords[j];
            ordsNew[i+1] = ords[j+1];
          }
          geometry = JGeometry.createLinearLineString(ordsNew, 2,geometry.getSRID());
        }
        */

}
