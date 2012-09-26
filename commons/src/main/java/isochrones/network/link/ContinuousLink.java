package isochrones.network.link;

import isochrones.network.Offset;

/**
 * The <code>PedLink</code> class represents a link of the pedestrian 
 * network. Pedestrian links also store their cost and a list of disjoint 
 * {@link Offset}s.
 *  
 * @author     Willi Cometti
 * @version    3.0 
 */
public class ContinuousLink extends Link {

    /**
     * the link cost
     */
    private double length;
    /**
     * the link offsets
     */
    private Offset offset;
    
    /**
     * 
     * <p>Constructs a(n) <code>PedLink</code> object.</p> used for memory measurement
     */
    public ContinuousLink() {
      // TODO Auto-generated constructor stub
    }

    /**
     * Class Constructor that uses the link, start and end node IDs, and the 
     * link cost to create the link object.
     * 
     * @param linkId         the link ID
     * @param startNodeId    the link start node ID 
     * @param endNodeId      the link end node ID
     * @param linkCost       the link cost
     */
    public ContinuousLink(int linkId, int startNodeId, int endNodeId,
            double linkCost) {
        this.id = linkId;
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.length = linkCost;
    }

    /**
     * Returns the cost of this link.
     * 
     * @return    the cost of this link
     */
    public double getLength() {
        return length;
    }
    
    public void setLength(double length) {
      this.length = length;
    }

    /**
     * Returns a list of link offsets. 
     * 
     * @return    the link offsets
     */
    public Offset getOffset() {
        return offset;
    }

    /**
     * Adds an offset to this link offsets. Offsets represent the segments of a 
     * link that are not reachable.
     * 
     * @param offset    the computed offset
     */
    public void setOffset(Offset offset) {
      this.offset = offset;
    }
    

    /**
     * 
     * <p>Method sizeOf</p>
     * @return the size of each bus link object in bytes
     */
    public static int sizeOf() {
      int size = 0;
      size += 32; // node id
      size += 32; // start node id
      size += 32; // end node id
      size += 64; // length
      size += 128;// offset 
      return size/8;
    }
}
