package isochrones.web;

public interface Feature {
  
  public final static String ID = "id";
  
  public final static char OPEN_BRACKET = '[';
  public final static char CLOSING_BRACKET = ']';
  public final static char COMMA = ',';
  
  public interface Link {
    public final static String SOURCE = "startnodeId";
    public final static String TARGET = "endnodeId";
    public final static String LENGTH = "length";
    public final static String PARTIAL = "partial";
  }
  
  public interface Node {
    public final static String DISTANCE = "distance";
    public final static String ROUTE_NAME = "routeName";
    public final static String ROUTE_TYPE = "routeType";
    public final static String ROUTES = "routes";
    public final static String REACHED_BY_TYPE = "reachedByType";
    public final static String STATUS = "status";
  }
}
