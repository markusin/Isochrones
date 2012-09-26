package isochrones.network.link;

import isochrones.network.Offset;

/**
 * The <code>BusLink</code> class represents a link of the bus network. Since
 * bus networks are discrete in space and time, no offset information is needed.
 * In addition, while, for example, {@link ContinuousLink}s store cost information, the
 * cost for traversing a bus link is determined by means of a bus schedule.
 * 
 * @author Willi Cometti
 * @version 3.0
 */
public class DiscreteLink extends Link {

	/**
	 * the ID of the route the bus node belongs to
	 */
	private short routeId;

	/**
	 * 
	 * <p>Constructs a(n) <code>BusLink</code> object.</p> used for memory measurement
	 */
	public DiscreteLink() {}

	/**
	 * Class Constructor that uses the link, start node, end node and route IDs
	 * to create the link object.
	 * 
	 * @param linkId
	 *            the link ID
	 * @param startNodeId
	 *            the link start node ID
	 * @param endNodeId
	 *            the link end node ID
	 * @param routeId
	 *            the route ID this link belongs to
	 */
	public DiscreteLink(int linkId, int startNodeId, int endNodeId, short routeId) {
		this.id = linkId;
		this.startNodeId = startNodeId;
		this.endNodeId = endNodeId;
		this.routeId = routeId;
	}

	/**
	 * Returns the ID of the route this node belongs to.
	 * 
	 * @return the ID of the route this node belongs to
	 */
	public short getRouteId() {
		return routeId;
	}

  @Override
  public void setOffset(Offset arg0) {
    // TODO Auto-generated method stub
  }

  @Override
  public double getLength() {
    return 0;
  }

  @Override
  public Offset getOffset() {
    return null;
  }
  
  /**
   * 
   * <p>Method sizeOf</p>
   * @return the size of each bus link object in bytes
   */
  public static int sizeOf() {
    int size = 0;
    size += 32;   // node id
    size += 32;   // start node id
    size += 32;   // end node id
    size += 16;   // route_id
    size += 16*3; // type
    return size/8;
  }
  
  @Override
  public String toString() {
    String s = super.toString();
    s = s.substring(0, s.lastIndexOf(")"));
    s += "," + routeId + ")";
    return s; 
  }
	
}
