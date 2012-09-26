/**
 * 
 */
package isochrones.network;

import isochrones.network.node.INode;

/**
 *
 * <p>The <code>QueryPoint</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class QueryPoint implements INode {
  
  /**
   * the link on which the query points belongs to
   */
  private int id;
  /**
   * the start node offset
   */
  private double startOffset = Double.NEGATIVE_INFINITY;
  
  public QueryPoint(int linkId, double startOffset) {
    this.id = linkId;
    this.startOffset = startOffset;
  }
  
  public QueryPoint(int nodeId) {
    this.id = nodeId;
  }

  public double getStartOffset() {
    return startOffset;
  }
  
  public boolean isNode() {
    return startOffset==Double.NEGATIVE_INFINITY;
  }

  @Override
  public String toString() {
    return "QueryPoint [" + (isNode() ?  ("nodeId: " + id  + "]") :  ("linkId: " + id  + ", offset: " + startOffset +"]"));
  }

  @Override
  public double getDistance() {
    return 0;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public boolean isClosed() {
    return true;
  }

  @Override
  public void setClosed() {
  }
  
}
