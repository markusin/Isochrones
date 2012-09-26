package isochrones.minex.network.node;

import isochrones.network.node.Node;



/**
 * The <code>Node</code> class represents a node of the single bimodal network. 
 * A node is uniquely identified by a node ID. In addition, it has fields which 
 * allow to store its distance from the POI, the number of outgoing links and 
 * the number of links through which it has been reached.
 * 
 * @author     Markus Innerebner
 */
public class NodeBINE  extends Node {

    /**
     * counts the number of outgoing links and decrements it, if
     * an outgoing vertex has been visited(changes state to closed).
     */
    private short degreeCounter;
    
    /**
     * 
     * <p>Constructs a(n) <code>Node</code> object.</p> used for memory measurement
     */
    public NodeBINE() {}
    
    /**
     * Class Constructor that creates a node.
     * 
     * @param nodeId    the node ID
     */
    public NodeBINE(int nodeId) {
        super(nodeId);
    }
    
    /**
     * 
     * <p>Constructs a(n) <code>NodeBINE</code> object.</p>
     * @param nodeId
     * @param outgoingNodes
     */
    public NodeBINE(int nodeId, short outDegree) {
      super(nodeId);
      this.degreeCounter = outDegree;
    }
    
    
    /**
     * 
     * <p>Method registerVisitedAdjacentLinks</p>
     * @param visitedLinks the number of visited adjacent links
     */
    public void registerVisitedAdjacentLinks(short visitedLinks) {
      this.degreeCounter -= visitedLinks;
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
     * <p>Method sizeOf</p>
     * @return the size of each node object in bytes
     */
    public static int sizeOf() {
      return Node.sizeOf() + 32/8; // counter
    }
   
}