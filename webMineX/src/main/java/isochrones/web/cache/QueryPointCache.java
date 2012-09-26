/**
 * 
 */
package isochrones.web.cache;

import isochrones.network.Location;
import isochrones.web.network.node.QueryPoint;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * The <code>QueryPointCache</code> class
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
public class QueryPointCache {
  private Map<QueryPoint, Set<Location>> cachedQueryPoints = new HashMap<QueryPoint, Set<Location>>();

  /**
   * Class Constructor that creates an object containing key-value entries which are read from a file.
   */
  protected QueryPointCache() {
  }

  /*
   * The SingletonHolder containing the Config object
   */
  private static class SingletonHolder {
    private final static QueryPointCache INSTANCE = new QueryPointCache();
  }

  /**
   * <p>
   * Method addQueryPoint
   * </p>
   * 
   * @param qPoint
   * @param locs
   */
  public synchronized static void addQueryPoint(QueryPoint qPoint, Set<Location> locs) {
    SingletonHolder.INSTANCE.cachedQueryPoints.put(qPoint, locs);
  }

  /**
   * <p>
   * Method getCachedQueryPoint
   * </p>
   * 
   * @param qPoint
   * @return
   */
  public static Set<Location> getCachedQueryPoint(QueryPoint qPoint) {
    return SingletonHolder.INSTANCE.cachedQueryPoints.get(qPoint);
  }

}
