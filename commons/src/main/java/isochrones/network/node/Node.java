package isochrones.network.node;



/**
 * The <code>Node</code> class represents a node of the single bimodal network. 
 * A node is uniquely identified by a node ID. In addition, it has fields which 
 * allow to store its distance from the POI, the number of outgoing links and 
 * the number of links through which it has been reached.
 * 
 * @author     Markus Innerebner
 * @author     Willi Cometti
 * @version    3.0Node
 */
public class Node implements Comparable<Node> , INode {

    /**
     * the node ID
     */
    private int id;
    /**
     * the temporal distance computed during graph exploration
     */
    private double distance = INode.Value.INFINITY;
    
    
    /**
     * flag that specifies if the current node is closed/expanded
     */
    private boolean closed = false;
    
    /**
     * 
     * <p>Constructs a(n) <code>Node</code> object.</p> used for memory measurement
     */
    public Node() {}
    
    /**
     * Class Constructor that creates a node.
     * 
     * @param nodeId    the node ID
     */
    public Node(int nodeId) {
        id = nodeId;
    }
    

    @Override
    public int getId() {
        return id;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    /**
     * Sets the temporal distance of this node from the POI
     * 
     * @param distance    the temporal distance of this node from the POI
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }
    
    /**
     * 
     * <p>Method setClosed</p>
     * @param closed
     */
    public void setClosed() {
      this.closed = true;
    }
    
    /**
     * 
     * <p>Method isClosed</p>
     * @return
     */
    public boolean isClosed() {
      return closed;
    }

    @Override
    public int compareTo(Node other) {
      if(other==null) return -1;
      if(this==null) return 1;
    	if(this.distance<other.distance) return -1;
    	if(this.distance>other.distance) return 1;
    	return 0;
    }
    
    @Override
    public String toString() {
      return id + ","+ distance;
    }
    
    /**
     * 
     * <p>Method sizeOf</p>
     * @return the size of each node object in bytes
     */
    public static int sizeOf() {
      int size = 0;
      size += 32; // id (int)
      size += 64; // distance (double)
      size += 1;  // closed flag (boolean)
      return size/8;
    }
   
}