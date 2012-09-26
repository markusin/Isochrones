/**
 * 
 */
package isochrones.web.db;

import isochrones.db.DBResult;
import isochrones.network.link.ILink;
import isochrones.web.coverage.IsoEdge;
import isochrones.web.geometry.BBox;
import isochrones.web.geometry.Point;
import isochrones.web.network.node.QueryPoint;
import isochrones.web.network.node.WebNode;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * <p>The <code>WebQuery</code> class</p> provides methods used for the web based version
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public interface IWebQuery {
  
  /**
   * 
   * <p>Method projectOnLinks</p>
   * @param qPoint
   * @return
   * @throws SQLException
   */
  public DBResult projectOnLinks(QueryPoint qPoint) throws SQLException;
  
  /**
   * 
   * <p>Method getNodeAnnotation</p> shows the annotated information of a node
   * @param nodeId the node id from where to find the annotated info
   * @return the annotated information of the node containing the route information
   * @throws SQLException
   */
  public DBResult getNodeAnnotation(int nodeId) throws SQLException;


  /**
   * 
   * <p>Method storeVertices</p>
   * @param storableNodes
   * @throws SQLException
   */
  public void storeVertices(Set<WebNode> storableNodes);
  
  /**
   * 
   * <p>Method getIsochroneBoundingBox</p> returns the bounding box of the isochrone
   * @return
   */
  public BBox getIsochroneBoundingBox();
  
  /**
   * 
   * <p>Method transform</p> transform the geometry
   * @param p
   * @return
   */
  public Point transform(Point p);
  
  
  /**
   * <p>
   * Method getTotalNumberOfInhabitants
   * </p>
   * 
   * @return
   */
  public int getTotalNumberOfInhabitants();
  
  /**
   * 
   * <p>Method createBuffer</p>
   * @param links
   * @param bufferSize the buffer size in meters
   * @param incoming true if incoming directions are considered
   */
  public void createBuffer(Collection<IsoEdge> edges, double bufferSize, boolean incoming);
  
  /**
   * 
   * <p>Method getWebConfig</p>
   * @return
   */
  public isochrones.web.config.Config getWebConfig();
  
  
  /**
   * 
   * <p>Method getIsochoneEdges</p>
   * @param dMax
   * @param speed
   * @param avoidNonPartialDuplicates
   * @return
   */
    public Collection<IsoEdge> getIsochoneEdges(int dMax, double speed, boolean avoidNonPartialDuplicates);
    
    /**
     * 
     * <p>Method storeArea</p> insert an area around this edges
     * @param areaId
     * @param points
     * @param bufferSize
     */
    public void storeArea(int areaId, List<Point> points, double bufferSize);
    
    
    public void storeAreaFromEdges(int areaId, List<IsoEdge> borderEdges, double bufferSize);


    /**
     * 
     * <p>Method edgesInArea</p> returns the edges residing in the area with the given id
     * @param areaId the area identifier
     * @return
     * @throws SQLException
     */
    public DBResult edgesInArea(int areaId) throws SQLException;
    
    
    /**
     * 
     * <p>Method reachedInhabitants</p>
     * @return
     */
    public int reachedInhabitants();
    
    /**
     * 
     * <p>Method totalInhabitants</p>
     * @return
     */
    public int totalInhabitants();

    
    /**
     * 
     * <p>Method updateVertexTable</p> updates the vertex table with additional information: route_type
     */
    public void updateVertexTable();
    
    /**
     * 
     * <p>Method getAnnotation</p> returns the annotated information of a vertex
     * @param vertexId
     * @return
     * @throws SQLException
     */
    public DBResult getAnnotation(int vertexId) throws SQLException;
    
}
