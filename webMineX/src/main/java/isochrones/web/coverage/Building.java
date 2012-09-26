package isochrones.web.coverage;


public class Building {
  private int houseID,inhabitants;
  private double startOffset;
  private double distanceToStreet;

  /**
   * 
   * <p>Constructs a(n) <code>Building</code> object.</p>
   * @param id
   * @param startOffset
   * @param distanceToStreet
   * @param inhabitants
   */
  public Building(int id, double startOffset, double distanceToStreet, int inhabitants) {
      this.houseID = id;
      this.startOffset = startOffset;
      this.distanceToStreet = distanceToStreet;
      this.inhabitants = inhabitants;
  }

  /**
   * 
   * <p>Method getHouseID</p>
   * @return
   */
  public int getHouseID() {
      return houseID;
  }

  /**
   * 
   * <p>Method getStartOffset</p>
   * @return
   */
  public double getStartOffset() {
      return startOffset;
  }
  
  /**
   * 
   * <p>Method getDistanceToStreet</p>
   * @return
   */
  public double getDistanceToStreet() {
    return distanceToStreet;
  }
  
  /**
   * 
   * <p>Method getInhabitants</p>
   * @return
   */
  public int getInhabitants() {
    return inhabitants;
  }
}
