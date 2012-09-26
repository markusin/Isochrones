package isochrones.network;

/**
 * Thrown to indicate that a certain node does not exist.
 * 
 * @author     Markus Innerebner
 * @version    3.0
 */
public class NoSuchNodeException extends RuntimeException {

    /**
     * the ID of the non existing link
     */
    private int nodeId;

    /**
     * Constructs a <code>NoSuchLinkException</code> with the specified 
     * link ID.
     * 
     * @param nodeId    the ID of the non existing node
     */
    public NoSuchNodeException(int nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return "The passed node \"" + nodeId + "\" does not exist! Please " +
                "check the data.";
    }
}
