/**
 * 
 */
package isochrones.algorithm.datastructure;

import isochrones.network.Value;
import isochrones.network.node.Entity;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 *
 * <p>The <code>NearestEntities</code> class</p>
 * <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
 * <p> Domenikanerplatz -  Bozen, Italy.</p>
 * <p> </p>
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class NearestEntities {
  
  LinkedList<Entity> entities;
  int k;
  
  public NearestEntities(int k) {
    entities = new LinkedList<Entity>();
    this.k = k;
  }
  
  public Entity getEntity(int entityId){
    for (int i = 0; i < entities.size(); i++) {
      Entity entity = entities.get(i);
      if(entity.getId()==entityId){
        return entity;
      }
    }
    return null;
  }
  
  public boolean update(Entity entity) {
    boolean resort = false;
    boolean found = false;
    int i;
    for (i = 0; i < entities.size() && !found; i++) {
      Entity candidate = entities.get(i);
      if(candidate.equals(entity)){
        found = true;
        if(candidate.getDistance()>entity.getDistance()){
          candidate=entity;
          resort = true;
        }
      }
    }
    if(found && resort){
      Collections.sort(entities);
      return true;
    }
    if(!found){
      entities.add(entity);
      Collections.sort(entities);
      return true;
    } 
    return false;
  }
  
  /**
   * 
   * <p>Method getKthDistance</p>
   * @return
   */
  public double getKthDistance() {
    if(entities.size()<k) return Value.INFINITY;
    return entities.get(k-1).getDistance();
  }
  
  /**
   * 
   * <p>Method getKthEntities</p>
   * @return
   */
  public Collection<Entity> getKthEntities() {
    return entities.subList(0,k-1);
  }

}
