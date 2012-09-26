package isochrones.network;


public class GeoQueryPoint extends QueryPoint {

  double x,y;
  
  public GeoQueryPoint(int linkId, double startOffset) {
    super(linkId, startOffset);
  }
  
  public GeoQueryPoint(int nodeId) {
    super(nodeId);
  }
  
  public void setCoordinates(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }
  
  public double getY() {
    return y;
  }

}
