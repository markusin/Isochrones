package isochrones.mrnex.algorithm.test;

import isochrones.algorithm.test.IRuntimeSizeTest;
import isochrones.mrnex.algorithm.MRNEX;
import isochrones.mrnex.network.node.ANode;
import isochrones.network.Location;
import isochrones.utils.Config;
import isochrones.utils.RuntimeEntry;

import java.util.Calendar;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 
*
* <p>The <code>RuntimeSizeTest</code> class</p>
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p> </p>
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
 */
public class RuntimeSizeTest extends MRNEX implements IRuntimeSizeTest {

	SortedMap<Integer, RuntimeEntry> runTimes = new TreeMap<Integer, RuntimeEntry>();
	long start;
	private int[] sizeCheckPoints;

	public RuntimeSizeTest(Config config, int[] sizeCheckPoints) {
    super(config);
    this.sizeCheckPoints = sizeCheckPoints;
	}

	@Override
  public void computeIsochrone(Location[] locations, int duration, double walkingSpeed, Calendar targetTime) {
    start = System.currentTimeMillis();
    super.computeIsochrone(locations, duration, walkingSpeed, targetTime);
  }
  
  @Override
  public void computeIsochrone(int[] nodeIds, int duration, double walkingSpeed, Calendar targetTime) {
    start = System.currentTimeMillis();
    super.computeIsochrone(nodeIds, duration, walkingSpeed, targetTime);
  }
	
  @Override
	public void compute(){
		ANode node = priorityQueue.poll();
		int i = 0;
		while (node != null) {
		  int isoSize = statistic.getExploredNodes();  
			if (i<sizeCheckPoints.length && isoSize >= sizeCheckPoints[i]) {
			  long t = System.currentTimeMillis() - start;
			  System.out.println(sizeCheckPoints[i] + "\t" + t);
				runTimes.put(sizeCheckPoints[i++], new RuntimeEntry(t, isoSize,node.getDistance()/60));
      }
			expandNode(node);
			node = priorityQueue.poll(); // Dequeues the next node
		}
		if (i < sizeCheckPoints.length) {
      long t = System.currentTimeMillis() - start;
      System.out.println(sizeCheckPoints[i] + "\t" + t);
      runTimes.put(sizeCheckPoints[i], new RuntimeEntry(t, statistic.getExploredNodes(),maxDuration/60));
    }
		terminate();
	}

  @Override
  public SortedMap<Integer, RuntimeEntry> getLogEntries() {
    return runTimes;
  }
  
  @Override
  public Config getConfig() {
    return config;
  }

}
