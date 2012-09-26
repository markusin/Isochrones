/**
 * 
 */
package isochrones.web.network.node;


/**
 * <p>
 * The <code>GNode</code> class
 * </p>
 * <p>
 * Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a>
 * </p>
 * <p>
 * Domenikanerplatz - Bozen, Italy.
 * </p>
 * <p>
 * </p>
 * 
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class GNode extends WebNode {

//  JGeometry geometry;
//  Set<RouteAnnotation> routeAnns = new HashSet<GNode.RouteAnnotation>();
  int reachedByRouteType = 0;
  Status status;

//  public GNode(WebNode n) {
//    super(n.getId());
//    setRoute(n.getRouteId());
//    setDistance(n.getDistance());
//  }
//
//  public void setGeometry(JGeometry geometry) {
//    this.geometry = geometry;
//  }
//
//  public JGeometry getGeometry() {
//    return geometry;
//  }
//
//  public void setStatus(Status status) {
//    this.status = status;
//  }
//
//  public void addAnnotation(int routeId, String routeName, int routeType) {
//    if (this.getRouteId() == routeId) {
//      reachedByRouteType = routeType;
//    }
//    routeAnns.add(new RouteAnnotation(routeName, routeType));
//  }
//
//  /**
//   * <p>
//   * Method toJSON
//   * </p>
//   * 
//   * @return
//   * @throws JSONException
//   */
//  public JSONObject toJSON() throws JSONException {
//    JSONObject jsonFeature = new JSONObject();
//    // jsonFeature.put(JSON.FEATURE_TYPE, JSON.FEATURE_TYPE_VALUE);
//    JSONObject geo = new JSONObject();
//    geo.put(JSON.GEOMETRY_TYPE, JSON.POINT_TYPE);
//    if (geometry == null) {
//      System.out.println("Node with id " + id + " has no geometry!!");
//    } else {
//      double[] ordinates = geometry.getPoint();
//      JSONArray point = new JSONArray();
//      point.put(ordinates[0]);
//      point.put(ordinates[1]);
//      geo.put(JSON.COORDINATES, point);
//      jsonFeature.put(JSON.GEOMETRY, geo);
//      JSONObject props = new JSONObject();
//      props.put(Feature.ID, id);
//      props.put(Feature.Node.DISTANCE, distance);
//      props.put(Feature.Node.STATUS, status);
//      props.put(Feature.Node.REACHED_BY_TYPE, reachedByRouteType);
//      JSONArray jsonAnns = new JSONArray();
//      for (RouteAnnotation ann : routeAnns) {
//        JSONObject jsonAnn = new JSONObject();
//        jsonAnn.put(Feature.Node.ROUTE_NAME, ann.getRouteName());
//        jsonAnn.put(Feature.Node.ROUTE_TYPE, ann.getRouteType());
//        jsonAnns.put(jsonAnn);
//      }
//      props.put(Feature.Node.ROUTES, jsonAnns);
//      jsonFeature.put(JSON.PROPERTIES, props);
//      return jsonFeature;
//    }
//    return null;
//  }
//
//  public class RouteAnnotation {
//    String routeName;
//    int routeType = Values.NOT_SET;
//
//    RouteAnnotation(String routeName, int routeType) {
//      this.routeName = routeName;
//      this.routeType = routeType;
//    }
//
//    public String getRouteName() {
//      return routeName;
//    }
//
//    public int getRouteType() {
//      return routeType;
//    }
//
//  }
}
