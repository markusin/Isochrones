/**
 * 
 */
package isochrones.network.link;

import isochrones.network.Offset;

/**
 * <p>
 * The <code>ILink</code> class
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
public interface ILink {

  /**
   * Returns the ID which uniquely identifies this link.
   * 
   * @return the link ID
   */
  public int getId();

  /**
   * Returns the start node ID of this link.
   * 
   * @return the start node ID of this link
   */
  public int getStartNodeId();

  /**
   * Returns the end node ID of this link.
   * 
   * @return the end node ID of this link
   */
  public int getEndNodeId();

  /**
   * Returns the cost of this link.
   * 
   * @return the cost of this link
   */
  public double getLength();

  /**
   * Adds an offset to this link offsets. Offsets represent the unreachable segments of a link.
   * 
   * @param offset the computed offset
   */
  public void setOffset(Offset offset);

  /**
   * Returns all link offsets.
   * 
   * @return this link offsets
   */
  public Offset getOffset();

}
