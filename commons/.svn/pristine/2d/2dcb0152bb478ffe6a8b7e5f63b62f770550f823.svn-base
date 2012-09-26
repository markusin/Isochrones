package isochrones.network.link;


/**
 * The <code>Link</code> abstract class is a base class for network (graph) 
 * links. It allows to store the link ID, the start node ID and the end node ID.
 * 
 * @author     Willi Cometti
 * @author     Markus Innerebner
 * @version    3.0
 */
public abstract class Link implements ILink{

    /**
     * the link ID
     */
    protected int id;
    /**
     * the start node ID
     */
    protected int startNodeId;
    /**
     * the end node ID
     */
    protected int endNodeId;

    /**
     * Returns the ID which uniquely identifies this link.
     * 
     * @return    the link ID
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the ID of the start node of this link.
     * 
     * @return    the start node ID
     */
    public int getStartNodeId() {
        return startNodeId;
    }

    /**
     * Returns the ID of the end node of this link.
     * 
     * @return    the end node ID
     */
    public int getEndNodeId() {
        return endNodeId;
    }
    
    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      b.append("(").append(id).append(",(").append(startNodeId).append(",").append(endNodeId).append("))");
      return b.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
      return getId()==((ILink) obj).getId() ;
    }
    
    @Override
    public int hashCode() {
      return getId()*13;
    }
}
