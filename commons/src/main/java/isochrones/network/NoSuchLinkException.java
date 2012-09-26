package isochrones.network;

/**
 * Thrown to indicate that a certain link does not exist.
 * 
 * @author     Willi Cometti
 * @version    2.0
 */
public class NoSuchLinkException extends RuntimeException {

    /**
     * the ID of the non existing link
     */
    private int linkId;

    /**
     * Constructs a <code>NoSuchLinkException</code> with the specified 
     * link ID.
     * 
     * @param linkId    the ID of the non existing link
     */
    public NoSuchLinkException(int linkId) {
        this.linkId = linkId;
    }

    @Override
    public String toString() {
        return "The passed link \"" + linkId + "\" does not exist! Please " +
                "check the data.";
    }
}
