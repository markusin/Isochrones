package isochrones.network;

/**
 * 
*
* <p>The <code>Location</code> class</p> 
* represents an object consisting of a link id and its offset in respect to the source vertex
* 
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p> </p>
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
 */
public class Location {
  
  private int linkId;
  private double offset;

  /**
   * 
   * <p>Constructs a(n) <code>Location</code> object.</p>
   * @param linkId
   * @param offset
   */
  public Location(int linkId, double offset) {
    this.linkId = linkId;
    this.offset = offset;
  }
  
  /**
   * 
   * <p>Method getLinkId</p>
   * @return the link id belonging to the location
   */
  public int getLinkId() {
    return linkId;
  }
  
  /**
   * 
   * <p>Method getOffset</p>
   * @return the offset in respect to the source vertex
   */
  public double getOffset() {
    return offset;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Location)) return false;
    Location other = (Location) obj;
    return other.getLinkId()==this.getLinkId() && other.getOffset()==this.getOffset();
  }
  
  @Override
  public int hashCode() {
    return (int) (linkId + offset);
  }
  
  @Override
  public String toString() {
    return "linkId: " + linkId + ", offset: " + offset;
  }
  
  /**
   * 
   * <p>Method toStringNotation</p>
   * @return
   */
  public String toStringNotation() {
    return linkId + "#" + offset;
  }

}
