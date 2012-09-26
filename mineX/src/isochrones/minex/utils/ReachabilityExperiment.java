package isochrones.minex.utils;

import isochrones.minex.algorithm.test.ReachabilityTest;
import isochrones.utils.DBUtility;
import isochrones.utils.ReachabilityEntry;
import isochrones.utils.ReachabilityUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * 
*
* <p>The <code>ReachabilityExperiment</code> class</p> does a reachability experiment using different 
* arrival time intervals with the same query point. After some specific time points (e.g. every 10 minutes)
* there are measured the percentage of the entire network to be reached. 
* 
* <p>Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a></p>
* <p> Domenikanerplatz -  Bozen, Italy.</p>
* <p> </p>
* @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
* @version 2.2
 */
public class ReachabilityExperiment {

  public static void main(String[] args) throws SQLException {
    ReachabilityUtil util = new ReachabilityUtil(args, '=',"BINE");
    System.out.println(util);
    String fileName = util.getAlgorithmName() + "_"  + util.getDataset() + "_reachability_var";
    
    Map<Calendar, List<ReachabilityEntry>> experimentLogger = new TreeMap<Calendar, List<ReachabilityEntry>>();
    
    int networkSize = 0;
    
    Calendar targetTime = util.getStartTargetTime();
    do {
      ReachabilityTest algorithm = new ReachabilityTest(util.getConfig());
      algorithm.computeIsochrone(util.getLocations(), util.getMaxDuration(), util
          .getSpeed(), targetTime);
      if(networkSize==0) {
        networkSize = DBUtility.getNetworkSize(util.getConfig().getConnection(), util.getConfig().getEdgeTable(),util.getMode());
      }
      experimentLogger.put(targetTime, algorithm.getLogEntries());
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(targetTime.getTimeInMillis());
      cal.add(Calendar.MINUTE, +1);
      targetTime = cal;
    } while(targetTime.before(util.getEndTargetTime()));
    util.logIntoFile(experimentLogger, fileName, networkSize);
    /** End computation of the algorithm **/
  }
}
