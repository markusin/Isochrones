package isochrones.web.utils;

public class SessionEntry {

  String sessionId, edgeTableName, vertexTableName, edgeLayerName, vertexLayerName, bufferTableName, bufferLayerName,vertexAnnotatedTableName;

  /**
   * <p>
   * Constructs a(n) <code>SessionEntry</code> object.
   * </p>
   * 
   * @param sessionId
   * @param edgeTableName
   * @param edgeLayerName
   * @param vertexTableName
   * @param vertexLayerName
   * @param vertexAnnotatedTableName
   * @param bufferTableName
   * @param bufferLayerName
   */
  public SessionEntry(String sessionId, String edgeTableName, String edgeLayerName, String vertexTableName,
                      String vertexLayerName, String vertexAnnotatedTableName,String bufferTableName, String bufferLayerName) {
    this.sessionId = sessionId;
    this.edgeTableName = edgeTableName;
    this.edgeLayerName = edgeLayerName;
    this.vertexTableName = vertexTableName;
    this.vertexLayerName = vertexLayerName;
    this.vertexAnnotatedTableName = vertexAnnotatedTableName;
    this.bufferTableName = bufferTableName;
    this.bufferLayerName = bufferLayerName;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getEdgeTableName() {
    return edgeTableName;
  }

  public String getVertexTableName() {
    return vertexTableName;
  }

  public String getEdgeLayerName() {
    return edgeLayerName;
  }

  public String getVertexLayerName() {
    return vertexLayerName;
  }

  public String getBufferLayerName() {
    return bufferLayerName;
  }

  public String getBufferTableName() {
    return bufferTableName;
  }
}
