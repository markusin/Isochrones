package isochrones.web.network;

public class RouteEntity {
  
  public static final short TYPE_PED = 0;
  public static final short TYPE_TRAM = 1;
  public static final short TYPE_TRAIN = 2;
  public static final short TYPE_BUS = 3;
  public static final short TYPE_FERRY = 4;
  public static final short TYPE_CABLECAR = 5;
  public static final short TYPE_GONDOLA = 6;
  public static final short TYPE_FUNICULAR = 7;
  
  int routeId; 
  short type = TYPE_BUS;
  String shortName = "undefined";

  public RouteEntity(int routeId) {
    this.routeId = routeId;
  }
  
  public RouteEntity(int routeId, String shortName, short type) {
    this.routeId = routeId;
    this.shortName = shortName;
    this.type = type;
  }

  /**
   * 
   * <p>Method getRouteId</p>
   * @return
   */
  public int getRouteId() {
    return routeId;
  }

  /**
   * 
   * <p>Method getShortName</p>
   * @return
   */
  public String getShortName() {
    return shortName;
  }

  /**
   * 
   * <p>Method getType</p>
   * @return
   */
  public short getType() {
    return type;
  }

}