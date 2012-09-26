package isochrones.network;

import isochrones.network.link.Link;

/**
 * The <code>Offset</code> class represents a {@link Link} offset which defines the part of the link that is not
 * reachable.
 * <p>
 * A link offset consists of a distance from the link start node and a distance from the link end node. The distances
 * indicate the two outer link segments that cannot be reached. Moreover, these two segments delimit the reachable,
 * inner part of this link.
 * 
 * @author Gytis Tumas
 * @author Willi Cometti
 * @version 2.0
 */
public class Offset {

  /**
   * the start offset in respect to the start node
   */
  private double startOffset;
  /**
   * the end offset in respect to the start node
   */
  private double endOffset;

  /**
   * Class Constructor that uses two distances to create the <code>Offset
   * </code>.
   * 
   * @param startOffset the distance from the start node
   * @param endOffset the distance from the end node
   */
  public Offset(double startOffset, double endOffset) {
    this.startOffset = startOffset;
    this.endOffset = endOffset;
  }

  /**
   * Returns the beginning of the offset in respect to the start node
   * 
   * @return the start node offset
   */
  public double getStartOffset() {
    return startOffset;
  }

  /**
   * Returns the ending of the offset in respect to the start node
   * 
   * @return the end node offset
   */
  public double getEndOffset() {
    return endOffset;
  }
}
