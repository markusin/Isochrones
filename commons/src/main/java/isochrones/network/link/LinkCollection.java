/**
 * 
 */
package isochrones.network.link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * <p>The <code>LinkCollection</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class LinkCollection {
  
  private Collection<ContinuousLink> continuousLinks = new ArrayList<ContinuousLink>();
  private Collection<DiscreteLink> discreteLinks;
  
  private Set<Integer> adjacentDiscreteNodes;
  private Set<Short> routes;

  /**
   * 
   * <p>Constructs a(n) <code>LinkCollection</code> object.</p>
   */
  public LinkCollection() {
  }
  
  /**
   * 
   * <p>Constructs a(n) <code>LinkCollection</code> object.</p>
   * @param initDiscreteLinks
   */
  public LinkCollection(boolean initDiscreteLinks) {
    if(initDiscreteLinks) {
      discreteLinks = new ArrayList<DiscreteLink>();
    }
  }

  /**
   * 
   * <p>Method add</p>
   * @param link
   */
  public void add(ILink link){
    if(link instanceof ContinuousLink) add((ContinuousLink)link);
    else add((DiscreteLink)link);
  }
  
  /**
   * 
   * <p>Method addAll</p>
   * @param links
   */
  public void addAll(Collection<ILink> links, boolean incoming) {
    for (ILink link : links) {
      if(link instanceof ContinuousLink) add((ContinuousLink)link);
      else add((DiscreteLink)link,incoming);
    }
  }
  
  public void addAllDiscreteLinks(Collection<DiscreteLink> links) {
    for (DiscreteLink link : links) {
      add(link);
    }
  }
  
  
  /**
   * 
   * <p>Method add</p> adds a continuous link to the collection
   * @param link the continuous link to be added
   */
  public void add(ContinuousLink link){
    continuousLinks.add(link);
  }
  
  
  /**
   * 
   * <p>Method add</p>
   * @param link link the discrete link to be added 
   * @param inCommingMode the mode to be used
   */
  public void add(DiscreteLink link, boolean inCommingMode){
    if(discreteLinks==null){
      discreteLinks = new HashSet<DiscreteLink>();
    }
    discreteLinks.add(link);
    if(adjacentDiscreteNodes==null){
      adjacentDiscreteNodes = new HashSet<Integer>();
    }
    adjacentDiscreteNodes.add(inCommingMode ? link.getStartNodeId() : link.getEndNodeId());
    if(routes==null){ 
      routes = new HashSet<Short>();
    }
    routes.add(link.getRouteId());
  }  
  
  /**
   * 
   * <p>Method getContinuousLinks</p>
   * @return
   */
  public Collection<ContinuousLink> getContinuousLinks(){
    return continuousLinks;
  }
  
  /**
   * 
   * <p>Method getDiscreteLinks</p>
   * @return
   */
  public Collection<DiscreteLink> getDiscreteLinks(){
    return discreteLinks;
  }
  
  /**
   * 
   * <p>Method sizeDiscreteLinks</p>
   * @return
   */
  public int sizeDiscreteLinks() {
    if (discreteLinks==null) return 0;
    return discreteLinks.size();
  }
  
  /**
   * 
   * <p>Method isEmpty</p>
   * @return
   */
  public boolean isEmpty() {
    int size = continuousLinks.size() + (discreteLinks!=null ? discreteLinks.size() : 0);
    return size==0;
  }
  
  /**
   * 
   * <p>Method getAdjacentDiscreteNodeSize</p>
   * @return
   */
  public int getAdjacentDiscreteNodeSize(){
    if(adjacentDiscreteNodes==null) return 0;
    return adjacentDiscreteNodes.size();
  }
  
  /**
   * 
   * <p>Method getAdjacentDiscreteNodes</p>
   * @return
   */
  public Set<Integer> getAdjacentDiscreteNodes() {
    return adjacentDiscreteNodes;
  }
  
  /**
   * 
   * <p>Method getRouteIds</p>
   * @return
   */
  public Set<Short> getRouteIds() {
    return routes;
  }

  

}
