package isochrones.cfg;

import isochrones.algorithm.TableEntry;
import isochrones.algorithm.TableType;

public class ServerConfig {
  
  private String linkLayer, linkBufferLayer, areaBufferLayer;
  private TableEntry linkTableEntry, linkBufferTableEntry, areaBufferTableEntry;

  /**
   * 
   * <p>Constructs a(n) <code>ServerConfig</code> object.</p>
   * @param clientConfig
   * @param sessionId
   */
  public ServerConfig(ClientConfig clientConfig, String sessionId) {
      this.linkLayer = clientConfig.getLinkLayer() + sessionId ;
      if(linkLayer.length()>15) {
        String shortName = linkLayer.substring(0,9) + "_" + linkLayer.substring(linkLayer.length()-4,linkLayer.length());
        linkTableEntry = new TableEntry(shortName, "sidx_" + shortName, TableType.LINK);
      } 
      this.linkBufferLayer = clientConfig.getLinkBufferLayer() + sessionId ;
      if(linkBufferLayer.length()>15) {
        String shortName = linkBufferLayer.substring(0,9) + "_" + linkBufferLayer.substring(linkBufferLayer.length()-4,linkBufferLayer.length());
        linkBufferTableEntry = new TableEntry(shortName, "sidx_" + shortName,TableType.POLYGON_BUFFER);
      }
      this.areaBufferLayer = clientConfig.getAreaBufferLayer() + sessionId ;
      if(areaBufferLayer.length()>15) {
        String shortName = areaBufferLayer.substring(0,9) + "_" + areaBufferLayer.substring(areaBufferLayer.length()-4,areaBufferLayer.length());
        areaBufferTableEntry = new TableEntry(shortName, "sidx_" + shortName,TableType.POLYGON_BUFFER);
      }
  }
  
  public String getLinkLayer() {
    return linkLayer;
  }
  
  public TableEntry getLinkTableEntry() {
    return linkTableEntry;
  }
  
  
  public String getAreaBufferLayer() {
    return areaBufferLayer;
  }
  
  public TableEntry getAreaBufferTableEntry() {
    return areaBufferTableEntry;
  }
  
  public TableEntry getLinkBufferTableEntry() {
    return linkBufferTableEntry;
  }
  
  public String getLinkBufferLayer() {
    return linkBufferLayer;
  }

}
