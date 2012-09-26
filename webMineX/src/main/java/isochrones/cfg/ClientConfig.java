package isochrones.cfg;

import isochrones.algorithm.Dataset;
import isochrones.web.JSON;
import isochrones.web.geometry.BBox;
import isochrones.web.geometry.Point;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ClientConfig implements Cloneable {
  
  private Dataset dataset;
  private BBox mbr;
  private Calendar arrivalTime;
  private Point queryPoint;
  private String linkLayer, linkBufferLayer, areaBufferLayer;
  
  public static DateFormat dateFormat = new SimpleDateFormat("MM'/'dd'/'yyyy' 'HH':'mm");
  
  
  public ClientConfig(Dataset dataset, BBox mbr, Calendar arrivalTime, Point queryPoint) {
    this.dataset = dataset;
    this.mbr = mbr;
    this.arrivalTime = arrivalTime;
    this.queryPoint = queryPoint;
  }
  
  public Dataset getDataset() {
    return dataset;
  }
  
  public BBox getExtent() {
    return mbr;
  }
  
  public Calendar getArrivalTime() {
    return arrivalTime;
  }
  
  public Point getQueryPoint() {
    return queryPoint;
  }
  
  public void setLinkLayer(String linkLayer) {
    this.linkLayer = linkLayer;
  }
  
  public String getLinkLayer() {
    return linkLayer;
  }
  
  /**
   * 
   * <p>Method getBufferLayer</p>
   * @return the name of the layer of the buffer table
   */
  public String getLinkBufferLayer(){
    return linkBufferLayer;
  }
  
  /**
   * 
   * <p>Method getAreaBufferLayer</p>
   * @return
   */
  public String getAreaBufferLayer(){
    return areaBufferLayer;
  }
  
  /**
   * 
   * <p>Method setBufferLayer</p> set the name of the buffer layer. 
   * Name must correspond to the layer name defined in the mapserver
   * @param bufferLayer
   */
  public void setLinkBufferLayer(String linkBufferLayer) {
    this.linkBufferLayer = linkBufferLayer;
  }
  
  public void setAreaBufferLayer(String areaBufferLayer) {
    this.areaBufferLayer = areaBufferLayer;
  }
  
  /**
   * 
   * <p>Method toJSON</p>
   * @return
   */
  public JSONObject toJSON(){
    JSONObject json = new JSONObject();
    try {
      json.put("name", dataset.toString());
      json.put("arrivalTime", dateFormat.format(arrivalTime.getTime()));
      JSONObject qPoint = new JSONObject();
      qPoint.put("x", queryPoint.getX());
      qPoint.put("y", queryPoint.getY());
      json.put("queryPoint", qPoint);
      JSONArray bboxVals = new JSONArray();
      bboxVals.put(mbr.getMinX());
      bboxVals.put(mbr.getMinY());
      bboxVals.put(mbr.getMaxX());
      bboxVals.put(mbr.getMaxY());
      json.put(JSON.BBOX, bboxVals);
      json.put("isoLinkLayer", linkLayer);
      if(linkBufferLayer!=null){
        json.put("isoLinkBufferLayer", linkBufferLayer);
      }
      if(areaBufferLayer!=null){
        json.put("isoAreaBufferLayer", areaBufferLayer);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return json;
  }
  
  @Override
  public Object clone() throws CloneNotSupportedException {
    ClientConfig cfg = new ClientConfig(dataset, mbr, arrivalTime, queryPoint);
    return cfg;
  }

  public void appendSessionId(String id) {
    linkLayer += "_" + id;
    linkBufferLayer += "_" + id;
    areaBufferLayer += "_" + id;
  }

}
