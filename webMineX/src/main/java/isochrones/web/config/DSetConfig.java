package isochrones.web.config;

import isochrones.algorithm.Dataset;
import isochrones.algorithm.TableEntry;
import isochrones.algorithm.TableType;
import isochrones.db.DBVendor;
import isochrones.web.JSON;
import isochrones.web.geometry.BBox;
import isochrones.web.geometry.Point;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DSetConfig implements Cloneable {
    
    private Dataset dataset;
    private BBox clientMBR, serverMBR;
    
    private Calendar arrivalTime;
    private Point queryPoint;
    private String edgeLayer,vertexLayer, areaBufferLayer, vertexAnnotatedTableName;
    private TableEntry edgeTableEntry, vertexTableEntry, areaBufferTableEntry, vertexAnnotatedTableEntry;
    
    private int serverSRID;
    private String workspace;
    private DBVendor dbVendor;
    
    public static DateFormat dateFormat = new SimpleDateFormat("MM'/'dd'/'yyyy' 'HH':'mm");
    
    
    public DSetConfig(Dataset dataset, BBox serverMBR, BBox clientMBR, Calendar arrivalTime, Point queryPoint, int serverSRID, String workspace, DBVendor dbVendor) {
      this.dataset = dataset;
      this.serverMBR = serverMBR;
      this.clientMBR = clientMBR;
      this.arrivalTime = arrivalTime;
      this.queryPoint = queryPoint;
      this.serverSRID = serverSRID;
      this.workspace = workspace;
      this.dbVendor = dbVendor;
    }
    
    public Dataset getDataset() {
      return dataset;
    }
    
    public BBox getClientExtent() {
      return clientMBR;
    }
    
    public BBox getServerExtent() {
      return serverMBR;
    }
    
    public Calendar getArrivalTime() {
      return arrivalTime;
    }
    
    public Point getQueryPoint() {
      return queryPoint;
    }
    
    public void setEdgeLayer(String edgeLayer) {
      this.edgeLayer = edgeLayer;
    }
    
    public String getEdgeLayer() {
      return edgeLayer;
    }
    
    public void setVertexLayer(String vertexLayer) {
      this.vertexLayer = vertexLayer;
    }
    
    public String getVertexLayer() {
      return vertexLayer;
    }
    
    
    /**
     * 
     * <p>Method getAreaBufferLayer</p>
     * @return
     */
    public String getAreaBufferLayer(){
      return areaBufferLayer;
    }
    
    public void setAreaBufferLayer(String areaBufferLayer) {
      this.areaBufferLayer = areaBufferLayer;
    }
    
    public void setVertexAnnotatedTableName(String tablename) {
      this.vertexAnnotatedTableName = tablename; 
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
        json.put("prefix",workspace);
        JSONObject qPoint = new JSONObject();
        qPoint.put("x", queryPoint.getX());
        qPoint.put("y", queryPoint.getY());
        json.put("queryPoint", qPoint);
        JSONArray bboxVals = new JSONArray();
        bboxVals.put(clientMBR.getMinX());
        bboxVals.put(clientMBR.getMinY());
        bboxVals.put(clientMBR.getMaxX());
        bboxVals.put(clientMBR.getMaxY());
        json.put(JSON.BBOX, bboxVals);
        JSONObject edgeLayer = new JSONObject();
        edgeLayer.put("name", edgeTableEntry.getDescription());
        edgeLayer.put("layer", edgeTableEntry.getTableName());
        json.put("isoEdgeLayer", edgeLayer);
        
        JSONObject vertexLayer = new JSONObject();
        vertexLayer.put("name", vertexTableEntry.getDescription());
        vertexLayer.put("layer", vertexTableEntry.getTableName());
        json.put("isoVertexLayer", vertexLayer);
        
        JSONObject bufferLayer = new JSONObject();
        bufferLayer.put("name", areaBufferTableEntry.getDescription());
        bufferLayer.put("layer", areaBufferTableEntry.getTableName());
        json.put("isoAreaBufferLayer", bufferLayer);
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return json;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
      DSetConfig cfg = new DSetConfig(dataset, serverMBR, clientMBR, arrivalTime, queryPoint,serverSRID, workspace,dbVendor);
      cfg.edgeLayer = edgeLayer;
      cfg.vertexLayer = vertexLayer;
      cfg.vertexAnnotatedTableName = vertexAnnotatedTableName;
      cfg.areaBufferLayer = areaBufferLayer;
      return cfg;
    }

    public void appendSessionId(String id) {
      edgeLayer += "_" + id;
      vertexLayer += "_" + id;
      areaBufferLayer += "_" + id;
      vertexAnnotatedTableName += "_" + id;
      
      if(edgeLayer.length()>15) {
        String shortName = edgeLayer.substring(0,10) + "_" + edgeLayer.substring(edgeLayer.length()-6,edgeLayer.length());
        shortName = dbVendor.equals(DBVendor.ORACLE) ? shortName.toUpperCase() : shortName.toLowerCase();
        edgeTableEntry = new TableEntry(shortName, "sidx_" + shortName, TableType.LINK);
        edgeLayer = shortName;
      } 
      
      if(vertexLayer.length()>15) {
        String shortName = vertexLayer.substring(0,10) + "_" + vertexLayer.substring(vertexLayer.length()-6,vertexLayer.length());
        shortName = dbVendor.equals(DBVendor.ORACLE) ? shortName.toUpperCase() : shortName.toLowerCase();
        vertexTableEntry = new TableEntry(shortName, "sidx_" + shortName, TableType.NODE);
        vertexLayer = shortName;
      } 
      if(vertexAnnotatedTableName.length()>15) {
        String shortName = vertexAnnotatedTableName.substring(0,10) + "_" + vertexAnnotatedTableName.substring(vertexAnnotatedTableName.length()-6,vertexAnnotatedTableName.length());
        shortName = dbVendor.equals(DBVendor.ORACLE) ? shortName.toUpperCase() : shortName.toLowerCase();
        vertexAnnotatedTableEntry = new TableEntry(shortName, "sidx_" + shortName, TableType.NODE);
        vertexAnnotatedTableName = shortName;
      } 
      if(areaBufferLayer.length()>15) {
        String shortName = areaBufferLayer.substring(0,10) + "_" + areaBufferLayer.substring(areaBufferLayer.length()-6,areaBufferLayer.length());
        shortName = dbVendor.equals(DBVendor.ORACLE) ? shortName.toUpperCase() : shortName.toLowerCase();
        areaBufferTableEntry = new TableEntry(shortName, "sidx_" + shortName,TableType.POLYGON_BUFFER);
        areaBufferLayer = shortName;
      }
      
    }
    
    public TableEntry getEdgeTableEntry() {
      return edgeTableEntry;
    }
    
    public TableEntry getVertexTableEntry() {
      return vertexTableEntry;
    }
    
    public TableEntry getVertexAnnotatedTableEntry() {
		return vertexAnnotatedTableEntry;
	}
    
    public TableEntry getAreaBufferTableEntry() {
      return areaBufferTableEntry;
    }
    
    public int getServerSRID() {
      return serverSRID;
    }

    public void setWorkspace(String workspace) {
      this.workspace = workspace;
    }
    
    public String getWorkspace() {
      return workspace;
    }

}
